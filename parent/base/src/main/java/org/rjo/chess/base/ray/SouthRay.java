package org.rjo.chess.base.ray;

import java.util.ArrayList;

import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.bits.BitSetFactory;
import org.rjo.chess.base.bits.BitSetUnifier;

public class SouthRay extends BaseRay {

	private static SouthRay instance = new SouthRay();

	private SouthRay() {
		super(RayType.SOUTH, new PieceType[] { PieceType.QUEEN, PieceType.ROOK });
		final int offset = -8;
		for (int i = 0; i < 64; i++) {
			raySquares[i] = new ArrayList<>(8);
			int startSquareIndex = i + offset;
			final BitSetUnifier bitset = BitSetFactory.createBitSet(64);
			while (startSquareIndex >= 0) {
				raySquares[i].add(startSquareIndex);
				bitset.set(startSquareIndex);
				startSquareIndex += offset;
			}
			attackBitBoard[i] = new BitBoard(bitset);
		}
	}

	public static SouthRay instance() {
		return instance;
	}
}
