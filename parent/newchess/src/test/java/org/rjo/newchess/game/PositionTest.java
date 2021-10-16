package org.rjo.newchess.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Position.SquareInfo;
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
      int sq = 0;
      for (Colour col : new Colour[] { Colour.WHITE, Colour.BLACK }) {
         for (PieceType pt : PieceType.values()) {
            posn.addPiece(col, pt, sq);
            assertTrue(!posn.isEmpty(sq));
            assertEquals(col, posn.colourOfPieceAt(sq));
            assertEquals(pt, posn.pieceAt(sq));
            sq++;
         }
      }
   }

   @Test
   public void speedTest() {
      Position posn = new Position(Square.e1, Square.e8);
      StopWatch sw = StopWatch.createStarted();
      for (int i = 0; i < 1000000; i++) {
         PieceType pt = posn.pieceAt(Square.e1.index());
         Colour col = posn.colourOfPieceAt(Square.e1.index());
         if (pt == PieceType.KING && col == Colour.BLACK) { System.out.println("nein"); }
      }
      System.out.println("Position speedTest #1: " + sw.getTime());
      sw = StopWatch.createStarted();
      Position.SquareInfo info = new SquareInfo(PieceType.KING, Colour.WHITE);
      for (int i = 0; i < 1000000; i++) {
         PieceType pt = info.getPieceType();
         Colour col = info.getColour();
         if (pt == PieceType.KING && col == Colour.BLACK) { System.out.println("nein"); }
      }
      System.out.println("Position speedTest #2: " + sw.getTime());
      /*
       * Position speedTest #1: 11, Position speedTest #2: 4
       */
   }

}
