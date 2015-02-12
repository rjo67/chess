package chess.pieces;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import chess.Chessboard;
import chess.Colour;
import chess.Square;
import chess.TestUtil;

public class PawnTest {

   private Pawn pawn;

   @Before
   public void init() {
      pawn = new Pawn(Colour.WHITE);
   }

   @Test
   public void startPosition() {
      Chessboard chessboard = new Chessboard();
      pawn.initPosition();
      TestUtil.checkMoves(
            pawn.findMoves(chessboard),
            new HashSet<>(Arrays.asList("a2-a3", "a2-a4", "b2-b3", "b2-b4", "c2-c3", "c2-c4", "d2-d3", "d2-d4",
                  "e2-e3", "e2-e4", "f2-f3", "f2-f4", "g2-g3", "g2-g4", "h2-h3", "h2-h4")));
   }

   @Test
   public void blockedPawn() {
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(pawn));
      pawn.initPosition(Square.a2, Square.a3);
      Set<Piece> blackPieces = new HashSet<>();
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      TestUtil.checkMoves(pawn.findMoves(chessboard), new HashSet<>(Arrays.asList("a3-a4")));
   }

   @Test
   public void captureLeft() {
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(pawn));
      pawn.initPosition(Square.b2);
      Pawn blackPawn = new Pawn(Colour.BLACK);
      blackPawn.initPosition(Square.a3, Square.b3);
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(blackPawn));
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      TestUtil.checkMoves(pawn.findMoves(chessboard), new HashSet<>(Arrays.asList("b2xa3")));
   }

   @Test
   public void captureRight() {
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(pawn));
      pawn.initPosition(Square.a2);
      Pawn blackPawn = new Pawn(Colour.BLACK);
      blackPawn.initPosition(Square.a3, Square.b3);
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(blackPawn));
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      TestUtil.checkMoves(pawn.findMoves(chessboard), new HashSet<>(Arrays.asList("a2xb3")));
   }

   @Test
   public void promotion() {
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(pawn));
      pawn.initPosition(Square.a7);
      Set<Piece> blackPieces = new HashSet<>();
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      TestUtil.checkMoves(pawn.findMoves(chessboard),
            new HashSet<>(Arrays.asList("a7-a8=Q", "a7-a8=B", "a7-a8=N", "a7-a8=R")));
   }

   @Test
   public void enpassantRight() {
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(pawn));
      pawn.initPosition(Square.a5);
      // don't need to set up the black pawns; it's enough to set the enpassant square
      Set<Piece> blackPieces = new HashSet<>();
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      chessboard.setEnpassantSquare(Square.b6);
      TestUtil.checkMoves(pawn.findMoves(chessboard), new HashSet<>(Arrays.asList("a5-a6", "a5xb6")));
   }

   @Test
   public void enpassantLeft() {
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(pawn));
      pawn.initPosition(Square.b5);
      // don't need to set up the black pawns; it's enough to set the enpassant square
      Set<Piece> blackPieces = new HashSet<>();
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      chessboard.setEnpassantSquare(Square.a6);
      TestUtil.checkMoves(pawn.findMoves(chessboard), new HashSet<>(Arrays.asList("b5-b6", "b5xa6")));
   }

   @Test
   public void enpassantWithTwoCandidatePawns() {
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(pawn));
      pawn.initPosition(Square.b5, Square.d5);
      // don't need to set up the black pawns; it's enough to set the enpassant square
      Set<Piece> blackPieces = new HashSet<>();
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      chessboard.setEnpassantSquare(Square.c6);
      TestUtil.checkMoves(pawn.findMoves(chessboard), new HashSet<>(Arrays.asList("b5-b6", "b5xc6", "d5-d6", "d5xc6")));
   }

}
