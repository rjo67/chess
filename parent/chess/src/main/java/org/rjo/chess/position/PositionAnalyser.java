package org.rjo.chess.position;

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
import org.rjo.chess.pieces.PieceManager.Pieces;

/**
 * Routines to discover if the king is in check.
 * <p>
 * Usage: construct the object with a position and then call {@link #isKingInCheck(Move, boolean)}. <br>
 * There are also various static methods which can be called directly.
 *
 * @author rich
 */
public class PositionAnalyser {

	private PositionAnalyser() {

	}

	/**
	 * Analyses the given position, returning e.g. a list of pieces which currently check the given king.
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
	 * @param friendlyPieces all the friendly pieces. FOR BACKWARDS COMPAT: can be null. In this case a list of pinned
	 *           pieces will not be created.
	 * @param enemyPieces all the enemy pieces.
	 * @param rayToExamine if set, JUST this ray will be examined. This is an optimization where the king was not in check
	 *           beforehand. Then we only need to check the ray which has been vacated by the moving piece. <b>Do not
	 *           set</b> if the king himself has moved.
	 * @param findAllChecks if true, all checking pieces will be found. If false, search stops after the first one.
	 * @return an object containing a list of pieces checking the king (list is empty if the king is not in check) and a
	 *         list of pieces which are pinned against the king. If friendlyPieces==null, pinned info will not be returned
	 */
	public static PositionInfo analysePosition(Square kingsSquare,
			Colour kingsColour,
			BitSetUnifier allFriendlyPieces,
			Pieces friendlyPieces,
			Pieces enemyPieces,
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
			var p = enemyPieces.stream(PieceType.KNIGHT).filter(kn -> kn.attacksSquare(null, kingsSquare)).findAny();
			if (p.isPresent()) {
				boardInfo.addChecker(PieceType.KNIGHT, p.get().getLocation().bitIndex());
				if (!findAllChecks || boardInfo.getCheckers().size() == 2) {
					return boardInfo;
				}
			}
			if (enemyPieces.getPawns().attacksSquare(null, kingsSquare)) {
				boardInfo.addChecker(PieceType.PAWN, -1 /* TODO */);
				if (!findAllChecks || boardInfo.getCheckers().size() == 2) {
					return boardInfo;
				}
			}
		}

		BitBoard allEnemyPieces = enemyPieces.createBitBoard();

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
						var pieceOnSquare = enemyPieces.stream(PieceType.QUEEN, PieceType.BISHOP)
								.filter(piece -> piece.pieceAt(Square.fromBitIndex(bitIndex))).findAny();
						if (pieceOnSquare.isPresent()) {
							boardInfo.addChecker(rayType, pieceOnSquare.get().getType(), bitIndex);
							keepSearching = !findAllChecks || boardInfo.isDoubleCheck();
						}
					} else // !rayType.isDiagonal()
					{
						var pieceOnSquare = enemyPieces.stream(PieceType.QUEEN, PieceType.ROOK)
								.filter(piece -> piece.pieceAt(Square.fromBitIndex(bitIndex))).findAny();
						if (pieceOnSquare.isPresent()) {
							boardInfo.addChecker(rayType, pieceOnSquare.get().getType(), bitIndex);
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
	 * Analyses the given position wrt the king's position and returns bitsets of squares, which if occupied by a piece
	 * would leave the king in check.
	 * <p>
	 * NB if a friendly piece is found first on a ray, then its square *is* included in the output, since an enemy piece
	 * could capture this piece.
	 *
	 * @param kingsSquare location of the king
	 * @param allFriendlyPieces bitset of all the king's pieces
	 * @param allEnemyPieces bitset of all enemy pieces
	 * @return bitsets of check-relevant squares. First bitset is for rooks/queens, second for bishops/queens.
	 */
	public static BitBoard[] findCheckingSquares(Square kingsSquare,
			BitBoard allFriendlyPieces,
			BitBoard allEnemyPieces) {

		var raysToCheck = RayType.values();

		var rookBitSet = new BitBoard();
		var bishopBitSet = new BitBoard();

		// look along each ray, starting from king's square
		for (RayType rayType : raysToCheck) {
			// decide which output bitset to use
			BitBoard output;
			if (rayType.isDiagonal()) {
				output = bishopBitSet;
			} else {
				output = rookBitSet;
			}
			Ray ray = RayUtils.getRay(rayType);
			Iterator<Integer> rayIter = ray.squaresFrom(kingsSquare);
			while (rayIter.hasNext()) {
				int bitIndex = rayIter.next();
				// stop at first enemy piece ...
				// ... or first friendly piece (its square will be included though)
				if (allEnemyPieces.get(bitIndex)) {
					break;
				}
				output.set(bitIndex);
				if (allFriendlyPieces.get(bitIndex)) {
					break;
				}
			}
		}
		return new BitBoard[] { rookBitSet, bishopBitSet };
	}

	/**
	 * Returns the type of piece at the bitindex location. TODO call Pieces.findPieceAt directly, and store the piece rather
	 * than the piece type
	 *
	 * @param bitIndex location
	 * @param pieces bitsets of pieces
	 * @return the type of piece at the location
	 */
	private static PieceType findPieceAt(int bitIndex,
			Pieces pieces) {
		return pieces.findPieceAt(Square.fromBitIndex(bitIndex)).getType();
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
			Pieces enemyPieces,
			RayType rayType,
			Iterator<Integer> rayIter) {
		while (rayIter.hasNext()) {
			int bitIndex = rayIter.next();
			// stop search if we've found another of our pieces on the ray
			if (friendlyPieces.get(bitIndex)) {
				return false;
			} else if (allEnemyPieces.get(bitIndex)) {
				if (rayType.isDiagonal()) {
					var pieceOnSquare = enemyPieces.stream(PieceType.QUEEN, PieceType.BISHOP)
							.filter(piece -> piece.pieceAt(Square.fromBitIndex(bitIndex))).findAny();
					if (pieceOnSquare.isPresent()) {
						return true;
					}
					break; // found an enemy's piece, but it's not a pinner
				} else // !rayType.isDiagonal
				{
					var pieceOnSquare = enemyPieces.stream(PieceType.QUEEN, PieceType.ROOK)
							.filter(piece -> piece.pieceAt(Square.fromBitIndex(bitIndex))).findAny();
					if (pieceOnSquare.isPresent()) {
						return true;
					}
					break; // found an enemy's piece, but it's not a pinner
				}
			}
		}
		return false;
	}

}
