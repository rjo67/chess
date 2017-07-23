package org.rjo.chess.ray;

import java.util.BitSet;
import java.util.Iterator;

import org.rjo.chess.Colour;
import org.rjo.chess.Position;
import org.rjo.chess.Square;
import org.rjo.chess.pieces.PieceType;

public class RayUtils {
	// lookup table for each square sq1, storing the ray for every other square relative to sq1
	private final static Ray[][] RAYS_BETWEEN_SQUARES = new Ray[64][64];

	// set up static lookups
	static {
		for (int sq1 = 0; sq1 < 64; sq1++) {
			for (int sq2 = 0; sq2 < 64; sq2++) {
				if (sq1 == sq2) {
					continue;
				}
				boolean found = false;
				for (RayType rayType : RayType.values()) {
					if (found) {
						break;
					}
					Iterator<Integer> iter = BaseRay.getRay(rayType).squaresFrom(sq1);
					while (iter.hasNext() && !found) {
						if (sq2 == iter.next()) {
							found = true;
							RAYS_BETWEEN_SQUARES[sq1][sq2] = BaseRay.getRay(rayType);
						}
					}
				}
			}
		}
	}

	private RayUtils() {

	}

	/**
	 * Finds a discovered check on the opponent's king after a move from square <code>moveFromSquare</code>.
	 *
	 * @param myColour my colour
	 * @param cb the chessboard -- required for pieceAt()
	 * @param emptySquares the empty squares
	 * @param myPieces BitSet of my pieces
	 * @param opponentsKingsSquare where the opponent's king is
	 * @param moveFromSquare the square where the piece moved from
	 * @return true if the move from square <code>moveFromSquare</code> leads to a discovered check on the king.
	 */
	public static boolean discoveredCheck(Colour myColour,
			Position cb,
			BitSet emptySquares,
			BitSet myPieces,
			Square opponentsKingsSquare,
			Square moveFromSquare) {
		// if moveFromSquare is on a ray to kingsSquare,
		// then inspect this ray for a checking bishop/queen/rook
		Ray ray = RayUtils.getRay(opponentsKingsSquare, moveFromSquare);
		if (ray != null) {
			// TODO optimization: only interested in my pieces here
			RayInfo info = RayUtils.findFirstPieceOnRay(myColour, emptySquares, myPieces, ray, opponentsKingsSquare.bitIndex());
			if (info.foundPiece() && (info.getColour() == myColour)) {
				PieceType firstPieceFound = cb.pieceAt(Square.fromBitIndex(info.getIndexOfPiece()), myColour);
				if (ray.isRelevantPieceForDiscoveredCheck(firstPieceFound)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Finds a discovered check on the opponent's king after a move from square <code>moveFromSquare</code>.
	 *
	 * @param myColour my colour
	 * @param cb the chessboard -- required for pieceAt()
	 * @param emptySquares the empty squares
	 * @param myPieces BitSet of my pieces
	 * @param opponentsKingsSquare where the opponent's king is
	 * @param moveFromSquare the square where the piece moved from
	 * @return true if the move from square <code>moveFromSquare</code> leads to a discovered check on the king.
	 */
	public static boolean kingInCheck(Colour kingsColour,
			Position cb,
			BitSet emptySquares,
			BitSet kingsColourPieces,
			Square kingsSquare,
			Square moveFromSquare) {
		// if moveFromSquare is on a ray to kingsSquare,
		// then inspect this ray for a checking bishop/queen/rook
		Ray ray = RayUtils.getRay(kingsSquare, moveFromSquare);
		if (ray != null) {
			RayInfo info = RayUtils.findFirstPieceOnRay(kingsColour, emptySquares, kingsColourPieces, ray, kingsSquare.bitIndex());
			if (info.foundPiece() && (info.getColour() != kingsColour)) {
				PieceType firstPieceFound = cb.pieceAt(Square.fromBitIndex(info.getIndexOfPiece()), Colour.oppositeColour(kingsColour));
				if (ray.isRelevantPieceForDiscoveredCheck(firstPieceFound)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Inspects the squares returned by the ray's iterator. A square with a piece on it gets recorded and the routine
	 * returns immediately. Otherwise, the empty square is recorded and the search repeats for the next value.
	 * <p>
	 * At the end, the information returned has the empty squares (if any) and the first piece found (if any) on the ray.
	 * The piece type is not recorded here since this would require an extra lookup. Instead, the square and the colour of
	 * the piece are recorded.
	 * <p>
	 * Special case: if called with emptySquares=BitSet(-1) and myPieces=opponentsPieces=BitSet(0), then this routine will
	 * return all squares for this ray.
	 * <p>
	 * It is assumed that if the square is not empty and does not contain a piece of my colour, it contains an opponent's
	 * piece.
	 *
	 * @param myColour my colour.
	 * @param emptySquares bitset of empty squares.
	 * @param myPieces bitset of my pieces.
	 * @param ray ray
	 * @param startSquare index of start square
	 * @return information about the first piece found on this ray and/or empty squares on the ray, starting from
	 *         startSquare.
	 */
	public static RayInfo findFirstPieceOnRay(Colour myColour,
			BitSet emptySquares,
			BitSet myPieces,
			Ray ray,
			int startSquare) {
		RayInfo info = new RayInfo();
		int distance = 0;
		Iterator<Integer> rayIter = ray.squaresFrom(startSquare);
		while (rayIter.hasNext()) {
			int sqIndex = rayIter.next();
			distance++;
			// empty square?
			if (emptySquares.get(sqIndex)) {
				info.addEmptySquare(sqIndex);
			}
			// my piece?
			else if (myPieces.get(sqIndex)) {
				info.storePiece(sqIndex, myColour, distance);
				break;
			}
			// assumed to be opponent's piece...
			else {
				info.storePiece(sqIndex, Colour.oppositeColour(myColour), distance);
				break;
			}
		}
		return info;
	}

	/**
	 * Returns the appropriate Ray joining sq1 to sq2, or null.
	 *
	 * @param sq1 first square
	 * @param sq2 second square
	 * @return the Ray object joining the two squares, or null if not on a ray.
	 */
	public static Ray getRay(Square sq1,
			Square sq2) {
		return RAYS_BETWEEN_SQUARES[sq1.bitIndex()][sq2.bitIndex()];
	}

}
