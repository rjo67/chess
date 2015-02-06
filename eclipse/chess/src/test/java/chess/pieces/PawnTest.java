package chess.pieces;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import chess.Chessboard;
import chess.Colour;
import chess.Move;
import chess.Square;

import static org.junit.Assert.assertEquals;

public class PawnTest {

   private Pawn pawn;

   @Before
   public void init() {
      pawn = new Pawn(Colour.White);
   }

   @Test
   public void startPosition() {
      Chessboard chessboard = new Chessboard();
      pawn.initPosition();
      List<Move> moves = pawn.findMoves(chessboard);
      assertEquals(16, moves.size());
   }

   @Test
   public void blockedPawn() {
      Set<Piece> whitePieces = new HashSet<Piece>(Arrays.asList(pawn));
      pawn.initPosition(Square.a2, Square.a3);
      Set<Piece> blackPieces = new HashSet<Piece>();
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);

      List<Move> moves = pawn.findMoves(chessboard);
      assertEquals(1, moves.size());
      assertEquals("a3-a4", moves.get(0).toString());
   }

}
