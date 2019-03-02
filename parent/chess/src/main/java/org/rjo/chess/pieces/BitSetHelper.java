package org.rjo.chess.pieces;

import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.bits.BitSetFactory;
import org.rjo.chess.base.bits.BitSetUnifier;

public class BitSetHelper {

	private BitSetHelper() {
	}

	/**
	 * General method to shift bits.
	 *
	 * @param startPosn starting position. Does not get changed by this routine.
	 * @param shift how much to shift. Positive==shift to right (<<). Negative==shift to left (>>>).
	 * @return a new bitset with the shifted bits.
	 */
	public static BitSetUnifier shift(BitSetUnifier startPosn,
			int shift) {
		if (startPosn.isEmpty()) {
			return (BitSetUnifier) startPosn.clone();
		}

		long lo = startPosn.toLongArray()[0];
		if (shift < 0) {
			return BitSetFactory.createBitSet(new long[] { lo >>> Math.abs(shift) });
		} else {
			return BitSetFactory.createBitSet(new long[] { lo << shift });
		}
	}

	/**
	 * Shifts the bits in <code>startPosn</code> one rank to North.
	 *
	 * @param startPosn starting position. Does not get changed by this routine.
	 * @return a new bitset with the shifted bits.
	 */
	public static BitSetUnifier shiftOneNorth(BitSetUnifier startPosn) {
		if (startPosn.isEmpty()) {
			return (BitSetUnifier) startPosn.clone();
		}
		long lo = startPosn.toLongArray()[0];
		// don't need to check for 'overlap' off 8th rank, since only passing one long to valueOf().
		return BitSetFactory.createBitSet(new long[] { lo << 8 });
	}

	/**
	 * Shifts the bits in <code>startPosn</code> one rank to South.
	 *
	 * @param startPosn starting position. Does not get changed by this routine.
	 * @return a new bitset with the shifted bits.
	 */
	public static BitSetUnifier shiftOneSouth(BitSetUnifier startPosn) {
		if (startPosn.isEmpty()) {
			return (BitSetUnifier) startPosn.clone();
		}
		long lo = startPosn.toLongArray()[0];
		return BitSetFactory.createBitSet(new long[] { lo >>> 8 });
	}

	/**
	 * Shifts the bits in <code>startPosn</code> one file to West. File 1 does not get wrapped.
	 *
	 * @param startPosn starting position. Does not get changed by this routine.
	 * @return a new bitset with the shifted bits.
	 */
	public static BitSetUnifier shiftOneWest(BitSetUnifier startPosn) {
		return shiftOneWest(startPosn, true);
	}

	/**
	 * Shifts the bits in <code>startPosn</code> one file to West. File 1 does not get wrapped.
	 *
	 * @param startPosn starting position. <b>May</b> get changed, depending on the value of <code>clone</code>.
	 * @param clone if true, will always clone <code>startPosn</code>. If false, <code>startPosn</code> will be changed by
	 *           this call.
	 * @return a (potentially new) bitset with the shifted bits.
	 */
	public static BitSetUnifier shiftOneWest(BitSetUnifier startPosn,
			boolean clone) {
		if (startPosn.isEmpty()) {
			if (clone) {
				return (BitSetUnifier) startPosn.clone();
			} else {
				return startPosn;
			}
		}
		BitSetUnifier bs;
		if (clone) {
			bs = (BitSetUnifier) startPosn.clone();
		} else {
			bs = startPosn;
		}
		bs.and(BitBoard.EXCEPT_FILE[0]);
		if (bs.isEmpty()) {
			return bs;
		}
		long lo = bs.toLongArray()[0];
		bs = BitSetFactory.createBitSet(new long[] { lo >>> 1 }); // unsigned shift
		return bs;
	}

	/**
	 * Shifts the bits in <code>startPosn</code> one file to East.
	 *
	 * @param startPosn starting position. Does not get changed by this routine.
	 * @return a new bitset with the shifted bits.
	 */
	public static BitSetUnifier shiftOneEast(BitSetUnifier startPosn) {
		return shiftOneEast(startPosn, true);
	}

	/**
	 * Shifts the bits in <code>startPosn</code> one file to East.
	 *
	 * @param startPosn starting position. <b>May</b> get changed, depending on the value of <code>clone</code>.
	 * @param clone if true, will always clone <code>startPosn</code>. If false, <code>startPosn</code> will be changed by
	 *           this call.
	 * @return a (potentially new) bitset with the shifted bits.
	 */
	public static BitSetUnifier shiftOneEast(BitSetUnifier startPosn,
			boolean clone) {
		if (startPosn.isEmpty()) {
			if (clone) {
				return (BitSetUnifier) startPosn.clone();
			} else {
				return startPosn;
			}
		}
		BitSetUnifier bs;
		if (clone) {
			bs = (BitSetUnifier) startPosn.clone();
		} else {
			bs = startPosn;
		}
		bs.and(BitBoard.EXCEPT_FILE[7]);
		if (bs.isEmpty()) {
			return bs;
		}
		long lo = bs.toLongArray()[0];
		bs = BitSetFactory.createBitSet(new long[] { lo << 1 });
		return bs;
	}

	/**
	 * Shifts the bits in <code>startPosn</code> one file to west and one rank north.
	 *
	 * @param startBoard starting position. Does not get changed by this routine.
	 * @return a new bitset with the shifted bits.
	 */
	public static BitSetUnifier shiftOneNorthWest(BitSetUnifier startBoard) {
		BitSetUnifier bs = shiftOneNorth(startBoard);
		return shiftOneWest(bs);
	}

	/**
	 * Shifts the bits in <code>startPosn</code> one file to west and one rank south.
	 *
	 * @param startBoard starting position. Does not get changed by this routine.
	 * @return a new bitset with the shifted bits.
	 */
	public static BitSetUnifier shiftOneSouthWest(BitSetUnifier startBoard) {
		BitSetUnifier bs = shiftOneSouth(startBoard);
		return shiftOneWest(bs);
	}

	/**
	 * Shifts the bits in <code>startPosn</code> one file to east and one rank north.
	 *
	 * @param startBoard starting position. Does not get changed by this routine.
	 * @return a new bitset with the shifted bits.
	 */
	public static BitSetUnifier shiftOneNorthEast(BitSetUnifier startBoard) {
		BitSetUnifier bs = shiftOneNorth(startBoard);
		return shiftOneEast(bs);
	}

	/**
	 * Shifts the bits in <code>startPosn</code> one file to east and one rank south.
	 *
	 * @param startBoard starting position. Does not get changed by this routine.
	 * @return a new bitset with the shifted bits.
	 */
	public static BitSetUnifier shiftOneSouthEast(BitSetUnifier startBoard) {
		BitSetUnifier bs = shiftOneSouth(startBoard);
		return shiftOneEast(bs);
	}
}
