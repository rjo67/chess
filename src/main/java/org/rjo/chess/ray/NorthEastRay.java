package org.rjo.chess.ray;

import java.util.ArrayList;

import org.rjo.chess.BitBoard;
import org.rjo.chess.pieces.PieceType;
import org.rjo.chess.util.BitSetFactory;
import org.rjo.chess.util.BitSetUnifier;

public class NorthEastRay extends BaseRay {

	private static NorthEastRay instance = new NorthEastRay();

	private NorthEastRay() {
		super(RayType.NORTHEAST, new PieceType[] { PieceType.QUEEN, PieceType.BISHOP });
		final int offset = 9;
		for (int i = 0; i < 64; i++) {
			final BitSetUnifier bitset = BitSetFactory.createBitSet(64);
			raySquares[i] = new ArrayList<>(8);
			int startSquareIndex = i + offset;
			while ((startSquareIndex < 64) && (startSquareIndex % 8 != 0)) {
				raySquares[i].add(startSquareIndex);
				bitset.set(startSquareIndex);
				startSquareIndex += offset;
			}
			attackBitBoard[i] = new BitBoard(bitset);
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
