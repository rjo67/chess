package org.rjo.chess;

import java.util.Iterator;

import org.rjo.chess.pieces.Knight;
import org.rjo.chess.pieces.Pawn;
import org.rjo.chess.pieces.PieceType;
import org.rjo.chess.ray.BaseRay;
import org.rjo.chess.ray.Ray;
import org.rjo.chess.ray.RayType;
import org.rjo.chess.ray.RayUtils;
import org.rjo.chess.util.BitSetUnifier;

/**
 * Routines to discover if the king is in check.
 * <p>
 * Usage: construct the object with a position and then call {@link #isKingInCheck(Move, boolean)}. <br>
 * There are also various static methods which can be called directly.
 *
 * @author rich
 */
public class KingCheck {

    private BitSetUnifier friendlyPieces;
    private BitSetUnifier[] enemyPieces;
    private Square myKing;
    private Colour myColour;
    private Colour opponentsColour;
    private Position posn;

    public KingCheck(Position posn, Colour opponentsColour, Square myKing) {
        this.posn = posn;
        this.myColour = Colour.oppositeColour(opponentsColour);
        this.opponentsColour = opponentsColour;
        friendlyPieces = posn.getAllPieces(myColour).getBitSet();
        enemyPieces = Position.setupEnemyBitsets(posn.getPieces(opponentsColour));
        this.myKing = myKing;
    }

    /**
     * Checks if my king is in check after <code>move</code>. Uses the position supplied in the constructor.
     *
     * @param move                 the move
     * @param kingIsAlreadyInCheck true if the king was already in check before the <code>move</code>
     * @return true if this move leaves the king in check (i.e. is an illegal move)
     */
    public boolean isKingInCheck(Move move,
                                 boolean kingIsAlreadyInCheck) {
        // need to distinguish capture moves here, since the KingCheck method changes 'enemyPieces' for a capture
        if (move.isCapture()) {
            return isKingInCheck(posn, move, opponentsColour, myKing, kingIsAlreadyInCheck);
        } else {
            return isKingInCheckAfterMove(myKing, myColour, friendlyPieces, enemyPieces, move, kingIsAlreadyInCheck);
        }
    }

    /**
     * Checks if my king is in check after <code>move</code>.
     *
     * @param posn                 the chessboard
     * @param move                 the move
     * @param opponentsColour      this colour's pieces will be inspected to see if they check my king
     * @param king                 where my king is
     * @param kingIsAlreadyInCheck true if the king was already in check before the <code>move</code>
     * @return true if this move leaves the king in check (i.e. is an illegal move)
     */
    public static boolean isKingInCheck(Position posn,
                                        Move move,
                                        Colour opponentsColour,
                                        Square king,
                                        boolean kingIsAlreadyInCheck) {

        // short circuit if king was not in check beforehand (therefore only
        // need to check for a pinned piece) and the moving piece's original square is not on a ray to the king
        if (!kingIsAlreadyInCheck && move.getPiece() != PieceType.KING) {
            if (null == RayUtils.getRay(king, move.from())) {
                return false;
            }
        }

        BitSetUnifier friendlyPieces = posn.getAllPieces(Colour.oppositeColour(opponentsColour)).getBitSet();
        BitSetUnifier[] enemyPieces = Position.setupEnemyBitsets(posn.getPieces(opponentsColour));

        if (kingIsAlreadyInCheck) {
            return isKingInCheckAfterMove_PreviouslyWasInCheck(king, Colour.oppositeColour(opponentsColour), friendlyPieces, enemyPieces,
                    move);
        } else {
            return isKingInCheckAfterMove_PreviouslyNotInCheck(king, Colour.oppositeColour(opponentsColour), friendlyPieces, enemyPieces,
                    move);
        }
    }

    /**
     * Returns true if the king is currently in check. NB stops as soon as at least one check is discovered.
     * <p>
     * Examines opponent's knights and pawns for checks, and all rays. This is therefore suitable for a game situation where
     * the king has just moved, or the king was in check beforehand.
     * <p>
     * For an optimized version use {@link #isKingInCheck(Square, Colour, BitSetUnifier, BitSetUnifier[], RayType)}, specifying the 5th
     * parameter.
     *
     * @param kingsSquare    where the king is.
     * @param kingsColour    colour of the king.
     * @param friendlyPieces bitset indicating location of the friendly pieces.
     * @param enemyPieces    bitsets indicating location of the enemy pieces.
     * @return true if the king is in check.
     */
    public static boolean isKingInCheck(Square kingsSquare,
                                        Colour kingsColour,
                                        BitSetUnifier friendlyPieces,
                                        BitSetUnifier[] enemyPieces) {
        return isKingInCheck(kingsSquare, kingsColour, friendlyPieces, enemyPieces, null);
    }

