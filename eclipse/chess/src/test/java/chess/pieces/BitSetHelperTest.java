package chess.pieces;

import java.util.BitSet;

import org.junit.Test;

import chess.BitBoard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BitSetHelperTest {

   @Test
   public void testOneRankNorth() {
      checkResult(BitSetHelper.oneRankNorth(initStartBits(new int[] { 0, 8, 44, 48, 55, 56, 57 })), new int[] { 8, 16,
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
      BitSet result = BitSetHelper.oneRankNorth(BitSet.valueOf(new long[] { -1 }));
      assertEquals(expected, result);
   }

   @Test
   public void testOneRankSouth() {
      checkResult(BitSetHelper.oneRankSouth(initStartBits(new int[] { 0, 3, 8, 44, 48, 55, 56, 57, 63 })), new int[] {
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
      BitSet result = BitSetHelper.oneRankSouth(BitSet.valueOf(new long[] { -1 }));
      assertEquals(expected, result);
   }

   @Test
   public void testOneFileWest() {
      checkResult(BitSetHelper.oneFileWest(initStartBits(new int[] { 0, 7, 8, 44, 57, 63 })),
            new int[] { 6, 43, 56, 62 });
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
      BitSet result = BitSetHelper.oneFileWest(BitSet.valueOf(new long[] { -1 }));
      assertEquals(expected, result);
   }

   @Test
   public void testOneFileEast() {
      checkResult(BitSetHelper.oneFileEast(initStartBits(new int[] { 0, 7, 8, 63 })), new int[] { 1, 9 });
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
      BitSet result = BitSetHelper.oneFileEast(BitSet.valueOf(new long[] { -1 }));
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
      for (int i = 0; i < setBits.length; i++) {
         bs.set(setBits[i]);
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
      for (int i = 0; i < setBits.length; i++) {
         assertTrue("bit " + setBits[i] + " not set: result: " + bs, bs.get(setBits[i]));
         // blank this bit
         bs.clear(setBits[i]);
      }
      // now the 'bs' should be empty
      assertTrue("bit set contains extraneous bits: " + bs, bs.isEmpty());
   }
}
