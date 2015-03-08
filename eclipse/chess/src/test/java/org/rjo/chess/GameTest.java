package org.rjo.chess;

import org.junit.Test;
import org.rjo.chess.pieces.PieceType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GameTest {

   @Test(expected = IllegalArgumentException.class)
   public void wrongSideToMove() {
      Game game = new Game();
      game.move(new Move(PieceType.PAWN, Colour.BLACK, Square.b7, Square.b5));
   }

   @Test
   public void testMove() {
      Game game = new Game();
      game.move(new Move(PieceType.PAWN, Colour.WHITE, Square.b2, Square.b4));
      Chessboard cb = game.getChessboard();

      assertEmptySquare(cb, Square.b2);
      assertPieceAt(cb, Square.b4, PieceType.PAWN);
   }

   @Test(expected = IllegalArgumentException.class)
   public void illegalMoveToNonEmptySquare() {
      Game game = Fen.decode("8/8/8/3p4/2P5/8/8/8 w - - 0 1");
      game.move(new Move(PieceType.PAWN, Colour.WHITE, Square.c4, Square.d5));
   }

   @Test
   public void kingsCastlingWhite() {
      Game game = Fen.decode("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KkQq - 0 1");
      game.move(Move.castleKingsSide(Colour.WHITE));
      Chessboard cb = game.getChessboard();
      assertEmptySquare(cb, Square.e1);
      assertPieceAt(cb, Square.g1, PieceType.KING);
      assertEmptySquare(cb, Square.h1);
      assertPieceAt(cb, Square.f1, PieceType.ROOK);
   }

   @Test
   public void kingsCastlingBlack() {
      Game game = Fen.decode("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b KkQq - 0 1");
      game.move(Move.castleKingsSide(Colour.BLACK));
      Chessboard cb = game.getChessboard();
      assertEmptySquare(cb, Square.e8);
      assertPieceAt(cb, Square.g8, PieceType.KING);
      assertEmptySquare(cb, Square.h8);
      assertPieceAt(cb, Square.f8, PieceType.ROOK);
   }

   @Test
   public void queensCastlingWhite() {
      Game game = Fen.decode("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KkQq - 0 1");
      game.move(Move.castleQueensSide(Colour.WHITE));
      Chessboard cb = game.getChessboard();
      assertEmptySquare(cb, Square.e1);
      assertPieceAt(cb, Square.c1, PieceType.KING);
      assertEmptySquare(cb, Square.a1);
      assertPieceAt(cb, Square.d1, PieceType.ROOK);
   }

   @Test
   public void queensCastlingBlack() {
      Game game = Fen.decode("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b KkQq - 0 1");
      game.move(Move.castleQueensSide(Colour.BLACK));
      Chessboard cb = game.getChessboard();
      assertEmptySquare(cb, Square.e8);
      assertPieceAt(cb, Square.c8, PieceType.KING);
      assertEmptySquare(cb, Square.a8);
      assertPieceAt(cb, Square.d8, PieceType.ROOK);
   }

   @Test
   public void capture() {
      Game game = Fen.decode("8/8/8/3p4/2P5/8/8/8 w - - 0 1");
      game.move(new Move(PieceType.PAWN, Colour.WHITE, Square.c4, Square.d5, PieceType.PAWN));
      Chessboard cb = game.getChessboard();
      assertEmptySquare(cb, Square.c4);
      assertPieceAt(cb, Square.d5, PieceType.PAWN);
      assertTrue(cb.getPieces(Colour.BLACK).get(PieceType.PAWN).getBitBoard().getBitSet().isEmpty());
   }

   @Test
   public void promotion() {
      Game game = Fen.decode("8/8/8/8/8/8/5p2/8 b - - 0 1");
      Move move = new Move(PieceType.PAWN, Colour.BLACK, Square.f2, Square.f1);
      move.setPromotionPiece(PieceType.QUEEN);
      game.move(move);
      Chessboard cb = game.getChessboard();
      assertEmptySquare(cb, Square.f2);
      assertPieceAt(cb, Square.f1, PieceType.QUEEN);
   }

   private void assertPieceAt(Chessboard cb, Square sq, PieceType expectedPiece) {
      assertEquals(expectedPiece, cb.pieceAt(sq));
   }

   private void assertEmptySquare(Chessboard cb, Square sq) {
      try {
         cb.pieceAt(sq);
         fail("expected exception");
      } catch (IllegalArgumentException x) {
         // ok
      }
   }

}
