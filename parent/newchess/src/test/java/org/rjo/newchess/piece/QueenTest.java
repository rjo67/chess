package org.rjo.newchess.piece;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.rjo.newchess.board.Board.Square;

public class QueenTest {

   @Test
   public void testCreate() {
      Piece queens = new Queen(Colour.WHITE, Square.a5, Square.g7);
      assertEquals(Colour.WHITE, queens.getColour());
      assertEquals(2, queens.getPositions().size());
      assertEquals(Square.a5.index(), queens.getPositions().get(0));
      assertEquals(Square.g7.index(), queens.getPositions().get(1));

      queens.add(Square.d1.index());
      assertEquals(3, queens.getPositions().size());
      assertEquals(Square.d1.index(), queens.getPositions().get(2));
   }

   @Test
   public void testEmptyCreate() {
      Piece queens = new Queen(Colour.WHITE);
      assertEquals(Colour.WHITE, queens.getColour());
      assertEquals(0, queens.getPositions().size());

      queens.add(Square.d1.index());
      assertEquals(1, queens.getPositions().size());
      assertEquals(Square.d1.index(), queens.getPositions().get(0));
   }

}
