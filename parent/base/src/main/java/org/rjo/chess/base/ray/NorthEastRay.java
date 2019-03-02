package org.rjo.chess.base.ray;

import java.util.ArrayList;

import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.bits.BitSetFactory;
import org.rjo.chess.base.bits.BitSetUnifier;

public class NorthEastRay extends BaseRay {

	private static NorthEastRay instance = new NorthEastRay();

	private NorthEastRay() {
		super(RayType.NORTHEAST, new PieceType[] { PieceType.QUEEN, PieceType.BISHOP });
		final int offset = 9;
		for (int i = 0; i < 64; i++) {
			final BitSetUnifier bitset = BitSetFactory.createBitSet(64);
			raySquares[i] = new ArrayList<>(8);
			int startSquareIndex = i + offset;
			while (startSquareIndex < 64 && startSquareIndex % 8 != 0) {
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

}
