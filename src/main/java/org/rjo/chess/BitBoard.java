package org.rjo.chess;

import org.rjo.chess.util.BitSetFactory;
import org.rjo.chess.util.BitSetUnifier;

/**
 * Representation of the chessboard using 64 bits. http://chessprogramming.wikispaces.com/Bitboards. Little-Endian
 * Rank-File Mapping:
 * <ul>
 * <li>bit index 0 == bottom left of board == A1</li>
 * <li>bit index 7 == bottom right of board == H1</li>
 * <li>bit index 56 == top left of board == A8</li>
 * <li>bit index 63 == top right of board == H8</li>
 * </ul>
 *
 * @author rich
 */
public class BitBoard {

	public final static BitBoard ALL_SET = new BitBoard(new BitBoard().flip());
	public final static BitBoard EMPTY = new BitBoard();

	/**
	 * BitSets for each <code>file</code> of the board.
	 * <p>
	 * Usage: to just get pieces on the second file, 'and' the bitset with FILE[1]. TODO maybe store these as immutable
	 * BitSets?
	 */
	public final static BitSetUnifier[] FILE = new BitSetUnifier[8];

	/**
	 * BitSets for every <code>file</code> of the board <b>except</b> the file of the array index. The opposite of FILE.
	 * <p>
	 * Usage: to get all pieces EXCEPT those on the second file, 'and' the bitset with EXCEPT_FILE[1]
	 */
	public final static BitSetUnifier[] EXCEPT_FILE = new BitSetUnifier[8];

	/**
	 * BitSets for each rank of the board.
	 * <p>
	 * Usage: to just get pieces on the second rank, 'and' the bitset with RANK[1]
	 */
	public final static BitSetUnifier[] RANK = new BitSetUnifier[8];

	/**
	 * BitSets for every rank of the board <b>except</b> the rank of the array index. The opposite of RANK.
	 * <p>
	 * Usage: to get all pieces EXCEPT those on the second file, 'and' the bitset with EXCEPT_RANK[1]
	 */
	public final static BitSetUnifier[] EXCEPT_RANK = new BitSetUnifier[8];

