package org.rjo.chess.base.ray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.bits.BitSetUnifier;

public class RayUtils {
	// lookup table for each square sq1, storing the ray for every other square relative to sq1
	private final static Ray[][] RAYS_BETWEEN_SQUARES = new Ray[64][64];
	// lookup table for each square sq1, storing the DIAGONAL ray (or null) for every other square relative to sq1
	private final static Ray[][] DIAGONAL_RAYS_BETWEEN_SQUARES = new Ray[64][64];
	// lookup table for each square sq1, storing the HORIZONTAL/VERTICAL ray (or null) for every other square relative to sq1
	private final static Ray[][] ORTHOGONAL_RAYS_BETWEEN_SQUARES = new Ray[64][64];

	@SuppressWarnings("unchecked")
	// stores for each starting square sq1, a list of the squares in between to get to sq2 (sq1, sq2 not included)
	//TODO sq2 should be included?
	// can be null: no ray between the two squares, or empty list.
	// SQUARES_ON_RAY[a1][c3] delivers the squares between a1 and c3 -- b2 and c3 in this case.
	private final static List<Integer>[][] SQUARES_ON_RAY = new List[64][64];

	// this is the same info as SQUARES_ON_RAY, but stored as a bitset
	private final static BitSetUnifier[][] BITSET_SQUARES_ON_RAY = new BitSetUnifier[64][64];

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
					List<Integer> squaresFound = new ArrayList<>();
					while (iter.hasNext() && !found) {
						Integer square = iter.next();
						if (sq2 == square) {
							found = true;
							Ray ray = BaseRay.getRay(rayType);
							RAYS_BETWEEN_SQUARES[sq1][sq2] = ray;
							if (ray.getRayType().isDiagonal()) {
								DIAGONAL_RAYS_BETWEEN_SQUARES[sq1][sq2] = ray;
							} else {
								ORTHOGONAL_RAYS_BETWEEN_SQUARES[sq1][sq2] = ray;
							}
						} else {
							squaresFound.add(square);
						}
					}
					if (found) {
						SQUARES_ON_RAY[sq1][sq2] = Collections.unmodifiableList(squaresFound);
						// System.out.println(sq1 + " -> " + sq2 + ": " + SQUARES_ON_RAY[sq1][sq2]);
						BitBoard bb = new BitBoard();
						squaresFound.forEach(sq -> bb.setBitsAt(Square.fromBitIndex(sq)));
						BITSET_SQUARES_ON_RAY[sq1][sq2] = bb.getBitSet();
					}
				}
			}
		}
	}

	private RayUtils() {

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
			BitSetUnifier emptySquares,
			BitSetUnifier myPieces,
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

	public static Ray getDiagonalRay(Square sq1,
			Square sq2) {
		return DIAGONAL_RAYS_BETWEEN_SQUARES[sq1.bitIndex()][sq2.bitIndex()];
	}

	public static Ray getOrthogonalRay(Square sq1,
			Square sq2) {
		return ORTHOGONAL_RAYS_BETWEEN_SQUARES[sq1.bitIndex()][sq2.bitIndex()];
	}

	public static BitSetUnifier getBitSetOfSquaresBetween(Square sq1,
			Square sq2) {
		return BITSET_SQUARES_ON_RAY[sq1.bitIndex()][sq2.bitIndex()];
	}

	public static BitSetUnifier getBitSetOfSquaresBetween(int sq1,
			int sq2) {
		return BITSET_SQUARES_ON_RAY[sq1][sq2];
	}
}
