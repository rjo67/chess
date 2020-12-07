package org.rjo.chess.base.bits;

/**
 * Interface for the various bitset implementations.
 *
 * @author rich
 * @since 2017-08-18
 */
public interface BitSetUnifier {

	Object clone();

	int size();

	boolean isEmpty();

	long[] toLongArray();

	void flip(int bitIndex);

	void flip(int fromIndex,
			int toIndex);

	boolean get(int bitIndex);

	void set(int bitIndex);

	void and(BitSetUnifier set);

	void andNot(BitSetUnifier set);

	void or(BitSetUnifier set);

	void xor(BitSetUnifier set);

	boolean intersects(BitSetUnifier set);

	int cardinality();

	void clear(int bitIndex);

	int nextSetBit(int fromIndex);

	int previousSetBit(int fromIndex);

}
