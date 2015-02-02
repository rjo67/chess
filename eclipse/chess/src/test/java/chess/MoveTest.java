package chess;

import org.junit.Test;

import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Queen;
import chess.pieces.Rook;

import static org.junit.Assert.assertEquals;

public class MoveTest {

   @Test
   public void pawnMove() {
      assertEquals(new Move(new Pawn(Colour.WHITE), Square.c5, Square.c6).toString(), "c5-c6");
   }

   @Test
   public void rookMove() {
      assertEquals(new Move(new Rook(Colour.WHITE), Square.a1, Square.a5).toString(), "Ra1-a5");
   }

   @Test
   public void knightMove() {
      assertEquals(new Move(new Knight(Colour.WHITE), Square.d4, Square.e6).toString(), "Nd4-e6");
   }

   @Test
   public void bishopMove() {
      assertEquals(new Move(new Bishop(Colour.WHITE), Square.c5, Square.d6).toString(), "Bc5-d6");
   }

   @Test
   public void queenMove() {
      assertEquals(new Move(new Queen(Colour.WHITE), Square.a1, Square.a8).toString(), "Qa1-a8");
   }

   @Test
   public void kingMove() {
      assertEquals(new Move(new King(Colour.WHITE), Square.c5, Square.c6).toString(), "Kc5-c6");
   }
}
