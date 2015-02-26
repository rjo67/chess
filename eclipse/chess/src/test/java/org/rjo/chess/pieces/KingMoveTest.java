package org.rjo.chess.pieces;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.rjo.chess.CastlingRights;
import org.rjo.chess.Chessboard;
import org.rjo.chess.Colour;
import org.rjo.chess.Game;
import org.rjo.chess.Square;
import org.rjo.chess.TestUtil;

/**
 * Test movement of the king.
 * 
 * Tests are repeated for white and black kings, although there shouldn't be any difference.
 * 
 * NB: For these tests the position of the opponent's king must always be specified.
 * 
 * @author rich
 */
public class KingMoveTest {
   private King whiteKing;
   private King blackKing;

   @Before
   public void init() {
      whiteKing = new King(Colour.WHITE);
      blackKing = new King(Colour.BLACK);
   }

   @Test
   public void startPosition() {
      Game game = new Game();
      whiteKing.initPosition(); // TODO not very clean
      blackKing.initPosition();
      TestUtil.checkMoves(whiteKing.findMoves(game), new HashSet<>());
      TestUtil.checkMoves(blackKing.findMoves(game), new HashSet<>());
   }

   @Test
   public void middleOfBoard() {
      whiteKing.initPosition(Square.c5);
      blackKing.initPosition(Square.f5);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKing));
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(blackKing));
      Game game = new Game(new Chessboard(whitePieces, blackPieces));
      TestUtil
            .checkMoves(
                  whiteKing.findMoves(game),
                  new HashSet<>(Arrays.asList("Kc5-c4", "Kc5-c6", "Kc5-b4", "Kc5-b5", "Kc5-b6", "Kc5-d4", "Kc5-d5",
                        "Kc5-d6")));
      TestUtil
            .checkMoves(
                  blackKing.findMoves(game),
                  new HashSet<>(Arrays.asList("Kf5-f4", "Kf5-f6", "Kf5-e4", "Kf5-e5", "Kf5-e6", "Kf5-g4", "Kf5-g5",
                        "Kf5-g6")));
   }

   @Test
   public void onRank1() {
      whiteKing.initPosition(Square.c1);
      blackKing.initPosition(Square.f1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKing));
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(blackKing));
      Game game = new Game(new Chessboard(whitePieces, blackPieces));
      TestUtil.checkMoves(whiteKing.findMoves(game),
            new HashSet<>(Arrays.asList("Kc1-c2", "Kc1-b1", "Kc1-b2", "Kc1-d1", "Kc1-d2")));
      TestUtil.checkMoves(blackKing.findMoves(game),
            new HashSet<>(Arrays.asList("Kf1-f2", "Kf1-e1", "Kf1-e2", "Kf1-g1", "Kf1-g2")));
   }

   @Test
   public void onRank8() {
      whiteKing.initPosition(Square.c8);
      blackKing.initPosition(Square.g8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKing));
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(blackKing));
      Game game = new Game(new Chessboard(whitePieces, blackPieces));
      TestUtil.checkMoves(whiteKing.findMoves(game),
            new HashSet<>(Arrays.asList("Kc8-c7", "Kc8-b8", "Kc8-b7", "Kc8-d8", "Kc8-d7")));
      TestUtil.checkMoves(blackKing.findMoves(game),
            new HashSet<>(Arrays.asList("Kg8-g7", "Kg8-f8", "Kg8-f7", "Kg8-h8", "Kg8-h7")));
   }

   @Test
   public void onFile1() {
      whiteKing.initPosition(Square.a4);
      blackKing.initPosition(Square.a7);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKing));
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(blackKing));
      Game game = new Game(new Chessboard(whitePieces, blackPieces));
      TestUtil.checkMoves(whiteKing.findMoves(game),
            new HashSet<>(Arrays.asList("Ka4-a5", "Ka4-a3", "Ka4-b3", "Ka4-b4", "Ka4-b5")));
      TestUtil.checkMoves(blackKing.findMoves(game),
            new HashSet<>(Arrays.asList("Ka7-a8", "Ka7-a6", "Ka7-b6", "Ka7-b7", "Ka7-b8")));
   }

   @Test
   public void onFile8() {
      whiteKing.initPosition(Square.h4);
      blackKing.initPosition(Square.h7);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKing));
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(blackKing));
      Game game = new Game(new Chessboard(whitePieces, blackPieces));
      TestUtil.checkMoves(whiteKing.findMoves(game),
            new HashSet<>(Arrays.asList("Kh4-h5", "Kh4-h3", "Kh4-g3", "Kh4-g4", "Kh4-g5")));
      TestUtil.checkMoves(blackKing.findMoves(game),
            new HashSet<>(Arrays.asList("Kh7-h8", "Kh7-h6", "Kh7-g6", "Kh7-g7", "Kh7-g8")));
   }

   @Test
   public void kingsAdjacent() {
      whiteKing.initPosition(Square.c4);
      blackKing.initPosition(Square.e5);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKing));
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(blackKing));
      Game game = new Game(new Chessboard(whitePieces, blackPieces));
      TestUtil.checkMoves(whiteKing.findMoves(game),
            new HashSet<>(Arrays.asList("Kc4-b3", "Kc4-b4", "Kc4-b5", "Kc4-c3", "Kc4-c5", "Kc4-d3"
            // not possible because of adjacent king: "Kc4-d4", "Kc4-d5"
                  )));
      TestUtil.checkMoves(blackKing.findMoves(game),
            new HashSet<>(Arrays.asList("Ke5-f4", "Ke5-f5", "Ke5-f6", "Ke5-e6", "Ke5-e4", "Ke5-d6"
            // not possible because of adjacent king: "Ke5-d5", "Ke5-d4"
                  )));
   }

   @Test
   public void castleKingsSide() {
      whiteKing.initPosition(Square.e1);
      blackKing.initPosition(Square.h7);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKing, new Pawn(Colour.WHITE, Square.d2, Square.e2,
            Square.f2), new Rook(Colour.WHITE, Square.h1)));
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(blackKing));
      Game game = new Game(new Chessboard(whitePieces, blackPieces));
      game.setCastlingRights(Colour.WHITE, CastlingRights.KINGS_SIDE);
      TestUtil.checkMoves(whiteKing.findMoves(game), new HashSet<>(Arrays.asList("Ke1-d1", "Ke1-f1", "O-O")));
   }

   @Test
   public void castleKingsSideInCheck() {
      whiteKing.initPosition(Square.e1);
      blackKing.initPosition(Square.h7);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKing, new Pawn(Colour.WHITE, Square.d2, Square.e2,
            Square.f2), new Rook(Colour.WHITE, Square.h1)));
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(blackKing, new Bishop(Colour.BLACK, Square.h2)));
      Game game = new Game(new Chessboard(whitePieces, blackPieces));
      game.setCastlingRights(Colour.WHITE, CastlingRights.KINGS_SIDE);
      TestUtil.checkMoves(whiteKing.findMoves(game), new HashSet<>(Arrays.asList("Ke1-d1", "Ke1-f1")));
   }

   @Test
   public void castleQueensSide() {
      whiteKing.initPosition(Square.e1);
      blackKing.initPosition(Square.h7);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKing, new Rook(Colour.WHITE, Square.a1)));
      Set<Piece> blackPieces = new HashSet<>(Arrays.asList(blackKing));
      Game game = new Game(new Chessboard(whitePieces, blackPieces));
      game.setCastlingRights(Colour.WHITE, CastlingRights.QUEENS_SIDE);
      TestUtil.checkMoves(whiteKing.findMoves(game),
            new HashSet<>(Arrays.asList("Ke1-d1", "Ke1-f1", "Ke1-d2", "Ke1-e2", "Ke1-f2", "O-O-O")));
   }
}
