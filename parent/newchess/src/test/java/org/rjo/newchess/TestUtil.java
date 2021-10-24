package org.rjo.newchess;

import static org.junit.jupiter.api.Assertions.fail;

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
   public final static Predicate<Move> BISHOP_FILTER = (move -> move.getMovingPiece() == PieceType.BISHOP);
   public final static Predicate<Move> ROOK_FILTER = (move -> move.getMovingPiece() == PieceType.ROOK);
   public final static Predicate<Move> QUEEN_FILTER = (move -> move.getMovingPiece() == PieceType.QUEEN);
   public static final Predicate<Move> ONLY_CHECKS = (move -> move.isCheck());

   private TestUtil() {
   }

   public static void checkMoves(List<Move> moves, String... requiredMoves) {
      checkMoves(moves, NOOP_FILTER, requiredMoves);
   }

   public static void checkMoves(List<Move> moves, Predicate<Move> moveFilter, String... requiredMoves) {
      checkMoves(moves, new HashSet<>(Arrays.asList(requiredMoves)), moveFilter);
   }

   /**
    * Checks that all <code>moves</code> are present in <code>requiredMoves</code>, and there aren't any superfluous moves
    * in either collection.
    *
    * @param moves         the moves found
    * @param requiredMoves the required moves
    * @param moveFilter    an optional predicate to further filter <code>moves</code>, e.g. in order to just concentrate on
    *                      pawn moves
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
         } else if (!moveFilter.test(m)) { iter.remove(); }
      }
      if (!requiredMoves.isEmpty()) {
         fail("not all required moves found, expected: " + requiredMoves + (moveClone.isEmpty() ? "" : ". Input-Moves not processed: " + moveClone));
      }
      // all required moves found but still some input moves left over?
      if (!moveClone.isEmpty()) { fail("found these unexpected moves: " + moveClone); }
   }
}
