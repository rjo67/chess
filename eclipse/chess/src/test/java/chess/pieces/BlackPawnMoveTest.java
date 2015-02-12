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

public class BlackPawnTest {

   private Pawn pawn;

   @Before
   public void init() {
      pawn = new Pawn(Colour.BLACK);
   }

   @Test
   public void startPosition() {
      Chessboard chessboard = new Chessboard();
      pawn.initPosition();
      TestUtil.checkMoves(
            pawn.findMoves(chessboard),
            new HashSet<>(Arrays.asList("a7-a6", "a7-a5", "b7-b6", "b7-b5", "c7-c6", "c7-c5", "d7-d6", "d7-d5",
                  "e7-e6", "e7-e5", "f7-f6", "f7-f5", "g7-g6", "g7-g5", "h7-h6", "h7-h5")));
   }

   @Test
   public void blockedPawn() {
      Set<Piece> whitePieces = new HashSet<>();
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      pawn.initPosition(Square.a7, Square.a6);
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      TestUtil.checkMoves(pawn.findMoves(chessboard), new HashSet<>(Arrays.asList("a6-a5")));
   }

   @Test
   public void captureLeft() {
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      pawn.initPosition(Square.b5);
      Pawn whitePawn = new Pawn(Colour.WHITE);
      whitePawn.initPosition(Square.a4, Square.b4);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whitePawn));
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      TestUtil.checkMoves(pawn.findMoves(chessboard), new HashSet<>(Arrays.asList("b5xa4")));
   }

   @Test
   public void captureRight() {
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      pawn.initPosition(Square.a4);
      Pawn whitePawn = new Pawn(Colour.WHITE);
      whitePawn.initPosition(Square.a3, Square.b3);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whitePawn));
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      TestUtil.checkMoves(pawn.findMoves(chessboard), new HashSet<>(Arrays.asList("a4xb3")));
   }

   @Test
   public void promotion() {
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      pawn.initPosition(Square.a2);
      Set<Piece> whitePieces = new HashSet<>();
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      TestUtil.checkMoves(pawn.findMoves(chessboard),
            new HashSet<>(Arrays.asList("a2-a1=Q", "a2-a1=B", "a2-a1=N", "a2-a1=R")));
   }

   @Test
   public void enpassantRight() {
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      pawn.initPosition(Square.a4);
      // don't need to set up the white pawns; it's enough to set the enpassant square
      Set<Piece> whitePieces = new HashSet<>();
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      chessboard.setEnpassantSquare(Square.b3);
      TestUtil.checkMoves(pawn.findMoves(chessboard), new HashSet<>(Arrays.asList("a4-a3", "a4xb3")));
   }

   @Test
   public void enpassantLeft() {
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      pawn.initPosition(Square.b4);
      // don't need to set up the white pawns; it's enough to set the enpassant square
      Set<Piece> whitePieces = new HashSet<>();
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      chessboard.setEnpassantSquare(Square.a3);
      TestUtil.checkMoves(pawn.findMoves(chessboard), new HashSet<>(Arrays.asList("b4-b3", "b4xa3")));
   }

   @Test
   public void enpassantWithTwoCandidatePawns() {
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      pawn.initPosition(Square.b4, Square.d4);
      // don't need to set up the white pawns; it's enough to set the enpassant square
      Set<Piece> whitePieces = new HashSet<>();
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      chessboard.setEnpassantSquare(Square.c3);
      TestUtil.checkMoves(pawn.findMoves(chessboard), new HashSet<>(Arrays.asList("b4-b3", "b4xc3", "d4-d3", "d4xc3")));
   }

}
