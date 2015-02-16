package org.rjo.chess.pieces;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.rjo.chess.Chessboard;
import org.rjo.chess.Colour;
import org.rjo.chess.Game;
import org.rjo.chess.Square;
import org.rjo.chess.TestUtil;

public class BlackPawnMoveTest {

   @Test
   public void startPosition() {
      Game game = new Game();
      Pawn pawn = new Pawn(Colour.BLACK);
      TestUtil.checkMoves(
            pawn.findMoves(game),
            new HashSet<>(Arrays.asList("a7-a6", "a7-a5", "b7-b6", "b7-b5", "c7-c6", "c7-c5", "d7-d6", "d7-d5",
                  "e7-e6", "e7-e5", "f7-f6", "f7-f5", "g7-g6", "g7-g5", "h7-h6", "h7-h5")));
   }

   @Test
   public void blockedPawn() {
      Set<Piece> whitePieces = new HashSet<>();
      Pawn pawn = new Pawn(Colour.BLACK, Square.a7, Square.a6);
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      Game game = new Game(new Chessboard(whitePieces, blackPieces));
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("a6-a5")));
   }

   @Test
   public void captureLeft() {
      Pawn pawn = new Pawn(Colour.BLACK, Square.b5);
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      Pawn whitePawn = new Pawn(Colour.WHITE, Square.a4, Square.b4);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whitePawn));
      Game game = new Game(new Chessboard(whitePieces, blackPieces));
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("b5xa4")));
   }

   @Test
   public void captureRight() {
      Pawn pawn = new Pawn(Colour.BLACK, Square.a4);
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      Pawn whitePawn = new Pawn(Colour.WHITE, Square.a3, Square.b3);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whitePawn));
      Game game = new Game(new Chessboard(whitePieces, blackPieces));
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("a4xb3")));
   }

   @Test
   public void promotion() {
      Pawn pawn = new Pawn(Colour.BLACK, Square.a2);
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      Set<Piece> whitePieces = new HashSet<>();
      Game game = new Game(new Chessboard(whitePieces, blackPieces));
      TestUtil.checkMoves(pawn.findMoves(game),
            new HashSet<>(Arrays.asList("a2-a1=Q", "a2-a1=B", "a2-a1=N", "a2-a1=R")));
   }

   @Test
   public void enpassantRight() {
      Pawn pawn = new Pawn(Colour.BLACK, Square.a4);
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      // don't need to set up the white pawns; it's enough to set the enpassant square
      Set<Piece> whitePieces = new HashSet<>();
      Game game = new Game(new Chessboard(whitePieces, blackPieces));
      game.getChessboard().setEnpassantSquare(Square.b3);
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("a4-a3", "a4xb3")));
   }

   @Test
   public void enpassantLeft() {
      Pawn pawn = new Pawn(Colour.BLACK, Square.b4);
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      // don't need to set up the white pawns; it's enough to set the enpassant square
      Set<Piece> whitePieces = new HashSet<>();
      Game game = new Game(new Chessboard(whitePieces, blackPieces));
      game.getChessboard().setEnpassantSquare(Square.a3);
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("b4-b3", "b4xa3")));
   }

   @Test
   public void enpassantWithTwoCandidatePawns() {
      Pawn pawn = new Pawn(Colour.BLACK, Square.b4, Square.d4);
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      // don't need to set up the white pawns; it's enough to set the enpassant square
      Set<Piece> whitePieces = new HashSet<>();
      Game game = new Game(new Chessboard(whitePieces, blackPieces));
      game.getChessboard().setEnpassantSquare(Square.c3);
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("b4-b3", "b4xc3", "d4-d3", "d4xc3")));
   }

}
