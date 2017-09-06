package org.rjo.chess.ray;

import java.util.ArrayList;

import org.rjo.chess.BitBoard;
import org.rjo.chess.pieces.PieceType;
import org.rjo.chess.util.BitSetFactory;
import org.rjo.chess.util.BitSetUnifier;

public class WestRay extends BaseRay {

	private static WestRay instance = new WestRay();

	private WestRay() {
		super(RayType.WEST, new PieceType[] { PieceType.QUEEN, PieceType.ROOK });
		final int offset = -1;
		for (int i = 0; i < 64; i++) {
			raySquares[i] = new ArrayList<>(8);
			int startSquareIndex = i + offset;
			final BitSetUnifier bitset = BitSetFactory.createBitSet(64);
			while ((startSquareIndex >= 0) && (startSquareIndex % 8 != 7)) {
				raySquares[i].add(startSquareIndex);
				bitset.set(startSquareIndex);
				startSquareIndex += offset;
			}
			attackBitBoard[i] = new BitBoard(bitset);
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
