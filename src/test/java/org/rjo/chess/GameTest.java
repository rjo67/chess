package org.rjo.chess;

import java.util.List;

import org.junit.Test;
import org.rjo.chess.pieces.PieceType;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GameTest {

   @Test
   public void colourTest() {
      // increase coverage of enum
      assertArrayEquals(new Colour[] { Colour.WHITE, Colour.BLACK }, Colour.values());
      Colour.valueOf("WHITE");
   }

   @Test
   public void castlingRightsTest() {
      // increase coverage of enum
      assertArrayEquals(new CastlingRights[] { CastlingRights.QUEENS_SIDE, CastlingRights.KINGS_SIDE },
            CastlingRights.values());
      CastlingRights.valueOf("QUEENS_SIDE");
   }

   @Test(expected = IllegalArgumentException.class)
   public void wrongSideToMove() {
      Game game = new Game();
      game.move(new Move(PieceType.PAWN, Colour.BLACK, Square.b7, Square.b5));
   }

   @Test(expected = IllegalArgumentException.class)
   public void noPieceOnStartSquare() {
      Game game = new Game();
      game.move(new Move(PieceType.PAWN, Colour.WHITE, Square.b5, Square.b6));
   }

   @Test
   public void testMove() {
      Game game = new Game();
      game.move(new Move(PieceType.PAWN, Colour.WHITE, Square.b2, Square.b4));
      Chessboard cb = game.getChessboard();

      assertEmptySquare(cb, Square.b2);
      assertPieceAt(cb, Square.b4, PieceType.PAWN);
   }

   @Test
   public void moveNbr() {
      Game game = new Game();
      game.move(new Move(PieceType.PAWN, Colour.WHITE, Square.b2, Square.b4));
      assertEquals(1, game.getMoveNumber());
      game.move(new Move(PieceType.KNIGHT, Colour.BLACK, Square.b8, Square.a6));
      assertEquals(2, game.getMoveNumber());
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
      assertFalse(game.canCastle(Colour.WHITE, CastlingRights.KINGS_SIDE));
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
      assertFalse(game.canCastle(Colour.BLACK, CastlingRights.KINGS_SIDE));
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
      assertFalse(game.canCastle(Colour.WHITE, CastlingRights.QUEENS_SIDE));
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
      assertFalse(game.canCastle(Colour.BLACK, CastlingRights.QUEENS_SIDE));
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

   @Test(expected = IllegalArgumentException.class)
   public void noPieceToCapture() {
      Game game = Fen.decode("8/8/8/3p4/2P5/8/8/8 w - - 0 1");
      game.move(new Move(PieceType.PAWN, Colour.WHITE, Square.c4, Square.b5, PieceType.PAWN));
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

   @Test(expected = IllegalArgumentException.class)
   public void unmoveWrongSideToMove() {
      Game game = new Game();
      game.unmove(new Move(PieceType.PAWN, Colour.WHITE, Square.b7, Square.b5));
   }

   @Test
   public void unmoveNbr() {
      Game game = Fen.decode("r1bqkbnr/pppppppp/n7/8/1P6/8/P1PPPPPP/RNBQKBNR w KQkq - 0 2");
      game.unmove(new Move(PieceType.KNIGHT, Colour.BLACK, Square.b8, Square.a6));
      assertEquals(1, game.getMoveNumber());
      game.unmove(new Move(PieceType.PAWN, Colour.WHITE, Square.b2, Square.b4));
      assertEquals(1, game.getMoveNumber());
   }

   @Test(expected = IllegalArgumentException.class)
   public void unmoveNoPieceOnEndSquare() {
      Game game = Fen.decode("8/8/8/8/8/8/5p2/8 w - - 0 1");
      game.unmove(new Move(PieceType.PAWN, Colour.BLACK, Square.f4, Square.f3));
   }

   @Test
   public void testUnmove() {
      Game game = Fen.decode("4k2r/5p2/p7/3r4/8/8/6P1/R3K2R w KkQ - 0 1");
      game.unmove(new Move(PieceType.ROOK, Colour.BLACK, Square.d7, Square.d5));
      Chessboard cb = game.getChessboard();
      assertEmptySquare(cb, Square.d5);
      assertPieceAt(cb, Square.d7, PieceType.ROOK);
   }

   @Test(expected = IllegalArgumentException.class)
   public void illegalUnmoveToNonEmptySquare() {
      Game game = Fen.decode("8/8/8/3p4/2P5/2P5/8/8 b - - 0 1");
      game.unmove(new Move(PieceType.PAWN, Colour.WHITE, Square.c3, Square.c4));
   }

   @Test
   public void unmoveKingsCastlingWhite() {
      Game game = Fen.decode("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w kKQq - 0 1");
      Move move = Move.castleKingsSide(Colour.WHITE);
      game.move(move);
      game.unmove(move);
      Chessboard cb = game.getChessboard();
      assertEmptySquare(cb, Square.f1);
      assertEmptySquare(cb, Square.g1);
      assertPieceAt(cb, Square.e1, PieceType.KING);
      assertPieceAt(cb, Square.h1, PieceType.ROOK);
      assertTrue(game.canCastle(Colour.WHITE, CastlingRights.KINGS_SIDE));
   }

   @Test
   public void unmoveKingsCastlingBlack() {
      Game game = Fen.decode("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R4RK1 b kQq - 0 1");
      Move move = Move.castleKingsSide(Colour.BLACK);
      game.move(move);
      game.unmove(move);
      Chessboard cb = game.getChessboard();
      assertEmptySquare(cb, Square.f8);
      assertEmptySquare(cb, Square.g8);
      assertPieceAt(cb, Square.e8, PieceType.KING);
      assertPieceAt(cb, Square.h8, PieceType.ROOK);
      assertTrue(game.canCastle(Colour.BLACK, CastlingRights.KINGS_SIDE));
   }

   @Test
   public void unmoveQueensCastlingWhite() {
      Game game = Fen.decode("2kr4/pppppppp/8/8/8/8/PPPPPPPP/R3K3 w Q - 0 1");
      Move move = Move.castleQueensSide(Colour.WHITE);
      game.move(move);
      game.unmove(move);
      Chessboard cb = game.getChessboard();
      assertEmptySquare(cb, Square.c1);
      assertEmptySquare(cb, Square.d1);
      assertPieceAt(cb, Square.a1, PieceType.ROOK);
      assertPieceAt(cb, Square.e1, PieceType.KING);
      assertTrue(game.canCastle(Colour.WHITE, CastlingRights.QUEENS_SIDE));
   }

   @Test
   public void unmoveQueensCastlingBlack() {
      Game game = Fen.decode("r3k3/pppppppp/8/8/8/8/PPPPPPPP/2KR4 b q - 0 1");
      Move move = Move.castleQueensSide(Colour.BLACK);
      game.move(move);
      game.unmove(move);
      Chessboard cb = game.getChessboard();
      assertEmptySquare(cb, Square.c8);
      assertEmptySquare(cb, Square.d8);
      assertPieceAt(cb, Square.a8, PieceType.ROOK);
      assertPieceAt(cb, Square.e8, PieceType.KING);
      assertTrue(game.canCastle(Colour.BLACK, CastlingRights.QUEENS_SIDE));
   }

   @Test
   public void unmoveCapture() {
      Game game = Fen.decode("8/8/8/3P4/8/8/8/8 b - - 0 1");
      game.unmove(new Move(PieceType.PAWN, Colour.WHITE, Square.c4, Square.d5, PieceType.PAWN));
      Chessboard cb = game.getChessboard();
      assertPieceAt(cb, Square.c4, PieceType.PAWN);
      assertTrue(cb.getPieces(Colour.BLACK).get(PieceType.PAWN).pieceAt(Square.d5));
   }

   @Test(expected = IllegalArgumentException.class)
   public void unmoveNoPieceToCapture() {
      Game game = Fen.decode("8/8/8/3P4/8/8/8/8 b - - 0 1");
      game.unmove(new Move(PieceType.PAWN, Colour.WHITE, Square.c4, Square.b5, PieceType.PAWN));
   }

   @Test
   public void unmovePromotion() {
      Game game = Fen.decode("8/8/8/8/8/8/8/5q2 w - - 0 1");
      Move move = new Move(PieceType.PAWN, Colour.BLACK, Square.f2, Square.f1);
      move.setPromotionPiece(PieceType.QUEEN);
      game.unmove(move);
      Chessboard cb = game.getChessboard();
      assertEmptySquare(cb, Square.f1);
      assertPieceAt(cb, Square.f2, PieceType.PAWN);
   }

   @Test
   public void castlingNotAllowedAfterRookCapture() {
      // this is 'posn2' from PerftTest
      // sequence of moves: Ne5xg6, b4-b3, Ng6xh8. O-O is then not allowed...
      Game game = Fen.decode("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 4");
      game.move(new Move(PieceType.KNIGHT, Colour.WHITE, Square.e5, Square.g6, PieceType.PAWN));
      game.move(new Move(PieceType.PAWN, Colour.BLACK, Square.b4, Square.b3));
      game.move(new Move(PieceType.KNIGHT, Colour.WHITE, Square.g6, Square.h8, PieceType.ROOK));
      List<Move> moves = game.findMoves(Colour.BLACK);
      assertMovePresent(moves, "O-O-O");
      assertMoveNotPresent(moves, "O-O");
   }

   @Test
   public void eval() {
      Game game = Fen.decode("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KkQq - 0 1");
      System.out.println(game.evaluate());
      game = Fen.decode("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R b KkQq - 0 1");
      System.out.println(game.evaluate());
   }

   private void assertMoveNotPresent(List<Move> moves, String requiredMove) {
      for (Move move : moves) {
         if (requiredMove.equals(move.toString())) {
            throw new AssertionError("move '" + requiredMove + "' was found in " + moves);
         }
      }
   }

   private void assertMovePresent(List<Move> moves, String requiredMove) {
      boolean found = false;
      for (Move move : moves) {
         if (requiredMove.equals(move.toString())) {
            found = true;
            break;
         }
      }
      if (!found) {
         throw new AssertionError("move '" + requiredMove + "' not found in " + moves);
      }
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