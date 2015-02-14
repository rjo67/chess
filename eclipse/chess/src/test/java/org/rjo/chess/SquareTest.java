package org.rjo.chess;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SquareTest {

   @Test
   public void mappingToBitPosn() {
      assertEquals(0, Square.a1.bitPosn());
   }

   @Test
   public void mappingFromBitPosn() {
      assertEquals(Square.a8, Square.fromBitPosn(56));
   }

   @Test
   public void nameOfSquare() {
      assertEquals("h1", Square.h1.toString());
   }

   /**
    * make sure the bit positions have been entered correctlry
    */
   @Test
   public void bitPosns() {
      for (int i = 0; i < 64; i++) {
         assertEquals(i, Square.fromBitPosn(i).bitPosn());
      }
   }

   @Test(expected = IllegalArgumentException.class)
   public void badRank() {
      Square.fromString("A9");
   }

   @Test(expected = IllegalArgumentException.class)
   public void badRank2() {
      Square.fromString("A0");
   }

   @Test(expected = IllegalArgumentException.class)
   public void badFile() {
      Square.fromString("I6");
   }

   @Test(expected = IllegalArgumentException.class)
   public void badFile2() {
      Square.fromString("06");
   }
}
