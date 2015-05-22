package org.rjo.chess;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class SimpleStrategy implements SearchStrategy {

   private static Random rand = new Random();

   @Override
   public MoveInfo findMove(Game game) {
      MoveInfo moveInfo = new MoveInfo();
      List<Move> computerMoves = game.findMoves(game.getSideToMove());
      if (computerMoves.size() == 0) {
         return null;
      } else {
         List<MoveEval> evalList = new ArrayList<>();
         for (Move move : computerMoves) {
            evalList.add(new MoveEval(game.evaluate(move), move));
         }
         int PLAY_BEST_MOVE = 95; // play 'best' move x% of the time
         evalList.sort(new Comparator<MoveEval>() {
            @Override
            public int compare(MoveEval arg0, MoveEval arg1) {
               return arg1.getValue() - arg0.getValue(); // to sort in 'best-first' order
            }
         });
         System.out.println(evalList);
         for (MoveEval moveEval : evalList) {
            if ((rand.nextInt(100) + 1) <= PLAY_BEST_MOVE) {
               moveInfo.setMove(moveEval.getMove());
               break;
            }
         }
         // if still haven't chosen, take the first one ;-)
         if (moveInfo.getMove() == null) {
            moveInfo.setMove(evalList.get(0).getMove());
         }
      }
      return moveInfo;
   }
}