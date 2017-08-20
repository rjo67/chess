package org.rjo.chess.ray;

import java.util.ArrayList;

import org.rjo.chess.pieces.PieceType;

public class NorthRay extends BaseRay {

	private static NorthRay instance = new NorthRay();

	private NorthRay() {
		super(RayType.NORTH, false, new PieceType[] { PieceType.QUEEN, PieceType.ROOK });
		final int offset = 8;
		for (int i = 0; i < 64; i++) {
			raySquares[i] = new ArrayList<>(8);
			int startSquareIndex = i + offset;
			while (startSquareIndex < 64) {
				raySquares[i].add(startSquareIndex);
				startSquareIndex += offset;
			}
		}
	}

	public static NorthRay instance() {
		return instance;
	}

	@Override
	public final boolean oppositeOf(Ray ray) {
		return ray.getRayType() == RayType.SOUTH;
	}

	@Override
	public Ray getOpposite() {
		return SouthRay.instance();
	}
}
