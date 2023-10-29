package org.rjo.newchess;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Position.PieceSquareInfo;
import org.rjo.newchess.move.IMove;
import org.rjo.newchess.move.Move;
import org.rjo.newchess.move.MovingPieceDecorator;
import org.rjo.newchess.piece.Pieces;

public class TestUtil {

   public final static Predicate<IMove> NOOP_FILTER = move -> true;
   public final static Predicate<IMove> KING_FILTER = move -> Pieces.isKing(move.getMovingPiece());
   public final static Predicate<IMove> PAWN_FILTER = move -> Pieces.isPawn(move.getMovingPiece());
   public final static Predicate<IMove> KNIGHT_FILTER = move -> Pieces.isKnight(move.getMovingPiece());
   public final static Predicate<IMove> BISHOP_FILTER = move -> Pieces.isBishop(move.getMovingPiece());
   public final static Predicate<IMove> ROOK_FILTER = move -> Pieces.isRook(move.getMovingPiece());
   public final static Predicate<IMove> QUEEN_FILTER = move -> Pieces.isQueen(move.getMovingPiece());
   public static final Predicate<IMove> ONLY_CHECKS = move -> move.isCheck();

   private TestUtil() {
   }

   public static IMove createCapture(Square origin, byte originPiece, Square target, byte targetPiece) {
      return new MovingPieceDecorator(Move.createCapture(origin, target, targetPiece), originPiece);
   }

   public static IMove createMove(Square origin, byte originPiece, Square target) {
      return new MovingPieceDecorator(Move.createMove(origin, target), originPiece);
   }

   public static boolean squareIsCheckSquare(Square square, List<PieceSquareInfo> checkSquares) {
      for (PieceSquareInfo ci : checkSquares) {
         if (ci.square() == square.index()) { return true; }
      }
      return false;
   }

   public static void checkMoves(List<IMove> moves, String... requiredMoves) {
      checkMoves(moves, NOOP_FILTER, requiredMoves);
   }

   public static void checkMoves(List<IMove> moves, Predicate<IMove> moveFilter, String... requiredMoves) {
      checkMoves(moves, new HashSet<>(Arrays.asList(requiredMoves)), moveFilter);
   }

   /**
    * Checks that all <code>moves</code> are present in <code>requiredMoves</code>, and there aren't any superfluous moves in either
    * collection.
    *
    * @param moves         the moves found
    * @param requiredMoves the required moves
    * @param moveFilter    an optional predicate to further filter <code>moves</code>, e.g. in order to just concentrate on pawn moves
    */
   private static void checkMoves(List<IMove> moves, Set<String> requiredMoves, Predicate<IMove> moveFilter) {

      // clone moves so as to avoid losing the move list for later tests
      List<IMove> moveClone = new ArrayList<>(moves);
      Iterator<IMove> iter = moveClone.iterator();
      while (iter.hasNext()) {
         IMove m = iter.next();
         if (requiredMoves.contains(m.toString())) {
            requiredMoves.remove(m.toString());
            iter.remove();
         } else if (!moveFilter.test(m)) { iter.remove(); }
      }
      if (!requiredMoves.isEmpty()) {
         fail("not all required moves found, expected: " + requiredMoves + (moveClone.isEmpty() ? "" : ". Input-Moves not processed: " + moveClone));
      }
      // all required moves found but still some input moves left over?
      if (!moveClone.isEmpty()) { fail("found unexpected moves: " + moveClone); }
   }
}
