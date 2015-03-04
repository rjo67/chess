package org.rjo.chess;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Perft, (PERFormance Test, move path enumeration) is a debugging function to walk the move generation tree of strictly
 * legal moves to count all the leaf nodes of a certain depth,
 * 
 * @author rich
 * @see http://chessprogramming.wikispaces.com/Perft
 * @see http://chessprogramming.wikispaces.com/Perft+Results
 */
public class PerftTest {

   // these tests only go to 1ply at the moment

   @Test
   public void initialPosition() {
      Game game = new Game();
      checkAnswer(20, game.findMoves(Colour.WHITE));
   }

   @Test
   public void posn2() {
      Game game = Fen.decode("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 0");
      List<Move> moves = game.findMoves(Colour.WHITE);
      checkAnswer(48, moves);
      checkAnswer(0, MoveUtil.getChecks(moves));
      checkAnswer(8, MoveUtil.getCaptures(moves));
   }

   @Test
   public void posn3() {
      Game game = Fen.decode("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 0");
      List<Move> moves = game.findMoves(Colour.WHITE);
      TestUtil.checkMoves(
            moves,
            new HashSet<>(Arrays.asList("Ka5-a6", "Ka5-a4", "g2-g3+", "g2-g4", "e2-e3", "e2-e4", "b5-b6", "Rb4-b3",
                  "Rb4-b2", "Rb4-b1", "Rb4-a4", "Rb4-c4", "Rb4-d4", "Rb4-e4", "Rb4xf4+")));
      checkAnswer(2, MoveUtil.getChecks(moves));
      checkAnswer(1, MoveUtil.getCaptures(moves));

   }

   @Test
   public void posn5() {
      Game game = Fen.decode("rnbqkb1r/pp1p1ppp/2p5/4P3/2B5/8/PPP1NnPP/RNBQK2R w KQkq - 0 6");
      List<Move> moves = game.findMoves(Colour.WHITE);
      checkAnswer(42, moves);
      checkAnswer(2, MoveUtil.getChecks(moves));
      checkAnswer(3, MoveUtil.getCaptures(moves));
   }

   @Test
   public void posn6() {
      Game game = Fen.decode("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10");
      List<Move> moves = game.findMoves(Colour.WHITE);
      checkAnswer(46, moves);
      checkAnswer(1, MoveUtil.getChecks(moves));
      checkAnswer(4, MoveUtil.getCaptures(moves));
   }

   // https://sites.google.com/site/numptychess/perft/position-2
   @Test
   public void numpty2() {
      Game game = Fen.decode("8/p7/8/1P6/K1k3p1/6P1/7P/8 w - - 0 10");
      List<Move> moves = game.findMoves(Colour.WHITE);
      checkAnswer(5, moves);
      checkAnswer(0, MoveUtil.getChecks(moves));
      checkAnswer(0, MoveUtil.getCaptures(moves));
   }

   // https://sites.google.com/site/numptychess/perft/position-3
   @Test
   public void numpty3() {
      Game game = Fen.decode("r3k2r/p6p/8/B7/1pp1p3/3b4/P6P/R3K2R w KQkq - 0 10");
      List<Move> moves = game.findMoves(Colour.WHITE);
      TestUtil.checkMoves(
            moves,
            new HashSet<>(Arrays.asList("Ra1-b1", "Ra1-c1", "Ra1-d1", "Ke1-f2", "Ke1-d2", "Ke1-d1", "O-O-O", "Rh1-g1",
                  "Rh1-f1", "a2-a4", "a2-a3", "h2-h4", "h2-h3", "Ba5-b6", "Ba5-c7", "Ba5-d8", "Ba5xb4")));
      checkAnswer(0, MoveUtil.getChecks(moves));
      checkAnswer(1, MoveUtil.getCaptures(moves));
   }

   // https://sites.google.com/site/numptychess/perft/position-4
   @Test
   public void numpty4() {
      Game game = Fen.decode("8/5p2/8/2k3P1/p3K3/8/1P6/8 b - - 0 10");
      List<Move> moves = game.findMoves(Colour.WHITE);
      checkAnswer(9, moves);
      checkAnswer(1, MoveUtil.getChecks(moves));
      checkAnswer(0, MoveUtil.getCaptures(moves));
   }

   // https://sites.google.com/site/numptychess/perft/position-5
   @Test
   public void numpty5() {
      Game game = Fen.decode("r3k2r/pb3p2/5npp/n2p4/1p1PPB2/6P1/P2N1PBP/R3K2R b KQkq - 0 10");
      List<Move> moves = game.findMoves(Colour.BLACK);
      TestUtil.checkMoves(
            moves,
            new HashSet<>(Arrays.asList("b4-b3", "Na5-b3", "Na5-c4", "Na5-c6", "d5xe4", "Nf6xe4", "Nf6-g4", "Nf6-h5",
                  "Nf6-d7", "Nf6-h7", "Nf6-g8", "g6-g5", "h6-h5", "a7-a6", "Bb7-c8", "Bb7-a6", "Bb7-c6", "Ra8-b8",
                  "Ra8-c8", "Ra8-d8", "Ke8-d7", "Ke8-e7", "Ke8-d8", "Ke8-f8", "O-O", "O-O-O", "Rh8-h7", "Rh8-g8",
                  "Rh8-f8")));
      checkAnswer(0, MoveUtil.getChecks(moves));
      checkAnswer(2, MoveUtil.getCaptures(moves));
   }

   /**
    * various discovered checks.
    * Pawn move - discovered check from rook.
    * King move - discovered check from bishop.
    * Pawn capture - discovered check from queen.
    */
   @Test
   public void discoveredCheck() {
      Game game = Fen.decode("8/8/8/2k3PR/8/1p2K3/2P2B2/2Q5 w - - 0 10");
      List<Move> moves = game.findMoves(Colour.WHITE);
      TestUtil.checkMoves(
            moves,
            new HashSet<>(Arrays.asList("Rh5-h6", "Rh5-h7", "Rh5-h8", "Rh5-h4", "Rh5-h3", "Rh5-h2", "Rh5-h1", "Bf2-e1",
                  "Bf2-g1", "Bf2-g3", "Bf2-h4", "Ke3-d3+", "Ke3-d2+", "Ke3-e2+", "Ke3-e4+", "Ke3-f3+", "Ke3-f4+",
                  "c2xb3+", "c2-c4", "c2-c3", "g5-g6+", "Qc1-b1", "Qc1-a1", "Qc1-d1", "Qc1-b2", "Qc1-a3+", "Qc1-d1",
                  "Qc1-e1", "Qc1-f1", "Qc1-g1", "Qc1-h1", "Qc1-d2")));
      checkAnswer(9, MoveUtil.getChecks(moves));
      checkAnswer(1, MoveUtil.getCaptures(moves));
   }

   private void checkAnswer(int expectedNbrOfMoves, List<Move> moveList) {
      assertEquals("got moves " + moveList, expectedNbrOfMoves, moveList.size());
   }
}