    /**
     * Returns true if the king is currently in check. NB stops as soon as at least one check is discovered.
     * <p>
     * Suitable for multithreaded use, since does not modify any global state. TODO: could be expanded to return which piece
     * is checking.
     *
     * @param kingsSquare    where the king is.
     * @param kingsColour    colour of the king.
     * @param friendlyPieces bitset indicating location of the friendly pieces.
     * @param enemyPieces    bitsets indicating location of the enemy pieces.
     * @param rayToExamine   if set, JUST this ray will be examined. This is an optimization where the king was not in check
     *                       beforehand. Then we only need to check the ray which has been vacated by the moving piece. <b>Do not
     *                       set</b> if the king himself has moved.
     * @return true if the king is in check.
     */
    public static boolean isKingInCheck(Square kingsSquare,
                                        Colour kingsColour,
                                        BitSetUnifier friendlyPieces,
                                        BitSetUnifier[] enemyPieces,
                                        RayType rayToExamine) {

        boolean optimizedRaySearch = rayToExamine != null;

        /*
         * The algorithm first handles the special cases of pawn or knight checks. Then, for each ray emenating from the king's
         * square, the squares on the ray get checked. If the square contains an enemy piece then this is checked for a possible
         * check.
         */

        if (!optimizedRaySearch) {
            // special cases: pawn and knight attacks
            if (Knight.attacksSquare(kingsSquare, enemyPieces[PieceType.KNIGHT.ordinal()])) {
                return true;
            }
            if (Pawn.attacksSquare(kingsSquare, Colour.oppositeColour(kingsColour), enemyPieces[PieceType.PAWN.ordinal()])) {
                return true;
            }
        }

        BitBoard allEnemyPieces = new BitBoard();
        for (PieceType pt : PieceType.ALL_PIECE_TYPES) {
            allEnemyPieces.getBitSet().or(enemyPieces[pt.ordinal()]);
        }

        RayType[] raysToCheck;
        if (optimizedRaySearch) {
            raysToCheck = new RayType[]{rayToExamine};
        } else {
            raysToCheck = RayType.values();
        }

        for (RayType rayType : raysToCheck) {
            Ray ray = BaseRay.getRay(rayType);
            Iterator<Integer> rayIter = ray.squaresFrom(kingsSquare);
            while (rayIter.hasNext()) {
                int bitIndex = rayIter.next();
                // stop at once if a friendly piece is on this ray
                if (friendlyPieces.get(bitIndex)) {
                    break;
                }
                // an enemy piece is relevant for diagonal (queen/bishop) or file (queen/rook)
                if (allEnemyPieces.get(bitIndex)) {
                    boolean queenPresentAtBitIndex = enemyPieces[PieceType.QUEEN.ordinal()].get(bitIndex);
                    if (rayType.isDiagonal() && (queenPresentAtBitIndex || enemyPieces[PieceType.BISHOP.ordinal()].get(bitIndex))) {
                        return true;
                    } else if (!rayType.isDiagonal() && (queenPresentAtBitIndex || enemyPieces[PieceType.ROOK.ordinal()].get(bitIndex))) {
                        return true;
                    } else {
                        break;
                    }
                }
            }

        }
        return false;
    }

    /**
     * Returns true if the king would be in check after <code>move</code>.
     * <p>
     * Helper-Method, delegates to {@link #isKingInCheckAfterMove(Square, Colour, BitSetUnifier, BitSetUnifier[], Move, boolean)} with last
     * parameter==true.
     * <p>
     * <b>Use this procedure if the king was already in check before the given move.</b>
     *
     * @param kingsSquare    where the king is. If <code>move</code> indicated that the king has moved, this value will be
     *                       ignored and the king's new square will be calculated.
     * @param kingsColour    colour of the king.
     * @param friendlyPieces bitset indicating location of the friendly pieces (pre-move).
     * @param enemyPieces    bitsets indicating location of the enemy pieces (pre-move).
     * @param move           the move to make
     * @return true if the king would be in check after the move.
     */
    public static boolean isKingInCheckAfterMove_PreviouslyWasInCheck(Square kingsSquare,
                                                                      Colour kingsColour,
                                                                      BitSetUnifier friendlyPieces,
                                                                      BitSetUnifier[] enemyPieces,
                                                                      Move move) {
        return isKingInCheckAfterMove(kingsSquare, kingsColour, friendlyPieces, enemyPieces, move, true);
    }

