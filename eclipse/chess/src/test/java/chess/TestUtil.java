package chess;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class TestUtil {

   private TestUtil() {
   }

   public static void checkMoves(List<Move> moves, Set<String> requiredMoves) {
      Iterator<Move> iter = moves.iterator();
      while (iter.hasNext()) {
         Move m = iter.next();
         if (requiredMoves.contains(m.toString())) {
            requiredMoves.remove(m.toString());
            iter.remove();
         }
      }
      assertTrue("not all required moves found: " + requiredMoves
            + (moves.isEmpty() ? "" : ". Input-Moves not processed: " + moves), requiredMoves.isEmpty());
      // all required moves found but still some input moves left over?
      assertTrue("unexpected moves found: " + moves, moves.isEmpty());
   }
}
