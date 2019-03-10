package org.rjo.chess.position.check;

import java.util.Iterator;

import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.bits.BitSetUnifier;
import org.rjo.chess.base.ray.Ray;
import org.rjo.chess.base.ray.RayType;
import org.rjo.chess.base.ray.RayUtils;
import org.rjo.chess.pieces.Knight;
import org.rjo.chess.pieces.Pawn;
import org.rjo.chess.position.Position;
import org.rjo.chess.position.PositionInfo;

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
	 * Returns a list of pieces which currently check the given king.
	 * <p>
	 * The search can be configured in two ways:
	 * <ul>
	 * <li>if <code>findAllChecks</code> is 'false', the routine will exit as soon as it finds one checking piece. Otherwise
	 * all checking pieces will be found (stopping at max 2, since more is not possible in a game situation.</li>
	 * <li>if <code>rayToExamine</code> is set, only this ray will be examined (and no pawn/knight moves either).</li>
	 * </ul>
	 * <p>
	 * Suitable for multithreaded use, since does not modify any global state.
	 *
	 * @param kingsSquare where the king is.
	 * @param kingsColour colour of the king.
	 * @param allFriendlyPieces bitset indicating location of the friendly pieces.
	 * @param friendlyPieces bitsets indicating location of the friendly pieces. FOR BACKWARDS COMPAT: can be null.
	 * @param enemyPieces bitsets indicating location of the enemy pieces.
	 * @param rayToExamine if set, JUST this ray will be examined. This is an optimization where the king was not in check
	 *           beforehand. Then we only need to check the ray which has been vacated by the moving piece. <b>Do not
	 *           set</b> if the king himself has moved.
	 * @param findAllChecks if true, all checking pieces will be found. If false, search stops after the first one.
	 * @return an object containing a list of pieces checking the king (list is empty if the king is not in check) and a
	 *         list of pieces which are pinned against the king. If friendlyPieces==null, pinned info will not be returned
	 */
	public static PositionInfo isKingInCheck(Square kingsSquare,
			Colour kingsColour,
			BitSetUnifier allFriendlyPieces,
			BitSetUnifier[] friendlyPieces,
			BitSetUnifier[] enemyPieces,
			RayType rayToExamine,
			boolean findAllChecks) {

		var boardInfo = new PositionInfo(kingsSquare);
		boolean optimizedRaySearch = rayToExamine != null;

		/*
		 * The algorithm first handles the special cases of pawn or knight checks. Then, for each ray emenating from the king's
		 * square, the squares on the ray get checked. If the square contains an enemy piece then this is checked for a possible
		 * check.
		 */

		if (!optimizedRaySearch) {
			// special cases: pawn and knight attacks
			var i = Knight.attacksSquare(kingsSquare, enemyPieces[PieceType.KNIGHT.ordinal()]);
			if (i >= 0) {
				boardInfo.addChecker(PieceType.KNIGHT, i);
				if (!findAllChecks || boardInfo.getCheckers().size() == 2) {
					return boardInfo;
				}
			}
			i = Pawn.attacksSquare(kingsSquare, Colour.oppositeColour(kingsColour), enemyPieces[PieceType.PAWN.ordinal()]);
			if (i >= 0) {
				boardInfo.addChecker(PieceType.PAWN, i);
				if (!findAllChecks || boardInfo.getCheckers().size() == 2) {
					return boardInfo;
				}
			}
		}

		BitBoard allEnemyPieces = new BitBoard();
		for (PieceType pt : PieceType.ALL_PIECE_TYPES) {
			allEnemyPieces.getBitSet().or(enemyPieces[pt.ordinal()]);
		}

		RayType[] raysToCheck;
		if (optimizedRaySearch) {
			raysToCheck = new RayType[] { rayToExamine };
		} else {
			raysToCheck = RayType.values();
		}

		// look along each ray, starting from king's square
		for (RayType rayType : raysToCheck) {
			Ray ray = RayUtils.getRay(rayType);
			boolean keepSearching = true; // we terminate early if we've found 2 checks
			Iterator<Integer> rayIter = ray.squaresFrom(kingsSquare);
			while (keepSearching && rayIter.hasNext()) {
				int bitIndex = rayIter.next();
				// stop search for a checker if a friendly piece is on this ray; however, process to see if this is pinned (if friendlyPieces has been supplied)
				if (allFriendlyPieces.get(bitIndex)) {
					if (friendlyPieces != null && pieceIsPinned(allFriendlyPieces, allEnemyPieces, enemyPieces, rayType, rayIter)) {
						boardInfo.addPinnedPiece(rayType, findPieceAt(bitIndex, friendlyPieces), bitIndex);
					}
					break;
				}
				// an enemy piece is relevant for diagonal (queen/bishop) or file (queen/rook)
				if (allEnemyPieces.get(bitIndex)) {
					if (rayType.isDiagonal()) {
						if (enemyPieces[PieceType.QUEEN.ordinal()].get(bitIndex)) {
							boardInfo.addChecker(rayType, PieceType.QUEEN, bitIndex);
							keepSearching = !findAllChecks || boardInfo.isDoubleCheck();
						} else if (enemyPieces[PieceType.BISHOP.ordinal()].get(bitIndex)) {
							boardInfo.addChecker(rayType, PieceType.BISHOP, bitIndex);
							keepSearching = !findAllChecks || boardInfo.isDoubleCheck();
						}
					} else // !rayType.isDiagonal()
					{
						if (enemyPieces[PieceType.QUEEN.ordinal()].get(bitIndex)) {
							boardInfo.addChecker(rayType, PieceType.QUEEN, bitIndex);
							keepSearching = !findAllChecks || boardInfo.isDoubleCheck();
						} else if (enemyPieces[PieceType.ROOK.ordinal()].get(bitIndex)) {
							boardInfo.addChecker(rayType, PieceType.ROOK, bitIndex);
							keepSearching = !findAllChecks || boardInfo.isDoubleCheck();
						}
					}
					break;
				}
			}

		}
		boardInfo.calculateRestrictedSquares();
		return boardInfo;

	}

	/**
	 * Returns the type of piece at the bitindex location.
	 *
	 * @param bitIndex location
	 * @param pieces bitsets of pieces
	 * @return the type of piece at the location
	 */
	private static PieceType findPieceAt(int bitIndex,
			BitSetUnifier[] pieces) {
		for (PieceType pt : PieceType.ALL_PIECE_TYPES_EXCEPT_KING) {
			if (pieces[pt.ordinal()].get(bitIndex)) {
				return pt;
			}
		}
		throw new IllegalArgumentException(
				String.format("no piece found at %d_(%s) for bitmaps: %s", bitIndex, Square.fromBitIndex(bitIndex), pieces));
	}

	/**
	 * This method is called when one of our pieces has been found on a ray from our king. It now checks to see if there is
	 * an enemy piece further along the ray which is pinning our piece. If so, returns true.
	 * <p>
	 * NB there is no need to know which of our pieces has been found or even on what square.
	 *
	 * @param friendlyPieces our pieces
	 * @param allEnemyPieces their pieces as a bitboard
	 * @param enemyPieces their pieces
	 * @param rayType type of ray
	 * @param rayIter the iterator along the ray in question
	 * @return true if there is an enemy piece pinning our piece.
	 */
	private static boolean pieceIsPinned(BitSetUnifier friendlyPieces,
			BitBoard allEnemyPieces,
			BitSetUnifier[] enemyPieces,
			RayType rayType,
			Iterator<Integer> rayIter) {
		while (rayIter.hasNext()) {
			int bitIndex = rayIter.next();
			// stop search if we've found another of our pieces on the ray
			if (friendlyPieces.get(bitIndex)) {
				return false;
			} else if (allEnemyPieces.get(bitIndex)) {
				if (rayType.isDiagonal()) {
					if (enemyPieces[PieceType.QUEEN.ordinal()].get(bitIndex)) {
						return true;
					} else if (enemyPieces[PieceType.BISHOP.ordinal()].get(bitIndex)) {
						return true;
					}
				} else // !rayType.isDiagonal
				{
					if (enemyPieces[PieceType.QUEEN.ordinal()].get(bitIndex)) {
						return true;
					} else if (enemyPieces[PieceType.ROOK.ordinal()].get(bitIndex)) {
						return true;
					}
				}
			}
		}
		return false;
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
			return !isKingInCheck(kingsSquare, kingsColour, friendlyPieces, null, enemyPieces, null, false).getCheckers().isEmpty();
		} else {
			return !isKingInCheck(kingsSquare, kingsColour, friendlyPieces, null, enemyPieces, rayFromKingToMoveOrigin.getRayType(), false)
					.getCheckers().isEmpty();
		}
	}
}
