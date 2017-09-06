package org.rjo.chess.ray;

import java.util.ArrayList;

import org.rjo.chess.BitBoard;
import org.rjo.chess.pieces.PieceType;
import org.rjo.chess.util.BitSetFactory;
import org.rjo.chess.util.BitSetUnifier;

public class SouthEastRay extends BaseRay {

	private static SouthEastRay instance = new SouthEastRay();

	private SouthEastRay() {
		super(RayType.SOUTHEAST, new PieceType[] { PieceType.QUEEN, PieceType.BISHOP });
		final int offset = -7;
		for (int i = 0; i < 64; i++) {
			raySquares[i] = new ArrayList<>(8);
			final BitSetUnifier bitset = BitSetFactory.createBitSet(64);
			int startSquareIndex = i + offset;
			while ((startSquareIndex >= 0) && (startSquareIndex % 8 != 0)) {
				raySquares[i].add(startSquareIndex);
				bitset.set(startSquareIndex);
				startSquareIndex += offset;
			}
			attackBitBoard[i] = new BitBoard(bitset);
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
