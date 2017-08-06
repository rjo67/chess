package org.rjo.chess.ray;

import java.util.ArrayList;

import org.rjo.chess.pieces.PieceType;

public class WestRay extends BaseRay {

	private static WestRay instance = new WestRay();

	private WestRay() {
		super(RayType.WEST, false, new PieceType[] { PieceType.QUEEN, PieceType.ROOK });
		final int offset = -1;
		for (int i = 0; i < 64; i++) {
			raySquares[i] = new ArrayList<>(8);
			int startSquareIndex = i + offset;
			while ((startSquareIndex >= 0) && (startSquareIndex % 8 != 7)) {
				raySquares[i].add(startSquareIndex);
				startSquareIndex += offset;
			}
		}
	}

	public static WestRay instance() {
		return instance;
	}

	@Override
	public final boolean oppositeOf(Ray ray) {
		return ray.getRayType() == RayType.EAST;
	}

	@Override
	public Ray getOpposite() {
		return EastRay.instance();
	}
}
