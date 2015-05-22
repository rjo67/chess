package org.rjo.chess;

public class MoveEval {
   public MoveEval(int value, Move move) {
      this.value = value;
      this.move = move;
   }

   private int value;
   private Move move;

   public int getValue() {
      return value;
   }

   public Move getMove() {
      return move;
   }

   @Override
   public String toString() {
      return move.toString() + ":" + value;
   }
}
