package org.rjo.newchess.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

public class PositionTest {

   @Test
   public void emptySquares() {
      Position posn = new Position();
      for (int i = 0; i < 64; i++) {
         assertTrue(posn.isEmpty(i));
      }
   }

   @Test
   public void addPiece() {
      Position posn = new Position();
      for (Colour col : new Colour[] { Colour.WHITE, Colour.BLACK }) {
         for (PieceType pt : PieceType.values()) {
            posn.addPiece(col, pt, 0);
            assertTrue(!posn.isEmpty(0));
            assertEquals(col, posn.colourOfPieceAt(0));
            assertEquals(pt, posn.pieceAt(0));
         }
      }
   }

}
