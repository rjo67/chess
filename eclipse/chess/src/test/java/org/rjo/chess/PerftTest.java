package org.rjo.chess;

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
   public void kiwiPete() {
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
      checkAnswer(16, moves);
      // TODO: checks not yet implemented. checkAnswer(2, MoveUtil.getChecks(moves));
      checkAnswer(1, MoveUtil.getCaptures(moves));

   }

   @Test
   public void posn5() {
      Game game = Fen.decode("rnbqkb1r/pp1p1ppp/2p5/4P3/2B5/8/PPP1NnPP/RNBQK2R w KQkq - 0 6");
      List<Move> moves = game.findMoves(Colour.WHITE);
      checkAnswer(42, moves);
      // TODO: checks not yet implemented. checkAnswer(2, MoveUtil.getChecks(moves));
      checkAnswer(3, MoveUtil.getCaptures(moves));
   }

   @Test
   public void posn6() {
      Game game = Fen.decode("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10");
      List<Move> moves = game.findMoves(Colour.WHITE);
      checkAnswer(46, moves);
      // TODO: checks not yet implemented. checkAnswer(1, MoveUtil.getChecks(moves));
      checkAnswer(4, MoveUtil.getCaptures(moves));
   }

   private void checkAnswer(int expectedNbrOfMoves, List<Move> moveList) {
      assertEquals("got moves " + moveList, expectedNbrOfMoves, moveList.size());
   }
}
