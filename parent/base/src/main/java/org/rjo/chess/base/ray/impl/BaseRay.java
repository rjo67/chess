package org.rjo.chess.base.ray.impl;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.ray.Ray;
import org.rjo.chess.base.ray.RayType;
import org.rjo.chess.base.ray.RayUtils;

public abstract class BaseRay implements Ray {

	private final RayType rayType;

	/**
	 * For each start square sq1, a list of bitset indices indicating the squares on this ray starting at sq1 (not including
	 * sq1).
	 */
	@SuppressWarnings("unchecked")
	protected final List<Integer>[] raySquares = new List[64];

	/**
	 * Contains a bitboard for each square sq1, which represents the squares attacked from sq1 along this ray (not including
	 * sq1).
	 */
	protected final BitBoard[] attackBitBoard = new BitBoard[64];

	/** which pieces can give a check along this ray */
	protected final PieceType[] piecesThatCanGiveCheckOnThisRay;

	/**
	 * Constructor.
	 *
	 * @param rayType type of ray
	 * @param piecesThatCanGiveCheckOnThisRay which pieces can check on this ray
	 */
	protected BaseRay(RayType rayType, PieceType[] piecesThatCanGiveCheckOnThisRay) {
		this.rayType = rayType;
		this.piecesThatCanGiveCheckOnThisRay = piecesThatCanGiveCheckOnThisRay;
	}

	@Override
	public final RayType getRayType() {
		return rayType;
	}

	@Override
	public final Iterator<Integer> squaresFrom(Square startSquare) {
		return squaresFrom(startSquare.bitIndex());
	}

	@Override
	public Iterator<Integer> squaresFrom(int startSquareIndex) {
		return raySquares[startSquareIndex].iterator();
	}

	@Override
	public final Stream<Integer> streamSquaresFrom(Square startSquare) {
		return streamSquaresFrom(startSquare.bitIndex());
	}

	@Override
	public Stream<Integer> streamSquaresFrom(int startSquareIndex) {
		return raySquares[startSquareIndex].stream();
	}

	@Override
	public boolean isRelevantPieceForDiscoveredCheck(PieceType piece) {
		for (PieceType pt : piecesThatCanGiveCheckOnThisRay) {
			if (pt == piece) {
				return true;
			}
		}
		return false;
	}

	@Override
	public BitBoard getAttackBitBoard(int sqIndex) {
		return attackBitBoard[sqIndex];
	}

	@Override
	public String toString() {
		return rayType.getAbbreviation();
	}

	@Override
	public final boolean oppositeOf(Ray ray) {
		return this.getRayType().getOpposite() == ray.getRayType();
	}

	@Override
	public final Ray getOpposite() {
		return RayUtils.getRay(this.getRayType().getOpposite());
	}
}
