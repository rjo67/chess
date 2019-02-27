package org.rjo.chess.util;

import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.FixedBitSet;

/**
 * BitSetUnifier-Implementation using the lucene FixedBitSet.
 *
 * @author rich
 * @since 2017-08-18
 */
public final class LuceneBitSet implements BitSetUnifier, Cloneable {

	private FixedBitSet bs;

	// just for cloning
	private LuceneBitSet() {
	}

	public LuceneBitSet(int nbits) {
		bs = new FixedBitSet(nbits);
	}

	public LuceneBitSet(long[] longarray) {
		bs = new FixedBitSet(longarray, 64);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof LuceneBitSet)) {
			return false;
		}
		return bs.equals(((LuceneBitSet) o).bs);
	}

	@Override
	public Object clone() {
		LuceneBitSet newBs = new LuceneBitSet();
		newBs.bs = this.bs.clone();
		return newBs;
	}

	@Override
	public int cardinality() {
		return bs.cardinality();
	}

	@Override
	public boolean get(int index) {
		return bs.get(index);
	}

	@Override
	public void set(int index) {
		bs.set(index);
	}

	@Override
	public void or(BitSetUnifier set) {
		LuceneBitSet set2 = (LuceneBitSet) set;
		bs.or(set2.bs);
	}

	@Override
	public void xor(BitSetUnifier set) {
		LuceneBitSet set2 = (LuceneBitSet) set;
		bs.xor(set2.bs);
	}

	@Override
	public void and(BitSetUnifier set) {
		LuceneBitSet set2 = (LuceneBitSet) set;
		bs.and(set2.bs);
	}

	@Override
	public void flip(int bitIndex) {
		bs.flip(bitIndex);
	}

	@Override
	public void andNot(BitSetUnifier set) {
		LuceneBitSet set2 = (LuceneBitSet) set;
		bs.andNot(set2.bs);
	}

	@Override
	public boolean intersects(BitSetUnifier set) {
		LuceneBitSet set2 = (LuceneBitSet) set;
		return bs.intersects(set2.bs);
	}

	@Override
	public void flip(int startIndex,
			int endIndex) {
		bs.flip(startIndex, endIndex);
	}

	@Override
	public int size() {
		return bs.length();
	}

	@Override
	public boolean isEmpty() {
		return bs.scanIsEmpty();
	}

	@Override
	public long[] toLongArray() {
		return bs.getBits();
	}

	@Override
	public void clear(int bitIndex) {
		bs.clear(bitIndex);
	}

	@Override
	public int nextSetBit(int fromIndex) {
		// lucene does not allow calling with fromIndex==size of bitset, unlike java.util.BitSet
		if (fromIndex == 64) {
			return -1;
		}
		int nextBit = bs.nextSetBit(fromIndex);
		if (nextBit == DocIdSetIterator.NO_MORE_DOCS) {
			return -1;
		}
		return nextBit;
	}

	@Override
	public int previousSetBit(int fromIndex) {
		// lucene does not allow calling with fromIndex==-1, unlike java.util.BitSet
		if (fromIndex == -1) {
			return -1;
		}
		return bs.prevSetBit(fromIndex);
	}

/*	private void iterator(int from)  {
		int start = from;
		return new IteratorImpl(this, start, false);
	}*/
}
