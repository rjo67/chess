package chess.pieces;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import chess.Chessboard;
import chess.Colour;
import chess.Square;
import chess.TestUtil;

public class RookMoveTest {

   @Test
   public void startPosition() {
      Chessboard chessboard = new Chessboard();
      Rook whiteRook = new Rook(Colour.WHITE);
      TestUtil.checkMoves(whiteRook.findMoves(chessboard), new HashSet<>());
   }

   @Test
   public void moveFromMiddleOfBoard() {
      Rook whiteRook = new Rook(Colour.WHITE, Square.d4);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteRook));
      Chessboard chessboard = new Chessboard(whitePieces, new HashSet<>());
      TestUtil.checkMoves(
            whiteRook.findMoves(chessboard),
            new HashSet<>(Arrays.asList("Rd4-d5", "Rd4-d6", "Rd4-d7", "Rd4-d8", "Rd4-e4", "Rd4-f4", "Rd4-g4", "Rd4-h4",
                  "Rd4-d3", "Rd4-d2", "Rd4-d1", "Rd4-c4", "Rd4-b4", "Rd4-a4")));
   }

   @Test
   public void moveFromA1() {
      Rook whiteRook = new Rook(Colour.WHITE, Square.a1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteRook));
      Chessboard chessboard = new Chessboard(whitePieces, new HashSet<>());
      TestUtil.checkMoves(
            whiteRook.findMoves(chessboard),
            new HashSet<>(Arrays.asList("Ra1-a2", "Ra1-a3", "Ra1-a4", "Ra1-a5", "Ra1-a6", "Ra1-a7", "Ra1-a8", "Ra1-b1",
                  "Ra1-c1", "Ra1-d1", "Ra1-e1", "Ra1-f1", "Ra1-g1", "Ra1-h1")));
   }

   /**
    * rook gets blocked by own pieces in north and east direction
    */
   @Test
   public void moveFromA1WithBlockade() {
      Rook whiteRook = new Rook(Colour.WHITE, Square.a1);
      Pawn pawn = new Pawn(Colour.WHITE, Square.a4);
      King king = new King(Colour.WHITE, Square.c1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(pawn, king, whiteRook));
      Chessboard chessboard = new Chessboard(whitePieces, new HashSet<>());
      TestUtil.checkMoves(whiteRook.findMoves(chessboard), new HashSet<>(Arrays.asList("Ra1-a2", "Ra1-a3", "Ra1-b1")));
   }

   /**
    * rook gets blocked by enemy pieces
    */
   @Test
   public void moveFromA1WithCapture() {
      Rook whiteRook = new Rook(Colour.WHITE, Square.a1);
      Pawn pawn = new Pawn(Colour.BLACK, Square.a4);
      King king = new King(Colour.WHITE, Square.c1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king, whiteRook));
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      TestUtil.checkMoves(whiteRook.findMoves(chessboard),
            new HashSet<>(Arrays.asList("Ra1-a2", "Ra1-a3", "Ra1xa4", "Ra1-b1")));
   }

   @Test
   public void moveFromH1() {
      Rook whiteRook = new Rook(Colour.WHITE, Square.h1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteRook));
      Chessboard chessboard = new Chessboard(whitePieces, new HashSet<>());
      TestUtil.checkMoves(
            whiteRook.findMoves(chessboard),
            new HashSet<>(Arrays.asList("Rh1-h2", "Rh1-h3", "Rh1-h4", "Rh1-h5", "Rh1-h6", "Rh1-h7", "Rh1-h8", "Rh1-g1",
                  "Rh1-f1", "Rh1-e1", "Rh1-d1", "Rh1-c1", "Rh1-b1", "Rh1-a1")));
   }

   /**
    * rook gets blocked by own pieces in north and west direction
    */
   @Test
   public void moveFromH1WithBlockade() {
      Rook whiteRook = new Rook(Colour.WHITE, Square.h1);
      Pawn pawn = new Pawn(Colour.WHITE, Square.h4);
      King king = new King(Colour.WHITE, Square.f1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(pawn, king, whiteRook));
      Chessboard chessboard = new Chessboard(whitePieces, new HashSet<>());
      TestUtil.checkMoves(whiteRook.findMoves(chessboard), new HashSet<>(Arrays.asList("Rh1-h2", "Rh1-h3", "Rh1-g1")));
   }

   /**
    * rook gets blocked by enemy pieces
    */
   @Test
   public void moveFromH1WithCapture() {
      Rook whiteRook = new Rook(Colour.WHITE, Square.h1);
      Pawn pawn = new Pawn(Colour.BLACK, Square.h4);
      King king = new King(Colour.WHITE, Square.f1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king, whiteRook));
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      TestUtil.checkMoves(whiteRook.findMoves(chessboard),
            new HashSet<>(Arrays.asList("Rh1-h2", "Rh1-h3", "Rh1xh4", "Rh1-g1")));
   }

   @Test
   public void moveFromA8() {
      Rook whiteRook = new Rook(Colour.WHITE, Square.a8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteRook));
      Chessboard chessboard = new Chessboard(whitePieces, new HashSet<>());
      TestUtil.checkMoves(
            whiteRook.findMoves(chessboard),
            new HashSet<>(Arrays.asList("Ra8-a7", "Ra8-a6", "Ra8-a5", "Ra8-a4", "Ra8-a3", "Ra8-a2", "Ra8-a1", "Ra8-b8",
                  "Ra8-c8", "Ra8-d8", "Ra8-e8", "Ra8-f8", "Ra8-g8", "Ra8-h8")));
   }

   /**
    * rook gets blocked by own pieces in south and east direction
    */
   @Test
   public void moveFromA8WithBlockade() {
      Rook whiteRook = new Rook(Colour.WHITE, Square.a8);
      Pawn pawn = new Pawn(Colour.WHITE, Square.a5);
      King king = new King(Colour.WHITE, Square.c8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(pawn, king, whiteRook));
      Chessboard chessboard = new Chessboard(whitePieces, new HashSet<>());
      TestUtil.checkMoves(whiteRook.findMoves(chessboard), new HashSet<>(Arrays.asList("Ra8-a7", "Ra8-a6", "Ra8-b8")));
   }

   /**
    * rook gets blocked by enemy pieces
    */
   @Test
   public void moveFromA8WithCapture() {
      Rook whiteRook = new Rook(Colour.WHITE, Square.a8);
      Pawn pawn = new Pawn(Colour.BLACK, Square.a5);
      King king = new King(Colour.WHITE, Square.c8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king, whiteRook));
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      TestUtil.checkMoves(whiteRook.findMoves(chessboard),
            new HashSet<>(Arrays.asList("Ra8-a7", "Ra8-a6", "Ra8xa5", "Ra8-b8")));
   }

   @Test
   public void moveFromH8() {
      Rook whiteRook = new Rook(Colour.WHITE, Square.h8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteRook));
      Chessboard chessboard = new Chessboard(whitePieces, new HashSet<>());
      TestUtil.checkMoves(
            whiteRook.findMoves(chessboard),
            new HashSet<>(Arrays.asList("Rh8-h7", "Rh8-h6", "Rh8-h5", "Rh8-h4", "Rh8-h3", "Rh8-h2", "Rh8-h1", "Rh8-g8",
                  "Rh8-f8", "Rh8-e8", "Rh8-d8", "Rh8-c8", "Rh8-b8", "Rh8-a8")));
   }

   /**
    * rook gets blocked by own pieces in south and west direction
    */
   @Test
   public void moveFromH8WithBlockade() {
      Rook whiteRook = new Rook(Colour.WHITE, Square.h8);
      Pawn pawn = new Pawn(Colour.WHITE, Square.h5);
      King king = new King(Colour.WHITE, Square.f8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(pawn, king, whiteRook));
      Chessboard chessboard = new Chessboard(whitePieces, new HashSet<>());
      TestUtil.checkMoves(whiteRook.findMoves(chessboard), new HashSet<>(Arrays.asList("Rh8-h7", "Rh8-h6", "Rh8-g8")));
   }

   /**
    * rook gets blocked by enemy pieces
    */
   @Test
   public void moveFromH8WithCapture() {
      Rook whiteRook = new Rook(Colour.WHITE, Square.h8);
      Pawn pawn = new Pawn(Colour.BLACK, Square.h5);
      King king = new King(Colour.WHITE, Square.f8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king, whiteRook));
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(pawn));
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      TestUtil.checkMoves(whiteRook.findMoves(chessboard),
            new HashSet<>(Arrays.asList("Rh8-h7", "Rh8-h6", "Rh8xh5", "Rh8-g8")));
   }
}
