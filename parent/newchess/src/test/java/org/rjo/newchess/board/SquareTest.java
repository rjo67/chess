package org.rjo.newchess.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.rjo.newchess.board.Board.Square;

public class SquareTest {

   @Test
   public void enumCoverage() {
      // increase coverage of enum
      Square.values();
      Square.valueOf("a2");
   }

   @Test
   public void mapFromIndexToSquare() {
      assertEquals(Square.a1, Square.toSquare(56));
   }

   @Test
   public void mapFromSquareToIndex() {
      assertEquals(49, Square.b2.index());
   }

   @Test
   public void nameOfSquare() {
      assertEquals("h1", Square.h1.toString());
      assertEquals("c4", Square.c4.toString());
   }

   @Test
   public void goodInput() {
      assertEquals(Square.b6, Square.valueOf("b6"));
   }

   @Test
   public void badInput() {
      assertThrows(IllegalArgumentException.class, () -> Square.valueOf("A9"));
      assertThrows(IllegalArgumentException.class, () -> Square.valueOf("t2"));
   }

}
