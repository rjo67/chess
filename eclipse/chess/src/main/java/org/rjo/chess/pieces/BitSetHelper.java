package org.rjo.chess.pieces;

import java.util.BitSet;

import org.rjo.chess.BitBoard;

public class BitSetHelper {

   private BitSetHelper() {
   }

   /**
    * General method to shift bits.
    * 
    * @param startPosn
    *           starting position. Does not get changed by this routine.
    * @param shift
    *           how much to shift. Positive==shift to right (<<). Negative==shift to left (>>>).
    * @return a new bitset with the shifted bits.
    */
   public static BitSet shift(BitSet startPosn, int shift) {
      if (startPosn.isEmpty()) {
         return (BitSet) startPosn.clone();
      }

      long lo = startPosn.toLongArray()[0];
      if (shift < 0) {
         return BitSet.valueOf(new long[] { (lo >>> Math.abs(shift)) });
      } else {
         return BitSet.valueOf(new long[] { (lo << shift) });
      }
   }

   /**
    * Shifts the bits in 'startPosn' one rank to North.
    * 
    * @param startPosn
    *           starting position. Does not get changed by this routine.
    * @return a new bitset with the shifted bits.
    */
   public static BitSet shiftOneNorth(BitSet startPosn) {
      if (startPosn.isEmpty()) {
         return (BitSet) startPosn.clone();
      }
      long lo = startPosn.toLongArray()[0];
      // don't need to check for 'overlap' off 8th rank, since only passing one long to valueOf().
      return BitSet.valueOf(new long[] { (lo << 8) });
   }

   /**
    * Shifts the bits in 'startPosn' one rank to South.
    * 
    * @param startPosn
    *           starting position. Does not get changed by this routine.
    * @return a new bitset with the shifted bits.
    */
   public static BitSet shiftOneSouth(BitSet startPosn) {
      if (startPosn.isEmpty()) {
         return (BitSet) startPosn.clone();
      }
      long lo = startPosn.toLongArray()[0];
      return BitSet.valueOf(new long[] { (lo >>> 8) });
   }

   /**
    * Shifts the bits in 'startPosn' one file to West.
    * File 1 does not get wrapped.
    * 
    * @param startPosn
    *           starting position. Does not get changed by this routine.
    * @return a new bitset with the shifted bits.
    */
   public static BitSet shiftOneWest(BitSet startPosn) {
      if (startPosn.isEmpty()) {
         return (BitSet) startPosn.clone();
      }
      BitSet bs = (BitSet) startPosn.clone();
      bs.and(BitBoard.EXCEPT_FILE[0].getBitSet());
      if (bs.isEmpty()) {
         return bs;
      }
      long lo = bs.toLongArray()[0];
      bs = BitSet.valueOf(new long[] { (lo >>> 1) }); // unsigned shift
      return bs;
   }

   /**
    * Shifts the bits in 'startPosn' one file to East.
    * 
    * @param startPosn
    *           starting position. Does not get changed by this routine.
    * @return a new bitset with the shifted bits.
    */
   public static BitSet shiftOneEast(BitSet startPosn) {
      if (startPosn.isEmpty()) {
         return (BitSet) startPosn.clone();
      }
      BitSet bs = (BitSet) startPosn.clone();
      bs.and(BitBoard.EXCEPT_FILE[7].getBitSet());
      if (bs.isEmpty()) {
         return bs;
      }
      long lo = bs.toLongArray()[0];
      bs = BitSet.valueOf(new long[] { (lo << 1) });
      return bs;
   }

   /**
    * Shifts the bits in 'startPosn' one file to west and one rank north.
    * 
    * @param startPosn
    *           starting position. Does not get changed by this routine.
    * @return a new bitset with the shifted bits.
    */
   public static BitSet shiftOneNorthWest(BitSet startBoard) {
      BitSet bs = shiftOneNorth(startBoard);
      return shiftOneWest(bs);
   }

   /**
    * Shifts the bits in 'startPosn' one file to west and one rank south.
    * 
    * @param startPosn
    *           starting position. Does not get changed by this routine.
    * @return a new bitset with the shifted bits.
    */
   public static BitSet shiftOneSouthWest(BitSet startBoard) {
      BitSet bs = shiftOneSouth(startBoard);
      return shiftOneWest(bs);
   }

   /**
    * Shifts the bits in 'startPosn' one file to east and one rank north.
    * 
    * @param startPosn
    *           starting position. Does not get changed by this routine.
    * @return a new bitset with the shifted bits.
    */
   public static BitSet shiftOneNorthEast(BitSet startBoard) {
      BitSet bs = shiftOneNorth(startBoard);
      return shiftOneEast(bs);
   }

   /**
    * Shifts the bits in 'startPosn' one file to east and one rank south.
    * 
    * @param startPosn
    *           starting position. Does not get changed by this routine.
    * @return a new bitset with the shifted bits.
    */
   public static BitSet shiftOneSouthEast(BitSet startBoard) {
      BitSet bs = shiftOneSouth(startBoard);
      return shiftOneEast(bs);
   }
}