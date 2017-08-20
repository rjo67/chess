package org.rjo.chess.ray;

import java.util.ArrayList;

import org.rjo.chess.pieces.PieceType;

public class SouthRay extends BaseRay {

	private static SouthRay instance = new SouthRay();

	private SouthRay() {
		super(RayType.SOUTH, new PieceType[] { PieceType.QUEEN, PieceType.ROOK });
		final int offset = -8;
		for (int i = 0; i < 64; i++) {
			raySquares[i] = new ArrayList<>(8);
			int startSquareIndex = i + offset;
			while (startSquareIndex >= 0) {
				raySquares[i].add(startSquareIndex);
				startSquareIndex += offset;
			}
		}
	}

	public static SouthRay instance() {
		return instance;
	}

	@Override
	public final boolean oppositeOf(Ray ray) {
		return ray.getRayType() == RayType.NORTH;
	}

	@Override
	public Ray getOpposite() {
		return NorthRay.instance();
	}
}
