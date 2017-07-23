package org.rjo.chess.ray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.rjo.chess.pieces.PieceType;

@SuppressWarnings("unchecked")
public class NorthRay extends BaseRay {

	private static NorthRay instance = new NorthRay();
	private static final List<Integer>[] RAY_SQUARES;

	static {
		final int offset = 8;
		RAY_SQUARES = new List[64];
		for (int i = 0; i < 64; i++) {
			RAY_SQUARES[i] = new ArrayList<>(8);
			int startSquareIndex = i + offset;
			while (startSquareIndex < 64) {
				RAY_SQUARES[i].add(startSquareIndex);
				startSquareIndex += offset;
			}
		}
	}

	private NorthRay() {
		super(RayType.NORTH, false, new PieceType[] { PieceType.QUEEN, PieceType.ROOK });
	}

	public static NorthRay instance() {
		return instance;
	}

	@Override
	public Iterator<Integer> squaresFrom(int startSquareIndex) {
		return RAY_SQUARES[startSquareIndex].iterator();
	}

	@Override
	public final boolean oppositeOf(Ray ray) {
		return ray.getRayType() == RayType.SOUTH;
	}
}
