package org.rjo.chess;

import org.junit.Test;
import org.rjo.chess.pieces.King;
import org.rjo.chess.pieces.Pawn;
import org.rjo.chess.pieces.Queen;

import static org.junit.Assert.assertEquals;

public class EvaluationTest {

   @Test
   public void pawnPieceValue() {
      Pawn whitePawns = new Pawn(Colour.WHITE, Square.c2, Square.d4);
      Pawn blackPawns = new Pawn(Colour.BLACK, Square.c7, Square.d5);

      assertEquals(230, whitePawns.calculatePieceSquareValue());
      assertEquals(230, blackPawns.calculatePieceSquareValue());

      for (Square sq : Square.values()) {
         Pawn p = new Pawn(Colour.BLACK, sq);
         System.out.println(sq + ":" + p.calculatePieceSquareValue());

      }
   }

   @Test
   public void queenPieceValue() {
      Queen white = new Queen(Colour.WHITE, Square.c2);
      Queen black = new Queen(Colour.BLACK, Square.a6);

      assertEquals(905, white.calculatePieceSquareValue());
      assertEquals(890, black.calculatePieceSquareValue());
      for (Square sq : Square.values()) {
         Queen p = new Queen(Colour.BLACK, sq);
         System.out.println(sq + ":" + p.calculatePieceSquareValue());

      }
   }

   @Test
   public void kingPieceValue() {
      for (Square sq : Square.values()) {
         King p = new King(Colour.BLACK, sq);
         System.out.println(sq + ":" + p.calculatePieceSquareValue());

      }
   }

   @Test
   public void mateInOne() {
      Game game = Fen.decode("r3k3/pppppp2/8/8/8/8/8/4K2R w - - 0 2");
      SearchStrategy strat = new AlphaBeta();
      MoveInfo m = strat.findMove(game);
      assertEquals("Rh1-h8+", m.getMove().toString());
   }

   @Test
   public void test() {
      Game game = Fen.decode("r1b1kb2/ppppqpp1/2n4r/4P3/2B4P/2P2Q2/PP3PP1/RN2R1K1 b q - 0 2");
      SearchStrategy strat = new AlphaBeta();
      MoveInfo m = strat.findMove(game);
      assertEquals("Rh1-h8+", m.getMove().toString());
   }

   @Test
   public void mateInTwo() {
      Game game = Fen.decode("4k3/3ppp2/5n2/6KR/8/8/8/8 w - - 0 2");
      SearchStrategy strat = new AlphaBeta();
      MoveInfo m = strat.findMove(game);
      assertEquals("Rh5-h8+", m.getMove().toString());
   }

   @Test
   public void opponentMateInOne() {
      Game game = Fen.decode("4k3/8/8/8/8/4P1PP/3PrPPP/7K w - - 0 1 ");
      SearchStrategy strat = new AlphaBeta();
      MoveInfo m = strat.findMove(game);
      assertEquals("Rh5-h8+", m.getMove().toString());
   }

   @Test
   public void mateInOneBetterThanMateInTwo() {
      Game game = Fen.decode("r1r3k1/5ppp/2R5/2R5/8/8/1B6/3K2Q1 w - - 0 15");
      SearchStrategy strat = new AlphaBeta();
      MoveInfo m = strat.findMove(game);
      assertEquals("Qg1xg7+", m.getMove().toString());
   }
}
