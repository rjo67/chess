package org.rjo.newchess.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Piece;
import org.rjo.newchess.piece.Pieces;

public class GameTest {

   @Test
   public void startingPosition() {
      Game g = Game.create();
      Position posn = g.getPosition();
      int sqIndex = Square.d1.index();

      assertTrue(!posn.squareIsEmpty(sqIndex));
      assertEquals(Colour.WHITE, posn.colourOfPieceAt(sqIndex));
      assertEquals(Piece.QUEEN, Pieces.toPiece(posn.pieceAt(sqIndex)));

      sqIndex = Square.d8.index();

      assertTrue(!posn.squareIsEmpty(sqIndex));
      assertEquals(Colour.BLACK, posn.colourOfPieceAt(sqIndex));
      assertEquals(Piece.QUEEN, Pieces.toPiece(posn.pieceAt(sqIndex)));
   }

}
