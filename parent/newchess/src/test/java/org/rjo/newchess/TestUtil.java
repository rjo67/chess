package org.rjo.newchess;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.game.Position.PieceSquareInfo;
import org.rjo.newchess.move.IMove;
import org.rjo.newchess.move.Move;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Piece;
import org.rjo.newchess.piece.Pieces;

public class TestUtil {

   public final static BiFunction<Position, IMove, Boolean> NOOP_FILTER = (p, m) -> true;
   public final static BiFunction<Position, IMove, Boolean> KING_FILTER = (p, m) -> Pieces.isKing(p.pieceAt(m.getOrigin()));
   public final static BiFunction<Position, IMove, Boolean> PAWN_FILTER = (p, m) -> Pieces.isPawn(p.pieceAt(m.getOrigin()));
   public final static BiFunction<Position, IMove, Boolean> KNIGHT_FILTER = (p, m) -> Pieces.isKnight(p.pieceAt(m.getOrigin()));
   public final static BiFunction<Position, IMove, Boolean> BISHOP_FILTER = (p, m) -> Pieces.isBishop(p.pieceAt(m.getOrigin()));
   public final static BiFunction<Position, IMove, Boolean> ROOK_FILTER = (p, m) -> Pieces.isRook(p.pieceAt(m.getOrigin()));
   public final static BiFunction<Position, IMove, Boolean> QUEEN_FILTER = (p, m) -> Pieces.isQueen(p.pieceAt(m.getOrigin()));
   public static final BiFunction<Position, IMove, Boolean> ONLY_CHECKS = (p, m) -> m.isCheck();

   private TestUtil() {
   }

   public static IMove createCapture(Square origin, Square target, byte targetPiece) {
      return Move.createCapture(origin.index(), target.index(), targetPiece);
   }

   public static IMove createMove(Square origin, Square target) {
      return Move.createMove(origin.index(), target.index());
   }

   public static boolean squareIsCheckSquare(Square square, List<PieceSquareInfo> checkSquares) {
      for (PieceSquareInfo ci : checkSquares) {
         if (ci.square() == square.index()) { return true; }
      }
      return false;
   }

   public static void checkMoves(Position p, List<IMove> moves, String... requiredMoves) {
      checkMoves(p, moves, NOOP_FILTER, requiredMoves);
   }

   public static void checkMoves(Position p, List<IMove> moves, BiFunction<Position, IMove, Boolean> moveFilter, String... requiredMoves) {
      checkMoves(p, moves, new HashSet<>(Arrays.asList(requiredMoves)), moveFilter);
   }

   /**
    * Checks that all <code>moves</code> are present in <code>requiredMoves</code>, and there aren't any superfluous moves in either
    * collection.
    * 
    * <b>NOTE</b> the 'move' no longer stores the moving piece, therefore move.toString() delivers e.g. b7-d6 instead of Nb7-d6. To avoid
    * changing all the tests, this method checks for both "b7-d6" and "Xb7-d6" where X is the symbol for the piece (see
    * {@link Piece#symbol(org.rjo.newchess.piece.Colour)}).
    *
    * @param moves         the moves found
    * @param requiredMoves the required moves
    * @param moveFilter    an optional predicate to further filter <code>moves</code>, e.g. in order to just concentrate on pawn moves
    */
   private static void checkMoves(Position p, List<IMove> moves, Set<String> requiredMoves, BiFunction<Position, IMove, Boolean> moveFilter) {

      // clone moves so as to avoid losing the move list for later tests
      List<IMove> moveClone = new ArrayList<>(moves);
      Iterator<IMove> iter = moveClone.iterator();
      while (iter.hasNext()) {
         IMove m = iter.next();
         String toStringWithPiece = Pieces.toPiece(p.pieceAt(m.getOrigin())).symbol(Colour.WHITE) + m.toString();
         if (requiredMoves.contains(m.toString())) {
            requiredMoves.remove(m.toString());
            iter.remove();
         } else if (requiredMoves.contains(toStringWithPiece)) {
            requiredMoves.remove(toStringWithPiece);
            iter.remove();
         } else if (!moveFilter.apply(p, m)) { iter.remove(); }
      }
      if (!requiredMoves.isEmpty()) {
         fail("not all required moves found, expected: " + requiredMoves + (moveClone.isEmpty() ? "" : ". Input-Moves not processed: " + moveClone));
      }
      // all required moves found but still some input moves left over?
      if (!moveClone.isEmpty()) { fail("found unexpected moves: " + moveClone); }
   }
}
