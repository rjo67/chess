package org.rjo.chess.ray;

import java.util.ArrayList;

import org.rjo.chess.pieces.PieceType;

public class NorthEastRay extends BaseRay {

	private static NorthEastRay instance = new NorthEastRay();

	private NorthEastRay() {
		super(RayType.NORTHEAST, new PieceType[] { PieceType.QUEEN, PieceType.BISHOP });
		final int offset = 9;
		for (int i = 0; i < 64; i++) {
			raySquares[i] = new ArrayList<>(8);
			int startSquareIndex = i + offset;
			while ((startSquareIndex < 64) && (startSquareIndex % 8 != 0)) {
				raySquares[i].add(startSquareIndex);
				startSquareIndex += offset;
			}
		}
	}

	public static NorthEastRay instance() {
		return instance;
	}

	@Override
	public final boolean oppositeOf(Ray ray) {
		return ray.getRayType() == RayType.SOUTHWEST;
	}

	@Override
	public Ray getOpposite() {
		return SouthWestRay.instance();
	}
}
