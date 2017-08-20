package org.rjo.chess.ray;

import java.util.ArrayList;

import org.rjo.chess.pieces.PieceType;

public class EastRay extends BaseRay {

	private static EastRay instance = new EastRay();

	private EastRay() {
		super(RayType.EAST, false, new PieceType[] { PieceType.QUEEN, PieceType.ROOK });
		final int offset = 1;
		for (int i = 0; i < 64; i++) {
			raySquares[i] = new ArrayList<>(8);
			int startSquareIndex = i + offset;
			while (startSquareIndex % 8 != 0) {
				raySquares[i].add(startSquareIndex);
				startSquareIndex += offset;
			}
		}
	}

	public static EastRay instance() {
		return instance;
	}

	@Override
	public final boolean oppositeOf(Ray ray) {
		return ray.getRayType() == RayType.WEST;
	}

	@Override
	public Ray getOpposite() {
		return WestRay.instance();
	}

}
