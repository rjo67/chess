package org.rjo.chess.base.ray;

import java.util.ArrayList;

import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.bits.BitSetFactory;
import org.rjo.chess.base.bits.BitSetUnifier;

public class NorthRay extends BaseRay {

	private static NorthRay instance = new NorthRay();

	private NorthRay() {
		super(RayType.NORTH, new PieceType[] { PieceType.QUEEN, PieceType.ROOK });
		final int offset = 8;
		for (int i = 0; i < 64; i++) {
			final BitSetUnifier bitset = BitSetFactory.createBitSet(64);
			raySquares[i] = new ArrayList<>(8);
			int startSquareIndex = i + offset;
			while (startSquareIndex < 64) {
				raySquares[i].add(startSquareIndex);
				bitset.set(startSquareIndex);
				startSquareIndex += offset;
			}
			attackBitBoard[i] = new BitBoard(bitset);
		}
	}

	public static NorthRay instance() {
		return instance;
	}
}
