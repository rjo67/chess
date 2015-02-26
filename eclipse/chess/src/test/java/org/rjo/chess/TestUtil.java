package org.rjo.chess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class TestUtil {

   private TestUtil() {
   }

   public static void checkMoves(List<Move> moves, Set<String> requiredMoves) {
      // clone moves so as to avoid losing the move list for later tests
      List<Move> moveClone = new ArrayList<>(moves);
      Iterator<Move> iter = moveClone.iterator();
      while (iter.hasNext()) {
         Move m = iter.next();
         if (requiredMoves.contains(m.toString())) {
            requiredMoves.remove(m.toString());
            iter.remove();
         }
      }
      assertTrue("not all required moves found: " + requiredMoves
            + (moveClone.isEmpty() ? "" : ". Input-Moves not processed: " + moveClone), requiredMoves.isEmpty());
      // all required moves found but still some input moves left over?
      assertTrue("unexpected moves found: " + moveClone, moveClone.isEmpty());
   }
}
