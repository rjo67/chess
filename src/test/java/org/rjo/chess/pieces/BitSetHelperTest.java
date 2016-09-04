package org.rjo.chess.pieces;

import java.util.BitSet;

import org.junit.Test;
import org.rjo.chess.BitBoard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BitSetHelperTest {

   @Test
   public void testShiftLeft() {
      checkResult(BitSetHelper.shift(initStartBits(new int[] { 0, 8, 44, 48, 55, 56, 57 }), 3), new int[] { 3, 11, 47,
         51, 58, 59, 60 });
   }

   @Test
   public void testOneRankNorth() {
      checkResult(BitSetHelper.shiftOneNorth(initStartBits(new int[] { 0, 8, 44, 48, 55, 56, 57 })), new int[] { 8, 16,
         52, 56, 63 });
   }

   @Test
   public void allBitsOneRankNorth() {
      BitSet expected = new BitBoard(new byte[] {
            //@formatter:off
            (byte) 0b11111111,
            (byte) 0b11111111,
            (byte) 0b11111111,
            (byte) 0b11111111,
            (byte) 0b11111111,
            (byte) 0b11111111,
            (byte) 0b11111111,
            (byte) 0b00000000 }).getBitSet();
      //@formatter:on
      BitSet result = BitSetHelper.shiftOneNorth(BitSet.valueOf(new long[] { -1 }));
      assertEquals(expected, result);
   }

   @Test
   public void testOneNorthWest() {
      // +7 excluding squares on LHS (0,8,48)
      checkResult(BitSetHelper.shiftOneNorthWest(initStartBits(new int[] { 0, 8, 44, 48, 55, 56, 57 })), new int[] {
         51, 62 });
   }

   @Test
   public void allBitsOneNorthWest() {
      BitSet expected = new BitBoard(new byte[] {
            //@formatter:off
            (byte) 0b11111110,
            (byte) 0b11111110,
            (byte) 0b11111110,
            (byte) 0b11111110,
            (byte) 0b11111110,
            (byte) 0b11111110,
            (byte) 0b11111110,
            (byte) 0b00000000 }).getBitSet();
      //@formatter:on
      BitSet result = BitSetHelper.shiftOneNorthWest(BitSet.valueOf(new long[] { -1 }));
      assertEquals(expected, result);
   }

   @Test
   public void testOneNorthEast() {
      // +9 excluding squares on RHS (7,39,55)
      checkResult(BitSetHelper.shiftOneNorthEast(initStartBits(new int[] { 0, 7, 8, 39, 44, 48, 55, 56, 57 })),
            new int[] { 9, 17, 53, 57 });
   }

   @Test
   public void allBitsOneNorthEast() {
      BitSet expected = new BitBoard(new byte[] {
            //@formatter:off
            (byte) 0b01111111,
            (byte) 0b01111111,
            (byte) 0b01111111,
            (byte) 0b01111111,
            (byte) 0b01111111,
            (byte) 0b01111111,
            (byte) 0b01111111,
            (byte) 0b00000000 }).getBitSet();
      //@formatter:on
      BitSet result = BitSetHelper.shiftOneNorthEast(BitSet.valueOf(new long[] { -1 }));
      assertEquals(expected, result);
   }

   @Test
   public void testOneSouthEast() {
      // -7 excluding squares on RHS (7,39,55)
      checkResult(BitSetHelper.shiftOneSouthEast(initStartBits(new int[] { 0, 7, 8, 39, 44, 48, 55, 56, 57 })),
            new int[] { 1, 37, 41, 49, 50 });
   }

   @Test
   public void allBitsOneSouthEast() {
      BitSet expected = new BitBoard(new byte[] {
            //@formatter:off
            (byte) 0b00000000,
            (byte) 0b01111111,
            (byte) 0b01111111,
            (byte) 0b01111111,
            (byte) 0b01111111,
            (byte) 0b01111111,
            (byte) 0b01111111,
            (byte) 0b01111111 }).getBitSet();
      //@formatter:on
      BitSet result = BitSetHelper.shiftOneSouthEast(BitSet.valueOf(new long[] { -1 }));
      assertEquals(expected, result);
   }

   @Test
   public void testOneSouthWest() {
      // -9 excluding squares on LHS (0,8,48,56)
      checkResult(BitSetHelper.shiftOneSouthWest(initStartBits(new int[] { 0, 8, 44, 48, 55, 56, 57 })), new int[] {
         35, 46, 48 });
   }

   @Test
   public void allBitsOneSouthWest() {
      BitSet expected = new BitBoard(new byte[] {
            //@formatter:off
            (byte) 0b00000000,
            (byte) 0b11111110,
            (byte) 0b11111110,
            (byte) 0b11111110,
            (byte) 0b11111110,
            (byte) 0b11111110,
            (byte) 0b11111110,
            (byte) 0b11111110 }).getBitSet();
      //@formatter:on
      BitSet result = BitSetHelper.shiftOneSouthWest(BitSet.valueOf(new long[] { -1 }));
      assertEquals(expected, result);
   }

   @Test
   public void testOneRankSouth() {
      checkResult(BitSetHelper.shiftOneSouth(initStartBits(new int[] { 0, 3, 8, 44, 48, 55, 56, 57, 63 })), new int[] {
         0, 36, 40, 47, 48, 49, 55 });
   }

   @Test
   public void allBitsOneRankSouth() {
      BitSet expected = new BitBoard(new byte[] {
            //@formatter:off
            (byte) 0b00000000,
            (byte) 0b11111111,
            (byte) 0b11111111,
            (byte) 0b11111111,
            (byte) 0b11111111,
            (byte) 0b11111111,
            (byte) 0b11111111,
            (byte) 0b11111111 }).getBitSet();
      //@formatter:on
      BitSet result = BitSetHelper.shiftOneSouth(BitSet.valueOf(new long[] { -1 }));
      assertEquals(expected, result);
   }

   @Test
   public void testOneFileWest() {
      checkResult(BitSetHelper.shiftOneWest(initStartBits(new int[] { 0, 7, 8, 44, 57, 63 })), new int[] { 6, 43, 56,
         62 });
   }

   @Test
   public void allBitsOneFileWest() {
      BitSet expected = new BitBoard(new byte[] {
            //@formatter:off
            (byte) 0b11111110,
            (byte) 0b11111110,
            (byte) 0b11111110,
            (byte) 0b11111110,
            (byte) 0b11111110,
            (byte) 0b11111110,
            (byte) 0b11111110,
            (byte) 0b11111110 }).getBitSet();
      //@formatter:on
      BitSet result = BitSetHelper.shiftOneWest(BitSet.valueOf(new long[] { -1 }));
      assertEquals(expected, result);
   }

   @Test
   public void testOneFileEast() {
      checkResult(BitSetHelper.shiftOneEast(initStartBits(new int[] { 0, 7, 8, 63 })), new int[] { 1, 9 });
   }

   @Test
   public void allBitsOneFileEast() {
      BitSet expected = new BitBoard(new byte[] {
            //@formatter:off
            (byte) 0b01111111,
            (byte) 0b01111111,
            (byte) 0b01111111,
            (byte) 0b01111111,
            (byte) 0b01111111,
            (byte) 0b01111111,
            (byte) 0b01111111,
            (byte) 0b01111111 }).getBitSet();
      //@formatter:on
      BitSet result = BitSetHelper.shiftOneEast(BitSet.valueOf(new long[] { -1 }));
      assertEquals(expected, result);
   }

   /**
    * Sets up a bit set with the required bits.
    *
    * @param setBits
    *           bits to set
    * @return a new bit set with the required bits set.
    */
   private BitSet initStartBits(int[] setBits) {
      BitSet bs = new BitSet(64);
      for (int setBit : setBits) {
         bs.set(setBit);
      }
      return bs;
   }

   /**
    * Checks that the given bitset contains the correct bits.
    *
    * @param bs
    *           bitset to check. Will be modified by this method!
    * @param setBits
    *           bits that should be set
    */
   private void checkResult(BitSet bs, int[] setBits) {
      for (int setBit : setBits) {
         assertTrue("bit " + setBit + " not set: remaining bits in result: " + bs, bs.get(setBit));
         // blank this bit
         bs.clear(setBit);
      }
      // now the 'bs' should be empty
      assertTrue("bit set contains extraneous bits: " + bs, bs.isEmpty());
   }
}