package org.rjo.chess.ray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.rjo.chess.pieces.PieceType;

public class NorthWestRay extends BaseRay {

	private static NorthWestRay instance = new NorthWestRay();
	private static final List<Integer>[] RAY_SQUARES;

	static {
		final int offset = 7;
		RAY_SQUARES = new List[64];
		for (int i = 0; i < 64; i++) {
			RAY_SQUARES[i] = new ArrayList<>(8);
			int startSquareIndex = i + offset;
			while ((startSquareIndex < 64) && (startSquareIndex % 8 != 7)) {
				RAY_SQUARES[i].add(startSquareIndex);
				startSquareIndex += offset;
			}
		}
	}

	private NorthWestRay() {
		super(RayType.NORTHWEST, true, new PieceType[] { PieceType.QUEEN, PieceType.BISHOP });
	}

	public static NorthWestRay instance() {
		return instance;
	}

	@Override
	public Iterator<Integer> squaresFrom(int startSquareIndex) {
		return RAY_SQUARES[startSquareIndex].iterator();
	}

	@Override
	public final boolean oppositeOf(Ray ray) {
		return ray.getRayType() == RayType.SOUTHEAST;
	}
}
