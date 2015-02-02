package chess;

import java.util.BitSet;

/**
 * Representation of the chessboard using 64 bits.
 * http://chessprogramming.wikispaces.com/Bitboards.
 * 
 * Little-Endian Rank-File Mapping:
 * <ul>
 * <li>bit index 0 == bottom left of board == A1</li>
 * <li>bit index 7 == bottom right of board == A8</li>
 * <li>bit index 56 == top left of board == H1</li>
 * <li>bit index 63 == top right of board == H8</li>
 * </ul>
 * 
 * @author rich
 */
/**
 * @author rich
 *
 */
public class BitBoard {

   private BitSet bs = new BitSet(64);

   public BitBoard() {
   }

   /**
    * Initialise a bitboard.
    * 
    * @param input
    *           must be an array of 8 bytes describing the bits that are set.
    *           The first byte corresponds to the bottom row of the board (rank 1).
    *           The 8th byte corresponds to the top row (rank 8).
    *           The bits of the input bytes will be mapped to the bit set, starting at bottom left === index 0.
    *           For example new byte[] { (byte) 0b1110_0010, ... };
    *           This will lead to the bits 0, 1, 2 and 6 being set in the BitSet.
    */
   public BitBoard(byte[] input) {
      if (input.length != 8) {
         throw new IllegalArgumentException("must supply 8 bytes");
      }
      int bitIndex = -1;
      for (int index = 0; index < input.length; index++) {
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
    * 
    * @return the bitset representing the BitBoard.
    */
   public BitSet getBitSet() {
      return bs;
   }

   /**
    * @return a string representation of the bitboard
    */
   public String display() {
      return display(this.bs);
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
}
