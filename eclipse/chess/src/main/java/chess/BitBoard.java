package chess;

import java.util.BitSet;

/**
 * Representation of the chessboard using 64 bits.
 * http://chessprogramming.wikispaces.com/Bitboards.
 * 
 * Little-Endian Rank-File Mapping:
 * <ul>
 * <li>bit index 0 == bottom left of board == A1</li>
 * <li>bit index 7 == bottom right of board == H1</li>
 * <li>bit index 56 == top left of board == A8</li>
 * <li>bit index 63 == top right of board == H8</li>
 * </ul>
 * 
 * @author rich
 *
 */
public class BitBoard {

   /**
    * all bits set apart from those on the first file i.e. the LHS.
    * TODO maybe store these directly as BitSets / immutable BitSets?
    */
   public static BitBoard NOT_FILE_ONE = new BitBoard(new byte[] {
         //@formatter:off
         (byte) 0b01111111,
         (byte) 0b01111111,
         (byte) 0b01111111,
         (byte) 0b01111111,
         (byte) 0b01111111,
         (byte) 0b01111111,
         (byte) 0b01111111,
         (byte) 0b01111111 });
         //@formatter:on
   /**
    * all bits set apart from those on the eighth file, i.e. the RHS.
    */
   public static BitBoard NOT_FILE_EIGHT = new BitBoard(new byte[] {
         //@formatter:off
         (byte) 0b11111110,
         (byte) 0b11111110,
         (byte) 0b11111110,
         (byte) 0b11111110,
         (byte) 0b11111110,
         (byte) 0b11111110,
         (byte) 0b11111110,
         (byte) 0b11111110 });
         //@formatter:on
   /**
    * only the 2nd rank bits are set.
    */
   public static BitBoard ONLY_RANK_TWO = new BitBoard(new byte[] {
         //@formatter:off
         (byte) 0b00000000,
         (byte) 0b00000000,
         (byte) 0b00000000,
         (byte) 0b00000000,
         (byte) 0b00000000,
         (byte) 0b00000000,
         (byte) 0b11111111,
         (byte) 0b00000000 });
         //@formatter:off
   /**
    * only the 8th rank bits are set.
    */
   public static BitBoard ONLY_RANK_EIGHT = new BitBoard(new byte[] {
         //@formatter:off
         (byte) 0b11111111,
         (byte) 0b00000000,
         (byte) 0b00000000,
         (byte) 0b00000000,
         (byte) 0b00000000,
         (byte) 0b00000000,
         (byte) 0b00000000,
         (byte) 0b00000000 });
         //@formatter:off

   private BitSet bs = new BitSet(64);

   public BitBoard() {
   }

   public BitBoard(long lo) {
      bs = BitSet.valueOf(new long[] { lo });
   }

   public BitBoard(BitSet bs) {
      this.bs = bs;
   }

   /**
    * Initialise a bitboard.
    * 
    * @param input
    *           must be an array of 8 bytes describing the bits that are set.
    *           The eighth byte corresponds to the bottom row of the board (rank 1).
    *           The first byte corresponds to the top row (rank 8).
    *           The bits of the input bytes will be mapped to the bit set, starting at bottom left === index 0.
    *           For example <pre>new byte[] { (byte) 0b1110_0010, ... };</pre>
    *           This will lead to the bits 56, 57, 58 and 62 being set in the BitSet.
    *           <p>
    *           The advantage of this format is that the initialisation byte array can be read like a chessboard, e.g.:
    *       <pre>new BitBoard(new byte[] {
    *                (byte) 0b01111111,
    *                (byte) 0b01111111,
    *                (byte) 0b01111111,
    *                (byte) 0b01111111,
    *                (byte) 0b01111111,
    *                (byte) 0b01111111,
    *                (byte) 0b01111111,
    *                (byte) 0b01111111 });</pre>
    * 
    *           <B>This is not the same order as used by the BitSet.parse(byte[]) method!!</B>
    */
   public BitBoard(byte[] input) {
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
    * @param squares
    *           a number of squares
    */
   public void setBitsAt(Square... squares) {
      for (Square coord : squares) {
         this.bs.set(coord.bitPosn());
      }
   }

   /**
    * allow operations on the underlying BitSet.
    * TODO: return a non-writable version of the bitset?
    * 
    * @return the bitset representing the BitBoard.
    */
   public BitSet getBitSet() {
      return bs;
   }

   /**
    * returns a copy of the underlying BitSet.
    * 
    * @return a copy of the underlying bitset.
    */
   public BitSet cloneBitSet() {
      return (BitSet) bs.clone();
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
   public static String display(BitSet bs) {
      if (bs.size() != 64) {
         return "cannot display bitset of size " + bs.size();
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
   public BitSet flip() {
      BitSet bs = cloneBitSet();
      bs.flip(0, 64);
      return bs;
   }
}
