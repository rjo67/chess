package org.rjo.newchess.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

public class GameTest {

   @Test
   public void startingPosition() {
      Game g = Game.create();
      Position posn = g.getPosition();
      int sqIndex = Square.d1.index();

      assertTrue(!posn.isEmpty(sqIndex));
      assertEquals(Colour.WHITE, posn.colourOfPieceAt(sqIndex));
      assertEquals(PieceType.QUEEN, posn.pieceAt(sqIndex));

      sqIndex = Square.d8.index();

      assertTrue(!posn.isEmpty(sqIndex));
      assertEquals(Colour.BLACK, posn.colourOfPieceAt(sqIndex));
      assertEquals(PieceType.QUEEN, posn.pieceAt(sqIndex));
   }

}