	// set up the static constants
	static {
		//@formatter:off
      BitBoard[] tmpFile = new BitBoard[] {
            new BitBoard(new byte[] {
                  (byte) 0b10000000,
                  (byte) 0b10000000,
                  (byte) 0b10000000,
                  (byte) 0b10000000,
                  (byte) 0b10000000,
                  (byte) 0b10000000,
                  (byte) 0b10000000,
                  (byte) 0b10000000 }),
            new BitBoard(new byte[] {
                  (byte) 0b01000000,
                  (byte) 0b01000000,
                  (byte) 0b01000000,
                  (byte) 0b01000000,
                  (byte) 0b01000000,
                  (byte) 0b01000000,
                  (byte) 0b01000000,
                  (byte) 0b01000000 }),
            new BitBoard(new byte[] {
                  (byte) 0b00100000,
                  (byte) 0b00100000,
                  (byte) 0b00100000,
                  (byte) 0b00100000,
                  (byte) 0b00100000,
                  (byte) 0b00100000,
                  (byte) 0b00100000,
                  (byte) 0b00100000 }),
            new BitBoard(new byte[] {
                  (byte) 0b00010000,
                  (byte) 0b00010000,
                  (byte) 0b00010000,
                  (byte) 0b00010000,
                  (byte) 0b00010000,
                  (byte) 0b00010000,
                  (byte) 0b00010000,
                  (byte) 0b00010000 }),
            new BitBoard(new byte[] {
                  (byte) 0b00001000,
                  (byte) 0b00001000,
                  (byte) 0b00001000,
                  (byte) 0b00001000,
                  (byte) 0b00001000,
                  (byte) 0b00001000,
                  (byte) 0b00001000,
                  (byte) 0b00001000 }),
            new BitBoard(new byte[] {
                  (byte) 0b00000100,
                  (byte) 0b00000100,
                  (byte) 0b00000100,
                  (byte) 0b00000100,
                  (byte) 0b00000100,
                  (byte) 0b00000100,
                  (byte) 0b00000100,
                  (byte) 0b00000100 }),
            new BitBoard(new byte[] {
                  (byte) 0b00000010,
                  (byte) 0b00000010,
                  (byte) 0b00000010,
                  (byte) 0b00000010,
                  (byte) 0b00000010,
                  (byte) 0b00000010,
                  (byte) 0b00000010,
                  (byte) 0b00000010 }),
            new BitBoard(new byte[] {
                  (byte) 0b00000001,
                  (byte) 0b00000001,
                  (byte) 0b00000001,
                  (byte) 0b00000001,
                  (byte) 0b00000001,
                  (byte) 0b00000001,
                  (byte) 0b00000001,
                  (byte) 0b00000001 })
      };
      BitBoard[] tmpRank = new BitBoard[] {
            new BitBoard(new byte[] {
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b11111111 }),
            new BitBoard(new byte[] {
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b11111111,
                  (byte) 0b00000000 }),
            new BitBoard(new byte[] {
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b11111111,
                  (byte) 0b00000000,
                  (byte) 0b00000000 }),
            new BitBoard(new byte[] {
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b11111111,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000 }),
            new BitBoard(new byte[] {
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b11111111,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000 }),
            new BitBoard(new byte[] {
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b11111111,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000 }),
            new BitBoard(new byte[] {
                  (byte) 0b00000000,
                  (byte) 0b11111111,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000 }),
            new BitBoard(new byte[] {
                  (byte) 0b11111111,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000,
                  (byte) 0b00000000 })
      };
      // @formatter:on

		for (int i = 0; i < 8; i++) {
			FILE[i] = tmpFile[i].getBitSet();
			EXCEPT_FILE[i] = tmpFile[i].flip();
			RANK[i] = tmpRank[i].getBitSet();
			EXCEPT_RANK[i] = tmpRank[i].flip();
		}
	}

	private BitSetUnifier bs;

	/**
	 * default constructor. Initialises the underlying bit set to 64 bits.
	 */
	public BitBoard() {
		bs = BitSetFactory.createBitSet(64);
	}

	/**
	 * Fills the underlying bit with the given 64 bits.
	 */
	public BitBoard(long lo) {
		bs = BitSetFactory.createBitSet(new long[] { lo });
	}

	/**
	 * Fills the underlying bit with the given bitset.
	 */
	public BitBoard(BitSetUnifier bs) {
		this.bs = bs;
	}

	/**
	 * Copy constructor. Clones the underlying bitset of the parameter.
	 */
	public BitBoard(BitBoard bb) {
		this.bs = bb.cloneBitSet();
	}

	public int getValueForRank(int startOfRankAsBitIndex) {
		int val = 0;
		int mask = 1;
		// BitSet.toByteArray() is unfortunately little-endian, we have the least sig bit on the RHS
		for (int i = (startOfRankAsBitIndex * 8) + 7; i >= (startOfRankAsBitIndex * 8); i--) {
			if (this.bs.get(i)) {
				val += mask;
			}
			mask *= 2;
		}
		return val;
	}

	public int getValueForFile(int startOfFileAsBitIndex) {
		int val = 0;
		// we're counting 'down' from most significant bit
		for (int i = startOfFileAsBitIndex; i < 64; i += 8) {
			val = val << 1;
			if (this.bs.get(i)) {
				val += 1;
			}
		}
		return val;
	}

