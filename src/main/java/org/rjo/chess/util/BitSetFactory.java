package org.rjo.chess.util;

/**
 * Decides on the BitSet implementation to use.
 *
 * @author rich
 * @since 2017-08-19
 */
public class BitSetFactory {

	enum BitSetImplementation {
		CHESS_BITSET, JAVA_UTIL, LUCENE, JAVOLUTION
    }

	/** the bitset implementation to use */
	private final static BitSetImplementation BITSET_IMPL = BitSetImplementation.CHESS_BITSET;

	private BitSetFactory() {
	}

	public static BitSetUnifier createBitSet(int nBits) {
		switch (BITSET_IMPL) {
		case CHESS_BITSET:
			return new ChessBitSetUnifier(nBits);
		case JAVA_UTIL:
			return new JavaUtilBitSet(nBits);
		case LUCENE:
			return new LuceneBitSet(nBits);
		case JAVOLUTION:
			return new JavolutionBitSet(nBits);
		default:
			throw new IllegalArgumentException("non-implemented case switch");
		}
	}

	public static BitSetUnifier createBitSet(long[] longarray) {
		switch (BITSET_IMPL) {
		case CHESS_BITSET:
			return new ChessBitSetUnifier(longarray);
		case JAVA_UTIL:
			return new JavaUtilBitSet(longarray);
		case LUCENE:
			return new LuceneBitSet(longarray);
		case JAVOLUTION:
			return new JavolutionBitSet(longarray);
		default:
			throw new IllegalArgumentException("non-implemented case switch");
		}
	}
}
