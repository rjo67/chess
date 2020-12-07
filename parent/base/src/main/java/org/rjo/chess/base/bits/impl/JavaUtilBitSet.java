package org.rjo.chess.base.bits.impl;

import java.util.BitSet;
import java.util.stream.IntStream;

import org.rjo.chess.base.bits.BitSetUnifier;

/**
 * BitSetUnifier-Implementation using java.util.BitSet.
 *
 * @author rich
 * @since 2017-08-18
 */
public final class JavaUtilBitSet implements BitSetUnifier, Cloneable {

	private BitSet bs;

	// just for cloning
	private JavaUtilBitSet() {
	}

	public JavaUtilBitSet(int nbits) {
		bs = new BitSet(nbits);
	}

	public JavaUtilBitSet(long[] longarray) {
		bs = BitSet.valueOf(longarray);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof JavaUtilBitSet) {
			return bs.equals(((JavaUtilBitSet) obj).bs);
		} else {
			return false;
		}
	}

	@Override
	public Object clone() {
		JavaUtilBitSet newBs = new JavaUtilBitSet();
		newBs.bs = (BitSet) this.bs.clone();
		return newBs;
	}

	@Override
	public void flip(int bitIndex) {
		bs.flip(bitIndex);
	}

	@Override
	public void flip(int fromIndex, int toIndex) {
		bs.flip(fromIndex, toIndex);
	}

	@Override
	public void set(int bitIndex) {
		bs.set(bitIndex);
	}

	@Override
	public boolean get(int bitIndex) {
		return bs.get(bitIndex);
	}

	@Override
	public int cardinality() {
		return bs.cardinality();
	}

	@Override
	public void and(BitSetUnifier set) {
		JavaUtilBitSet set2 = (JavaUtilBitSet) set;
		bs.and(set2.bs);
	}

	@Override
	public void andNot(BitSetUnifier set) {
		JavaUtilBitSet set2 = (JavaUtilBitSet) set;
		bs.andNot(set2.bs);

	}

	@Override
	public void or(BitSetUnifier set) {
		JavaUtilBitSet set2 = (JavaUtilBitSet) set;
		bs.or(set2.bs);
	}

	@Override
	public void xor(BitSetUnifier set) {
		JavaUtilBitSet set2 = (JavaUtilBitSet) set;
		bs.xor(set2.bs);
	}

	@Override
	public boolean intersects(BitSetUnifier set) {
		JavaUtilBitSet set2 = (JavaUtilBitSet) set;
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
	public void clear(int bitIndex) {
		bs.clear(bitIndex);
	}

	@Override
	public int nextSetBit(int fromIndex) {
		return bs.nextSetBit(fromIndex);
	}

	@Override
	public int previousSetBit(int fromIndex) {
		return bs.previousSetBit(fromIndex);
	}

	public IntStream stream() {
		return bs.stream();
	}
}
