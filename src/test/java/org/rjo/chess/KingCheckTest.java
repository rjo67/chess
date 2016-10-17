package org.rjo.chess;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.rjo.chess.pieces.PieceType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test determination of whether the king is in check.
 *
 * @author rich
 */
public class KingCheckTest {
   private Game game;
   private Map<PieceType, BitSet> enemyPieces;

   private void setup(String fen) {
      game = Fen.decode(fen);
      enemyPieces = setupBlackBitsets(game.getPosition());
   }

   /**
    * moving the rook away from the file leaves the king in check
    */
   @Test
   public void pinnedOnFileMovesAway() {
      setup("3r4/4k3/6b1/8/3RP3/3K4/8/8 w - - 10 10");
      Move move = new Move(PieceType.ROOK, Colour.WHITE, Square.d4, Square.c4);
      assertTrue(Position.isKingInCheck(game.getPosition(), move, Colour.BLACK, Square.d3, false));
   }

   /**
    * moving the pawn away from the diagonal leaves the king in check
    */
   @Test
   public void pinnedOnDiagonalMovesAway() {
      setup("3r4/4k3/6b1/8/3RP3/3K4/8/8 w - - 10 10");
      Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.e4, Square.e5);
      assertTrue(Position.isKingInCheck(game.getPosition(), move, Colour.BLACK, Square.d3, false));
   }

   /**
    * moving the rook along from the file does not leave the king in check
    */
   @Test
   public void pinnedOnFileStaysOnFile() {
      setup("3r4/4k3/6b1/8/3RP3/3K4/8/8 w - - 10 10");
      Move move = new Move(PieceType.ROOK, Colour.WHITE, Square.d4, Square.d5);
      assertFalse(Position.isKingInCheck(game.getPosition(), move, Colour.BLACK, Square.d3, false));
   }

   /**
    * capturing the checking piece does not leave the king in check
    */
   @Test
   public void checkingPieceGetsCaptured() {
      setup("8/4k3/6b1/8/4P3/3K3r/8/5Q2 w - - 10 10");
      Move move = new Move(PieceType.QUEEN, Colour.WHITE, Square.f1, Square.h3, PieceType.ROOK);
      assertFalse(Position.isKingInCheck(game.getPosition(), move, Colour.BLACK, Square.d3, false));
   }

   /**
    * 'static' check of a position
    */
   @Test
   public void knightGivesCheck() {
      setup("8/4k3/8/2n5/4P3/3K4/8/8 w - - 10 10");
      Map<PieceType, BitSet> enemyPieces = setupBlackBitsets(game.getPosition());
      assertTrue(KingCheck.isKingInCheck(Square.d3, Colour.WHITE, getWhitePieces(game.getPosition()), enemyPieces));
   }

   /**
    * 'static' check of a position
    */
   @Test
   public void pawnGivesCheck() {
      setup("8/4k3/8/8/2p5/3K4/8/8 w - - 10 10");
      assertTrue(KingCheck.isKingInCheck(Square.d3, Colour.WHITE, getWhitePieces(game.getPosition()), enemyPieces));
   }

   /**
    * 'static' check of a position
    */
   @Test
   public void bishopGivesCheck() {
      setup("8/4k3/8/1b6/4P3/3K4/8/8 w - - 10 10");
      assertTrue(KingCheck.isKingInCheck(Square.d3, Colour.WHITE, getWhitePieces(game.getPosition()), enemyPieces));
   }

   /**
    * 'static' check of a position
    */
   @Test
   public void queenGivesCheckOnFile() {
      setup("8/4k3/8/8/4P3/3K3q/8/6q1 w - - 10 10");
      assertTrue(KingCheck.isKingInCheck(Square.d3, Colour.WHITE, getWhitePieces(game.getPosition()), enemyPieces));
   }

   /**
    * 'static' check of a position
    */
   @Test
   public void queenGivesCheckDiagonally() {
      setup("8/4k3/8/8/4P3/3K4/8/5q2 w - - 10 10");
      assertTrue(KingCheck.isKingInCheck(Square.d3, Colour.WHITE, getWhitePieces(game.getPosition()), enemyPieces));
   }

   /**
    * 'static' check of a position
    */
   @Test
   public void rookGivesCheck() {
      setup("3r4/4k3/8/r7/4P3/3K4/8/8 w - - 10 10");
      assertTrue(KingCheck.isKingInCheck(Square.d3, Colour.WHITE, getWhitePieces(game.getPosition()), enemyPieces));
   }

   /**
    * 'static' check of a position
    */
   @Test
   public void notInCheck() {
      setup("3bq3/pp2k3/8/rn3b2/4P3/3K1Pr1/8/8 w - - 10 10");
      assertFalse(KingCheck.isKingInCheck(Square.d3, Colour.WHITE, getWhitePieces(game.getPosition()), enemyPieces));
   }

   /**
    * the 'friendlyPieces' bitset must not get changed by the call to
    * {@link KingCheck#isKingInCheck(Square, Colour, BitSet, Map, Move)}.
    */
   @Test
   public void friendlyPiecesDoesNotGetChangedAfterMove() {
      setup("3bq3/pp2k3/8/rn3b2/4P3/3K1Pr1/8/8 w - - 10 10");
      BitSet friendlyPieces = getWhitePieces(game.getPosition());
      Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.e4, Square.e5);
      assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.d3, Colour.WHITE, friendlyPieces,
            enemyPieces, move));
      assertEquals(friendlyPieces, getWhitePieces(game.getPosition()));
   }

   /**
    * the 'enemyPieces' bitset must not get changed by the call to
    * {@link KingCheck#isKingInCheck(Square, Colour, BitSet, Map, Move)}.
    */
   @Test
   public void enemyPiecesNotChangedAfterCaptureMove() {
      setup("3r4/4k3/8/r7/4P3/8/2Kb4/8 w - - 10 10");
      BitSet bishopBitSet = game.getPosition().getPieces(Colour.BLACK).get(PieceType.BISHOP).getBitBoard()
            .getBitSet();
      Move move = new Move(PieceType.KING, Colour.WHITE, Square.c2, Square.d2, PieceType.BISHOP);
      assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.d2, Colour.WHITE,
            getWhitePieces(game.getPosition()), enemyPieces, move));
      // should be same object
      assertTrue(bishopBitSet == game.getPosition().getPieces(Colour.BLACK).get(PieceType.BISHOP).getBitBoard()
            .getBitSet());
   }

   //
   // ********* pawn moves
   //

   @Test
   public void checkAfterNonCaptureMove() {
      setup("3bq3/pp2k3/8/rn3b2/4P3/3K1Pr1/8/8 w - - 10 10");
      Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.e4, Square.e5);
      assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.d3, Colour.WHITE,
            getWhitePieces(game.getPosition()), enemyPieces, move));
   }

   @Test
   public void pawnCaptureIntoCheck() {
      setup("3r4/4k3/8/r7/4q3/3P4/3K4/8 w - - 10 10");
      Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.d3, Square.e4, PieceType.QUEEN);
      assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.d2, Colour.WHITE,
            getWhitePieces(game.getPosition()), enemyPieces, move));
   }

   @Test
   public void enpassantCaptureIntoCheck() {
      setup("3r4/4k3/8/2pP4/8/3K4/8/8 w - - 10 10");
      Move move = Move.enpassant(Colour.WHITE, Square.d5, Square.c6);
      assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.d3, Colour.WHITE,
            getWhitePieces(game.getPosition()), enemyPieces, move));
   }

   //
   // ********* king moves
   //

   @Test
   public void kingMovesIntoCheck() {
      setup("3r4/4k3/8/r7/4P3/8/2K5/8 w - - 10 10");
      Move move = new Move(PieceType.KING, Colour.WHITE, Square.c2, Square.d2);
      assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.d2, Colour.WHITE,
            getWhitePieces(game.getPosition()), enemyPieces, move));
   }

   @Test
   public void kingCapturesIntoCheck() {
      setup("3r4/4k3/8/r7/4P3/8/2Kb4/8 w - - 10 10");
      Move move = new Move(PieceType.KING, Colour.WHITE, Square.c2, Square.d2, PieceType.BISHOP);
      assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.d2, Colour.WHITE,
            getWhitePieces(game.getPosition()), enemyPieces, move));
   }

   @Test
   public void kingCastlesIntoCheck() {
      setup("8/6r1/8/8/8/k7/8/4K2R w K - 10 10");
      Move move = Move.castleKingsSide(Colour.WHITE);
      assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.g1, Colour.WHITE,
            getWhitePieces(game.getPosition()), enemyPieces, move));
   }

   //
   // ********* bishop moves
   //

   @Test
   public void discoveredCheck() {
      setup("8/4k3/8/b7/8/2BP4/3K4/8 w - - 10 10");
      Move move = new Move(PieceType.BISHOP, Colour.WHITE, Square.c3, Square.d4);
      assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.d2, Colour.WHITE,
            getWhitePieces(game.getPosition()), enemyPieces, move));
   }

   private BitSet getWhitePieces(Position chessboard) {
      return chessboard.getAllPieces(Colour.WHITE).getBitSet();
   }

   private Map<PieceType, BitSet> setupBlackBitsets(Position chessboard) {
      Map<PieceType, BitSet> enemyPieces = new HashMap<>();
      for (PieceType type : PieceType.ALL_PIECE_TYPES) {
         enemyPieces.put(type, chessboard.getPieces(Colour.BLACK).get(type).getBitBoard().getBitSet());
      }
      return enemyPieces;
   }
}
