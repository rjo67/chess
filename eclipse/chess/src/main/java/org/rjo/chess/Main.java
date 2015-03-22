package org.rjo.chess;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Main {

   public static void main(String[] args) {

      List<Move> captures, checks;
      List<Move> moves;
      // moves = findMoves(new Game(), Colour.WHITE, 1);
      // assertEquals(20, moves.size());

      // moves = findMoves(new Game(), Colour.WHITE, 2);
      // assertEquals(400, moves.size());

      // captures = new ArrayList<>();
      // checks = new ArrayList<>();
      // moves = findMoves(new Game(), Colour.WHITE, 3);
      // assertEquals(8902, moves.size());
      // for (Move move : moves) {
      // if (move.isCapture()) {
      // captures.add(move);
      // }
      // if (move.isCheck()) {
      // checks.add(move);
      // }
      // }
      // assertEquals(34, captures.size());
      // assertEquals(12, checks.size());

      captures = new ArrayList<>();
      checks = new ArrayList<>();
      moves = findMoves(new Game(), Colour.WHITE, 4, true);
      assertEquals(197281, moves.size());
      for (Move move : moves) {
         if (move.isCapture()) {
            captures.add(move);
         }
         if (move.isCheck()) {
            checks.add(move);
         }
      }
      assertEquals(1576, captures.size());
      assertEquals(469, checks.size());

      // moves = findMoves(new Game(), Colour.WHITE, 5);
      // assertEquals(4865609, moves.size());
   }

   public static List<Move> findMoves(Game game, Colour sideToMove, int depth, boolean debug) {
      List<Move> moves = findMoves(game, sideToMove, depth, new ArrayDeque<Move>(), new ArrayList<Move>(), debug);
      return moves;
   }

   public static List<Move> findMoves(Game game, Colour sideToMove, int depth, Deque<Move> movesSoFar,
         List<Move> totalMoves, boolean debug) {
      if (depth == 0) {
         return new ArrayList<Move>();
      }
      // movesAtThisLevel and movesSoFar are only used for "logging"
      List<Move> movesAtThisLevel = new ArrayList<>();
      for (Move move : game.findMoves(sideToMove)) {
         if (debug) {
            movesSoFar.addLast(move);
         }
         game.move(move);

         List<Move> movesFromThisPosn = findMoves(game, Colour.oppositeColour(sideToMove), depth - 1, movesSoFar,
               totalMoves, debug);
         if (movesFromThisPosn.isEmpty()) {
            totalMoves.add(move);
            if (debug) {
               movesAtThisLevel.add(move);
            }
         }

         game.unmove(move);
         if (debug) {
            movesSoFar.removeLast();
         }
      }

      if (debug) {
         if (!movesAtThisLevel.isEmpty()) {
            boolean check = false;
            boolean capture = false;
            if (!movesSoFar.isEmpty()) {
               check = movesSoFar.peekLast().isCheck();
            }
            if (!movesSoFar.isEmpty()) {
               capture = movesSoFar.peekLast().isCapture();
            }
            System.out.println((check ? "CHECK" : "") + (capture ? "CAPTURE" : "") + " moves: " + movesSoFar + " -> "
                  + movesAtThisLevel.size() + ":" + movesAtThisLevel);
         }
      }
      return totalMoves;

   }
}