	/**
	 * Initialise a bitboard.
	 *
	 * @param input must be an array of 8 bytes describing the bits that are set. The eighth byte corresponds to the bottom
	 *           row of the board (rank 1). The first byte corresponds to the top row (rank 8). The bits of the input bytes
	 *           will be mapped to the bit set, starting at bottom left === index 0. For example
	 *
	 *           <pre>
	 * new byte[] { (byte) 0b1110_0010, ... };
	 *           </pre>
	 *
	 *           This will lead to the bits 56, 57, 58 and 62 being set in the BitSet.
	 *           <p>
	 *           The advantage of this format is that the initialisation byte array can be read like a chessboard, e.g.:
	 *
	 *           <pre>
	 *           new BitBoard(new byte[] { (byte) 0b01111111, (byte) 0b01111111, (byte) 0b01111111, (byte) 0b01111111, (byte) 0b01111111,
	 *           		(byte) 0b01111111, (byte) 0b01111111, (byte) 0b01111111 });
	 *           </pre>
	 *
	 *           <B>This is not the same order as used by the BitSet.parse(byte[]) method!!</B>
	 */
	public BitBoard(byte[] input) {
		bs = BitSetFactory.createBitSet(64);
		if (input.length != 8) {
			throw new IllegalArgumentException("must supply 8 bytes");
		}
		int bitIndex = -1;
		for (int index = input.length - 1; index >= 0; index--) {
			byte b = input[index];
			// split byte into bits
			for (int mask = 128; mask > 0; mask = mask / 2) {
				bitIndex++;
				if ((b & mask) != 0) {
					bs.set(bitIndex);
				}
			}
		}
	}

	/**
	 * Convenience method to set various bits of the bitboard.
	 *
	 * @param squares a number of squares
	 */
	public void setBitsAt(Square... squares) {
		for (Square coord : squares) {
			set(coord);
		}
	}

	/**
	 * Convenience method to set various bits of the bitboard.
	 *
	 * @param squares a number of squares
	 */
	public void setBitsAt(Integer... bitIndices) {
		for (Integer coord : bitIndices) {
			bs.set(coord);
		}
	}

	/**
	 * allow operations on the underlying BitSet. TODO: return a non-writable version of the bitset?
	 *
	 * @return the bitset representing the BitBoard.
	 */
	public BitSetUnifier getBitSet() {
		return bs;
	}

	/**
	 * returns a copy of the underlying BitSet.
	 *
	 * @return a copy of the underlying bitset.
	 */
	public BitSetUnifier cloneBitSet() {
		return (BitSetUnifier) bs.clone();
	}

	/**
	 * @return a string representation of the bitboard
	 */
	public String display() {
		return display(this.bs);
	}

	/**
	 * @return the long representatation of the bitset.
	 */
	public long toLong() {
		return bs.toLongArray()[0];
	}

	/**
	 * Static method to display a given bitset.
	 */
	public static String display(BitSetUnifier bs) {
		if (bs.size() != 64) {
			return "cannot display bitset of size " + bs.size();
		}
		if (bs.isEmpty()) {
			return "<empty>";
		}
		StringBuilder sb = new StringBuilder(90);
		// need to parse in rows of 8, since bit 0 == bottom left
		for (int file = 56; file >= 0; file = file - 8) {
			for (int rank = 0; rank < 8; rank++) {
				sb.append(bs.get(file + rank) ? '1' : '0');
			}
			sb.append(System.lineSeparator());
		}
		sb.append("(").append(bs.toLongArray()[0]).append("L)");
		return sb.toString();
	}

	/**
	 * Convenience method to return this BitBoard's BitSet 'flipped', i.e. each set bit is unset and v.v.
	 *
	 * @return a new BitSet, complement of this BitBoard's BitSet.
	 */
	public BitSetUnifier flip() {
		BitSetUnifier bs = cloneBitSet();
		bs.flip(0, 64);
		return bs;
	}

	@Override
	public String toString() {
		return display();
	}

	/** delegate to underlying bitset */
	public boolean get(Square sq) {
		return get(sq.bitIndex());
	}

	/** delegate to underlying bitset */
	public boolean get(int sq) {
		return bs.get(sq);
	}

	/** delegate to underlying bitset */
	public void set(Square sq) {
		set(sq.bitIndex());
	}

	/** delegate to underlying bitset */
	public void set(int sqIndex) {
		bs.set(sqIndex);
	}

}
