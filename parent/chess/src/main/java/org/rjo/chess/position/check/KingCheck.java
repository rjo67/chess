package org.rjo.chess.position.check;

import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitSetUnifier;
import org.rjo.chess.base.ray.Ray;
import org.rjo.chess.base.ray.RayUtils;
import org.rjo.chess.position.Position;
import org.rjo.chess.position.PositionAnalyser;

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
		enemyPieces = Position.setupBitsets(posn.getPieces(opponentsColour));
		this.myKing = myKing;
	}

	/**
	 * Checks if my king is in check after <code>move</code>. Uses the position supplied in the constructor.
	 *
	 * @param move the move
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
	 * @param posn the chessboard
	 * @param move the move
	 * @param opponentsColour this colour's pieces will be inspected to see if they check my king
	 * @param king where my king is
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
		BitSetUnifier[] enemyPieces = Position.setupBitsets(posn.getPieces(opponentsColour));

		if (kingIsAlreadyInCheck) {
			return isKingInCheckAfterMove_PreviouslyWasInCheck(king, Colour.oppositeColour(opponentsColour), friendlyPieces, enemyPieces,
					move);
		} else {
			return isKingInCheckAfterMove_PreviouslyNotInCheck(king, Colour.oppositeColour(opponentsColour), friendlyPieces, enemyPieces,
					move);
		}
	}

	/**
	 * Returns true if the king would be in check after <code>move</code>.
	 * <p>
	 * Helper-Method, delegates to
	 * {@link #isKingInCheckAfterMove(Square, Colour, BitSetUnifier, BitSetUnifier[], Move, boolean)} with last
	 * parameter==true.
	 * <p>
	 * <b>Use this procedure if the king was already in check before the given move.</b>
	 *
	 * @param kingsSquare where the king is. If <code>move</code> indicated that the king has moved, this value will be
	 *           ignored and the king's new square will be calculated.
	 * @param kingsColour colour of the king.
	 * @param friendlyPieces bitset indicating location of the friendly pieces (pre-move).
	 * @param enemyPieces bitsets indicating location of the enemy pieces (pre-move).
	 * @param move the move to make
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
	 * Helper-Method, delegates to
	 * {@link #isKingInCheckAfterMove(Square, Colour, BitSetUnifier, BitSetUnifier[], Move, boolean)} with last
	 * parameter==false.
	 * <p>
	 * <b>Use this procedure if the king was NOT in check before the given move.</b>
	 *
	 * @param kingsSquare where the king is. If <code>move</code> indicated that the king has moved, this value will be
	 *           ignored and the king's new square will be calculated.
	 * @param kingsColour colour of the king.
	 * @param friendlyPieces bitset indicating location of the friendly pieces (pre-move).
	 * @param enemyPieces bitsets indicating location of the enemy pieces (pre-move).
	 * @param move the move to make
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
	 * @param kingsSquare where the king is. If <code>move</code> indicated that the king has moved, this value will be
	 *           ignored and the new king's square will be calculated.
	 * @param kingsColour colour of the king.
	 * @param friendlyPieces bitset indicating location of the friendly pieces (pre-move).
	 * @param enemyPieces bitsets indicating location of the enemy pieces (pre-move).
	 * @param move the move to make
	 * @param kingWasInCheck indicates that the king was in check before this move. Therefore, cannot use the optimized ray
	 *           search.
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
			return !PositionAnalyser.analysePosition(kingsSquare, kingsColour, friendlyPieces, null, enemyPieces, null, false).getCheckers()
					.isEmpty();
		} else {
			return !PositionAnalyser
					.analysePosition(kingsSquare, kingsColour, friendlyPieces, null, enemyPieces, rayFromKingToMoveOrigin.getRayType(), false)
					.getCheckers().isEmpty();
		}
	}
}
