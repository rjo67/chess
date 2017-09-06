package org.rjo.chess.util;

/**
 * Interface for the various bitset implementations.
 *
 * @author rich
 * @since 2017-08-18
 */
public interface BitSetUnifier {

	public Object clone();

	public int size();

	public boolean isEmpty();

	public long[] toLongArray();

	public void flip(int bitIndex);

	public void flip(int fromIndex,
			int toIndex);

	public boolean get(int bitIndex);

	public void set(int bitIndex);

	public void and(BitSetUnifier set);

	public void andNot(BitSetUnifier set);

	public void or(BitSetUnifier set);

	public void xor(BitSetUnifier set);

	public boolean intersects(BitSetUnifier set);

	public int cardinality();

	public void clear(int bitIndex);

	public int nextSetBit(int fromIndex);

	public int previousSetBit(int fromIndex);

}
