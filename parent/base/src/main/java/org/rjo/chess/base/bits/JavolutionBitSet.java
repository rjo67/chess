package org.rjo.chess.base.bits;

import java.lang.reflect.Field;

import org.javolution.util.BitSet;

/**
 * BitSetUnifier-Implementation using the javoluation BitSet.
 *
 * @author rich
 * @since 2017-08-18
 */
public final class JavolutionBitSet implements BitSetUnifier, Cloneable {

	private static Field bitsField;

	/* need this in order to create a BitSet from a long[] */
	static {
		try {
			bitsField = BitSet.class.getDeclaredField("bits");
			bitsField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new IllegalStateException("Javolution BitSet has no field named 'bits'?", e);
		}
	}
	private BitSet bs;

	// just for cloning
	private JavolutionBitSet() {
	}

	public JavolutionBitSet(int nbits) {
		bs = new BitSet();
	}

	public JavolutionBitSet(long[] longarray) {
		bs = new BitSet();
		try {
			bitsField.set(bs, longarray);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalStateException("could not set Javolution BitSet field 'bits' using reflection", e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof JavolutionBitSet)) {
			return false;
		}
		return bs.equals(((JavolutionBitSet) o).bs);
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

	@Override
	public int previousSetBit(int fromIndex) {
		return bs.previousSetBit(fromIndex);
	}

	private void test()  {
		//bs.iterator()
	}

}
