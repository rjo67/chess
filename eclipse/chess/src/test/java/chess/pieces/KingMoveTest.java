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

public class KingTest {
   private King king;

   @Before
   public void init() {
      king = new King(Colour.WHITE);
   }

   @Test
   public void startPosition() {
      Chessboard chessboard = new Chessboard();
      king.initPosition(); // TODO not very clean
      TestUtil.checkMoves(king.findMoves(chessboard), new HashSet<>());
   }

   @Test
   public void middleOfBoard() {
      king.initPosition(Square.d5);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king));
      Set<Piece> blackPieces = new HashSet<>();
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      TestUtil
            .checkMoves(
                  king.findMoves(chessboard),
                  new HashSet<>(Arrays.asList("Kd5-d4", "Kd5-d6", "Kd5-c4", "Kd5-c5", "Kd5-c6", "Kd5-e4", "Kd5-e5",
                        "Kd5-e6")));
   }

   @Test
   public void onRank1() {
      king.initPosition(Square.c1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king));
      Set<Piece> blackPieces = new HashSet<>();
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      TestUtil.checkMoves(king.findMoves(chessboard),
            new HashSet<>(Arrays.asList("Kc1-c2", "Kc1-b1", "Kc1-b2", "Kc1-d1", "Kc1-d2")));
   }

   @Test
   public void onRank8() {
      king.initPosition(Square.c8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king));
      Set<Piece> blackPieces = new HashSet<>();
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      TestUtil.checkMoves(king.findMoves(chessboard),
            new HashSet<>(Arrays.asList("Kc8-c7", "Kc8-b8", "Kc8-b7", "Kc8-d8", "Kc8-d7")));
   }

   @Test
   public void onFile1() {
      king.initPosition(Square.a4);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king));
      Set<Piece> blackPieces = new HashSet<>();
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      TestUtil.checkMoves(king.findMoves(chessboard),
            new HashSet<>(Arrays.asList("Ka4-a5", "Ka4-a3", "Ka4-b3", "Ka4-b4", "Ka4-b5")));
   }

   @Test
   public void onFile8() {
      king.initPosition(Square.h4);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king));
      Set<Piece> blackPieces = new HashSet<>();
      Chessboard chessboard = new Chessboard(whitePieces, blackPieces);
      TestUtil.checkMoves(king.findMoves(chessboard),
            new HashSet<>(Arrays.asList("Kh4-h5", "Kh4-h3", "Kh4-g3", "Kh4-g4", "Kh4-g5")));
   }

}
