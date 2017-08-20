package org.rjo.chess.ray;

import java.util.ArrayList;

import org.rjo.chess.pieces.PieceType;

public class SouthWestRay extends BaseRay {

	private static SouthWestRay instance = new SouthWestRay();

	private SouthWestRay() {
		super(RayType.SOUTHWEST, new PieceType[] { PieceType.QUEEN, PieceType.BISHOP });
		final int offset = -9;
		for (int i = 0; i < 64; i++) {
			raySquares[i] = new ArrayList<>(8);
			int startSquareIndex = i + offset;
			while ((startSquareIndex >= 0) && (startSquareIndex % 8 != 7)) {
				raySquares[i].add(startSquareIndex);
				startSquareIndex += offset;
			}
		}
	}

	public static SouthWestRay instance() {
		return instance;
	}

	@Override
	public final boolean oppositeOf(Ray ray) {
		return ray.getRayType() == RayType.NORTHEAST;
	}

	@Override
	public Ray getOpposite() {
		return NorthEastRay.instance();
	}
}
