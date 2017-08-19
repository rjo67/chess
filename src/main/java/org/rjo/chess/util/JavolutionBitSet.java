package org.rjo.chess.util;

import org.javolution.util.BitSet;

/**
 * BitSetUnifier-Implementation using the javoluation BitSet.
 *
 * @author rich
 * @since 2017-08-18
 */
public class JavolutionBitSet implements BitSetUnifier {

	private BitSet bs;

	// just for cloning
	private JavolutionBitSet() {
	}

	public JavolutionBitSet(int nbits) {
		bs = new BitSet();
	}

	public JavolutionBitSet(long[] longarray) {
		bs = new BitSet();
		//TODO hack in the array using reflection?
		throw new IllegalStateException("no such constructor for javolution yet");
	}

	@Override
	public boolean equals(Object obj) {
		return bs.equals(((JavolutionBitSet) obj).bs);
	}

	@Override
	public Object clone() {
		JavolutionBitSet newBs = new JavolutionBitSet();
		newBs.bs = this.bs.clone();
		return newBs;
	}

	@Override
	public int cardinality() {
		return bs.cardinality();
	}

	@Override
	public void flip(int fromIndex,
			int toIndex) {
		bs.flip(fromIndex, toIndex);
	}

	@Override
	public boolean get(int bitIndex) {
		return bs.get(bitIndex);
	}

	@Override
	public void set(int bitIndex) {
		bs.set(bitIndex);
	}

	@Override
	public void or(BitSetUnifier set) {
		JavolutionBitSet set2 = (JavolutionBitSet) set;
		bs.or(set2.bs);
	}

	@Override
	public void and(BitSetUnifier set) {
		JavolutionBitSet set2 = (JavolutionBitSet) set;
		bs.and(set2.bs);
	}

	@Override
	public void xor(BitSetUnifier set) {
		JavolutionBitSet set2 = (JavolutionBitSet) set;
		bs.xor(set2.bs);
	}

	@Override
	public void andNot(BitSetUnifier set) {
		JavolutionBitSet set2 = (JavolutionBitSet) set;
		bs.andNot(set2.bs);
	}

	@Override
	public boolean intersects(BitSetUnifier set) {
		JavolutionBitSet set2 = (JavolutionBitSet) set;
		return bs.intersects(set2.bs);
	}

	@Override
	public int size() {
		return bs.size();
	}

	@Override
	public boolean isEmpty() {
		return bs.isEmpty();
	}

	@Override
	public long[] toLongArray() {
		return bs.toLongArray();
	}

	@Override
	public void flip(int bitIndex) {
		bs.flip(bitIndex);
	}

	@Override
	public void clear(int bitIndex) {
		bs.clear(bitIndex);
	}

	@Override
	public int nextSetBit(int fromIndex) {
		return bs.nextSetBit(fromIndex);
	}
}
