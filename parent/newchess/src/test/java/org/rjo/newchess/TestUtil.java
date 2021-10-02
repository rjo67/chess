package org.rjo.newchess;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.rjo.newchess.move.Move;
import org.rjo.newchess.piece.PieceType;

public class TestUtil {

   public final static Predicate<Move> NOOP_FILTER = (move -> true);
   public final static Predicate<Move> KING_FILTER = (move -> move.getMovingPiece() == PieceType.KING);
   public final static Predicate<Move> PAWN_FILTER = (move -> move.getMovingPiece() == PieceType.PAWN);
   public final static Predicate<Move> KNIGHT_FILTER = (move -> move.getMovingPiece() == PieceType.KNIGHT);

   private TestUtil() {
   }

   public static void checkMoves(List<Move> moves, String... requiredMoves) {
      checkMoves(moves, NOOP_FILTER, requiredMoves);
   }

   public static void checkMoves(List<Move> moves, Predicate<Move> moveFilter, String... requiredMoves) {
      checkMoves(moves, new HashSet<>(Arrays.asList(requiredMoves)), moveFilter);
   }

   /**
    * Checks that all <code>moves</code> are present in <code>requiredMoves</code>,
    * and there aren't any superfluous moves in either collection.
    *
    * @param moves         the moves found
    * @param requiredMoves the required moves
    * @param moveFilter    an optional predicate to further filter
    *                      <code>moves</code>, e.g. in order to remove any king
    *                      moves if we're only interested in pawn moves. Will be
    *                      negated in use.
    */
   private static void checkMoves(List<Move> moves, Set<String> requiredMoves, Predicate<Move> moveFilter) {

      // clone moves so as to avoid losing the move list for later tests
      List<Move> moveClone = new ArrayList<>(moves);
      Iterator<Move> iter = moveClone.iterator();
      while (iter.hasNext()) {
         Move m = iter.next();
         if (requiredMoves.contains(m.toString())) {
            requiredMoves.remove(m.toString());
            iter.remove();
         } else if (moveFilter.negate().test(m)) {
            iter.remove();
         }
      }
      assertEquals(0, requiredMoves.size(),
            "not all required moves found, expected: " + requiredMoves + (moveClone.isEmpty() ? "" : ". Input-Moves not processed: " + moveClone));
      // all required moves found but still some input moves left over?
      assertEquals(0, moveClone.size(), "following unexpected moves found: " + moveClone);
   }
}