    /**
     * Returns true if the king would be in check after <code>move</code>.
     * <p>
     * Helper-Method, delegates to {@link #isKingInCheckAfterMove(Square, Colour, BitSetUnifier, BitSetUnifier[], Move, boolean)} with last
     * parameter==false.
     * <p>
     * <b>Use this procedure if the king was NOT in check before the given move.</b>
     *
     * @param kingsSquare    where the king is. If <code>move</code> indicated that the king has moved, this value will be
     *                       ignored and the king's new square will be calculated.
     * @param kingsColour    colour of the king.
     * @param friendlyPieces bitset indicating location of the friendly pieces (pre-move).
     * @param enemyPieces    bitsets indicating location of the enemy pieces (pre-move).
     * @param move           the move to make
     * @return true if the king would be in check after the move.
     */
    public static boolean isKingInCheckAfterMove_PreviouslyNotInCheck(Square kingsSquare,
                                                                      Colour kingsColour,
                                                                      BitSetUnifier friendlyPieces,
                                                                      BitSetUnifier[] enemyPieces,
                                                                      Move move) {
        return isKingInCheckAfterMove(kingsSquare, kingsColour, friendlyPieces, enemyPieces, move, false);
    }

    /**
     * Returns true if the king would be in check after <code>move</code>. This method can be called directly, but there are
     * helper methods available (see isKingInCheckAfterMove_PreviouslyNotInCheck and
     * isKingInCheckAfterMove_PreviouslyWasInCheck).
     * <p>
     * <b>If the king is already in check before <code>move</code>, you must set the parameter <code>kingWasInCheck</code>
     * to true.</b>
     * <p>
     * An optimised search is used. Assuming the king himself has not moved, and the king was not previously in check, only
     * the ray given by the king's square and the vacated square needs to be examined.
     *
     * @param kingsSquare    where the king is. If <code>move</code> indicated that the king has moved, this value will be
     *                       ignored and the new king's square will be calculated.
     * @param kingsColour    colour of the king.
     * @param friendlyPieces bitset indicating location of the friendly pieces (pre-move).
     * @param enemyPieces    bitsets indicating location of the enemy pieces (pre-move).
     * @param move           the move to make
     * @param kingWasInCheck indicates that the king was in check before this move. Therefore, cannot use the optimized ray
     *                       search.
     * @return true if the king would be in check after the move.
     */
    public static boolean isKingInCheckAfterMove(Square kingsSquare,
                                                 Colour kingsColour,
                                                 BitSetUnifier friendlyPieces,
                                                 BitSetUnifier[] enemyPieces,
                                                 Move move,
                                                 boolean kingWasInCheck) {

        boolean kingMoved = false;
        Ray rayFromKingToMoveOrigin = null;

        // update 'kingsSquare' if king has moved
        if (move.getPiece() == PieceType.KING) {
            kingsSquare = move.to();
            kingMoved = true;
        }
        if (!(kingMoved || kingWasInCheck)) {
            // can optimize by only searching the ray given by the direction kingsSquare -> move.from()
            rayFromKingToMoveOrigin = RayUtils.getRay(kingsSquare, move.from());
            if (rayFromKingToMoveOrigin == null) {
                return false;
            }
        }

        friendlyPieces = (BitSetUnifier) friendlyPieces.clone();
        friendlyPieces.set(move.to().bitIndex());
        friendlyPieces.clear(move.from().bitIndex());

        // may not be strictly necessary, but is consistent
        if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
            friendlyPieces.set(move.getRooksCastlingMove().to().bitIndex());
            friendlyPieces.clear(move.getRooksCastlingMove().from().bitIndex());
        }

        if (move.isCapture()) {
            // need to modify BitSet for the opponent's captured piece,
            // therefore clone and resave in 'enemyPieces'
            BitSetUnifier opponentsCapturedPiece = (BitSetUnifier) enemyPieces[move.getCapturedPiece().ordinal()].clone();
            enemyPieces[move.getCapturedPiece().ordinal()] = opponentsCapturedPiece;
            // .. and remove captured piece
            Square capturedPieceSquare = move.to();
            if (move.isEnpassant()) {
                capturedPieceSquare = Square.findMoveFromEnpassantSquare(move.to());
            }
            opponentsCapturedPiece.clear(capturedPieceSquare.bitIndex());
        }

        // no optimizations if the king moved or was in check beforehand
        if (kingMoved || kingWasInCheck) {
            return isKingInCheck(kingsSquare, kingsColour, friendlyPieces, enemyPieces);
        } else {
            return isKingInCheck(kingsSquare, kingsColour, friendlyPieces, enemyPieces, rayFromKingToMoveOrigin.getRayType());
        }
    }

}
