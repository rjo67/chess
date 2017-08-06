package org.rjo.chess.ray;

import java.util.ArrayList;

import org.rjo.chess.pieces.PieceType;

public class NorthWestRay extends BaseRay {

	private static NorthWestRay instance = new NorthWestRay();

	private NorthWestRay() {
		super(RayType.NORTHWEST, true, new PieceType[] { PieceType.QUEEN, PieceType.BISHOP });
		final int offset = 7;
		for (int i = 0; i < 64; i++) {
			raySquares[i] = new ArrayList<>(8);
			int startSquareIndex = i + offset;
			while ((startSquareIndex < 64) && (startSquareIndex % 8 != 7)) {
				raySquares[i].add(startSquareIndex);
				startSquareIndex += offset;
			}
		}
	}

	public static NorthWestRay instance() {
		return instance;
	}

	@Override
	public final boolean oppositeOf(Ray ray) {
		return ray.getRayType() == RayType.SOUTHEAST;
	}

	@Override
	public Ray getOpposite() {
		return SouthEastRay.instance();
	}
}
