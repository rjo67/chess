package org.rjo.chess.ray;

import java.util.ArrayList;

import org.rjo.chess.pieces.PieceType;

public class SouthEastRay extends BaseRay {

	private static SouthEastRay instance = new SouthEastRay();

	private SouthEastRay() {
		super(RayType.SOUTHEAST, true, new PieceType[] { PieceType.QUEEN, PieceType.BISHOP });
		final int offset = -7;
		for (int i = 0; i < 64; i++) {
			raySquares[i] = new ArrayList<>(8);
			int startSquareIndex = i + offset;
			while ((startSquareIndex >= 0) && (startSquareIndex % 8 != 0)) {
				raySquares[i].add(startSquareIndex);
				startSquareIndex += offset;
			}
		}
	}

	public static SouthEastRay instance() {
		return instance;
	}

	@Override
	public final boolean oppositeOf(Ray ray) {
		return ray.getRayType() == RayType.NORTHWEST;
	}

	@Override
	public Ray getOpposite() {
		return NorthWestRay.instance();
	}
}
