package org.rjo.chess;

import java.util.ArrayList;
import java.util.List;

public class MoveUtil {

   private MoveUtil() {

   }

   /**
    * Given a list of moves, returns a list containing the checking moves contained therein.
    * 
    * @param moves
    *           the list of moves
    * @return a list of the checking moves
    */
   public static List<Move> getChecks(List<Move> moves) {
      List<Move> checks = new ArrayList<Move>();
      for (Move move : moves) {
         if (move.isCheck()) {
            checks.add(move);
         }
      }
      return checks;
   }

   /**
    * Given a list of moves, returns a list containing the captures contained therein.
    * 
    * @param moves
    *           the list of moves
    * @return a list of the captures
    */
   public static List<Move> getCaptures(List<Move> moves) {
      List<Move> captures = new ArrayList<Move>();
      for (Move move : moves) {
         if (move.isCapture()) {
            captures.add(move);
         }
      }
      return captures;
   }
}
