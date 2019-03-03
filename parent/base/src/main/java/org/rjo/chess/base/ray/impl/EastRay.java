package org.rjo.chess.base.ray.impl;

import java.util.ArrayList;

import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.bits.BitSetFactory;
import org.rjo.chess.base.bits.BitSetUnifier;
import org.rjo.chess.base.ray.RayType;

public class EastRay extends BaseRay {

	private static EastRay instance = new EastRay();

	private EastRay() {
		super(RayType.EAST, new PieceType[] { PieceType.QUEEN, PieceType.ROOK });
		final int offset = 1;
		for (int i = 0; i < 64; i++) {
			final BitSetUnifier bitset = BitSetFactory.createBitSet(64);
			raySquares[i] = new ArrayList<>(8);
			int startSquareIndex = i + offset;
			while (startSquareIndex % 8 != 0) {
				raySquares[i].add(startSquareIndex);
				bitset.set(startSquareIndex);
				startSquareIndex += offset;
			}
			attackBitBoard[i] = new BitBoard(bitset);
		}
	}

	public static EastRay instance() {
		return instance;
	}
}
