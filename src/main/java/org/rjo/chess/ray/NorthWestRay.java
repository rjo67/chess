package org.rjo.chess.ray;

import java.util.ArrayList;

import org.rjo.chess.BitBoard;
import org.rjo.chess.pieces.PieceType;
import org.rjo.chess.util.BitSetFactory;
import org.rjo.chess.util.BitSetUnifier;

public class NorthWestRay extends BaseRay {

	private static NorthWestRay instance = new NorthWestRay();

	private NorthWestRay() {
		super(RayType.NORTHWEST, new PieceType[] { PieceType.QUEEN, PieceType.BISHOP });
		final int offset = 7;
		for (int i = 0; i < 64; i++) {
			raySquares[i] = new ArrayList<>(8);
			final BitSetUnifier bitset = BitSetFactory.createBitSet(64);
			int startSquareIndex = i + offset;
			while (startSquareIndex < 64 && startSquareIndex % 8 != 7) {
				raySquares[i].add(startSquareIndex);
				bitset.set(startSquareIndex);
				startSquareIndex += offset;
			}
			//TODO: store the raySquares lists as immutable and let BaseRay::squaresFrom return the list itself (then can use for-each loop)
			attackBitBoard[i] = new BitBoard(bitset);
		}
	}

	public static NorthWestRay instance() {
		return instance;
	}

}
