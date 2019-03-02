package org.rjo.chess.base.bits;

/**
 * BitSetUnifier-Implementation using ChessBitSet.
 *
 * @author rich
 * @since 2017-09-06
 */
public final class ChessBitSetUnifier implements BitSetUnifier, Cloneable {

	private ChessBitSet bs;

	// just for cloning
	private ChessBitSetUnifier() {
	}

	public ChessBitSetUnifier(int nbits) {
		bs = new ChessBitSet();
	}

	public ChessBitSetUnifier(long[] longarray) {
		bs = new ChessBitSet(longarray[0]);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof ChessBitSetUnifier) {
		   return bs.equals(((ChessBitSetUnifier) obj).bs);
		} else {
			return false;
		}
	}

	@Override
	public Object clone() {
		ChessBitSetUnifier newBs = new ChessBitSetUnifier();
		newBs.bs = this.bs.clone();
		return newBs;
	}

	@Override
	public void flip(int bitIndex) {
		bs.flip(bitIndex);
	}

	@Override
	public void flip(int fromIndex,
			int toIndex) {
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
		ChessBitSetUnifier set2 = (ChessBitSetUnifier) set;
		bs.and(set2.bs);
	}

	@Override
	public void andNot(BitSetUnifier set) {
		ChessBitSetUnifier set2 = (ChessBitSetUnifier) set;
		bs.andNot(set2.bs);

	}

	@Override
	public void or(BitSetUnifier set) {
		ChessBitSetUnifier set2 = (ChessBitSetUnifier) set;
		bs.or(set2.bs);
	}

	@Override
	public void xor(BitSetUnifier set) {
		ChessBitSetUnifier set2 = (ChessBitSetUnifier) set;
		bs.xor(set2.bs);
	}

	@Override
	public boolean intersects(BitSetUnifier set) {
		ChessBitSetUnifier set2 = (ChessBitSetUnifier) set;
		return bs.intersects(set2.bs);
	}

	@Override
	public int size() {
		return bs.length();
	}

	@Override
	public boolean isEmpty() {
		return bs.isEmpty();
	}

	@Override
	public long[] toLongArray() {
		return new long[] { bs.getBits() };
	}

	@Override
	public void clear(int bitIndex) {
		bs.clear(bitIndex);

	}

	@Override
	public int nextSetBit(int fromIndex) {
		if (fromIndex == 64) {
			return -1;
		}
		return bs.nextSetBit(fromIndex);
	}

	@Override
	public int previousSetBit(int fromIndex) {
		if (fromIndex == -1) {
			return -1;
		}
		return bs.prevSetBit(fromIndex);
	}

}
