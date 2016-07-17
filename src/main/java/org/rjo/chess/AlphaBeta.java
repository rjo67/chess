package org.rjo.chess;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AlphaBeta implements SearchStrategy {
   private static final int MIN_INT = -99999;
   private static final int MAX_INT = -MIN_INT;
   private static final int MAX_DEPTH = 4;

   private static final Logger LOG = LogManager.getLogger(AlphaBeta.class);

   private int nbrNodesEvaluated;

   private PrintStream outputStream;

   public AlphaBeta() {
      this.outputStream = System.out;
   }

   public AlphaBeta(PrintStream out) {
      this.outputStream = out;
   }

   @Override
   public String toString() {
      return "AlphaBeta, depth=" + MAX_DEPTH;
   }

   @Override
   public MoveInfo findMove(Game game) {
      final int depth = MAX_DEPTH;
      nbrNodesEvaluated = 0;
      // always store at least one move (even if all moves are equally bad i.e. return MIN_INT)
      int max = MIN_INT - 1;
      MoveInfo moveInfo = new MoveInfo();
      long overallStartTime = System.currentTimeMillis();
      List<Move> moves = game.findMoves(game.getSideToMove());
      for (Move move : moves) {
         boolean newMoveFound = false;
         long startTime = System.currentTimeMillis();
         Line line = new Line();
         game.move(move);
         LOG.debug("           ".substring(depth) + " " + depth + " " + move);
         int score = -alphaBeta(MIN_INT, MAX_INT, depth - 1, game, line);
         if (score > max) {
            max = score;
            moveInfo.setMove(move);
            // add current move before storing line
            line.addMove(move);
            moveInfo.setLine(line);
            newMoveFound = true;
            outputStream.print("info pv ");
            for (Move m : moveInfo.getLine().getMoves()) {
               outputStream.print(m.toUCIString() + " ");
            }
            outputStream.println("score cp " + score);
         }
         game.unmove(move);
         // show current move even if not best (not uci relevant)
         if (!newMoveFound) {
            LOG.debug(String.format("(%7s, %8d, %8d, %5dms)%s", move, score, nbrNodesEvaluated,
                  (System.currentTimeMillis() - startTime), ""));
         }
      }
      long overallStopTime = System.currentTimeMillis();
      outputStream.println(String.format("nodes: %7d, time: %7.2fs, %9.2f nodes/s", nbrNodesEvaluated,
            (overallStopTime - overallStartTime) / 1000.0,
            (1.0 * nbrNodesEvaluated / (overallStopTime - overallStartTime)) * 1000));
      return moveInfo;
   }

   private int alphaBeta(int alpha, int beta, int depth, Game game, Line currentLine) {
      if (depth == 0) {
         nbrNodesEvaluated++;
         currentLine.clearMoves();
         return game.evaluate();
      }
      List<Move> moves = game.findMoves(game.getSideToMove());
      for (Move move : moves) {
         Line line = new Line();
         game.move(move);
         LOG.debug("           ".substring(depth) + " " + depth + " " + move);
         int score = -alphaBeta(-beta, -alpha, depth - 1, game, line);
         LOG.debug("           ".substring(depth) + " " + depth + " " + move + " " + score + " -- " + line);
         game.unmove(move);
         if (score >= beta) {
            return beta; // fail hard beta-cutoff
         }
         if (score > alpha) {
            alpha = score;
            currentLine.storeLine(line);
            currentLine.addMove(move);
         }
      }
      if (moves.isEmpty()) {
         // a mate in two is better than a mate in 3
         return alpha + ((20 - depth) * 3);
      }
      return alpha;
   }

   // ------------------------------------------------------------------

   // no longer used

   // ------------------------------------------------------------------

   private int alphaBetaMax(int alpha, int beta, int depth, Game game, Line currentLine) {
      Line line = new Line();
      if (depth == 0) {
         nbrNodesEvaluated++;
         currentLine.clearMoves();
         return game.evaluate();
      }
      List<Move> moves = game.findMoves(game.getSideToMove());
      for (Move move : moves) {
         game.move(move);
         int score = alphaBetaMin(alpha, beta, depth - 1, game, line);
         game.unmove(move);
         if (score >= beta) {
            return beta; // fail hard beta-cutoff
         }
         if (score > alpha) {
            alpha = score; // alpha acts like max in MiniMax
            currentLine.storeLine(line);
            currentLine.addMove(move);
         }
      }
      // if (moves.isEmpty()) {
      // // a mate in two is better than a mate in 3
      // return alpha + ((20 - depth) * 3);
      // }
      return alpha;
   }

   private int alphaBetaMin(int alpha, int beta, int depth, Game game, Line currentLine) {
      Line line = new Line();
      if (depth == 0) {
         nbrNodesEvaluated++;
         currentLine.clearMoves();
         return -game.evaluate();
      }
      List<Move> moves = game.findMoves(game.getSideToMove());
      for (Move move : moves) {
         game.move(move);
         int score = alphaBetaMax(alpha, beta, depth - 1, game, line);
         game.unmove(move);
         if (score <= alpha) {
            return alpha; // fail hard alpha-cutoff
         }
         if (score < beta) {
            beta = score; // beta acts like min in MiniMax
            currentLine.storeLine(line);
            currentLine.addMove(move);
         }
      }
      // if this side has no moves, store this line as best
      if (moves.isEmpty()) {
         // a mate in two is better than a mate in 3
         // TODO? return beta - depth * 3;
      }
      return beta;
   }

   class Line {
      private Deque<Move> moves;

      public Line() {
         this.moves = new ArrayDeque<>();
      }

      public void storeLine(Line line) {
         this.moves = line.moves;
      }

      public void addMove(Move m) {
         moves.addFirst(m);
      }

      public void clearMoves() {
         moves = new ArrayDeque<>();
      }

      public Deque<Move> getMoves() {
         return moves;
      }

      @Override
      public String toString() {
         return moves.toString();
      }
   }
}
