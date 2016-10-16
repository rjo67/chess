package org.rjo.chess;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.rjo.chess.pieces.Piece;
import org.rjo.chess.pieces.PieceType;

/**
 * Attempts to optimize the test whether a king is in check,
 * by storing data structures which don't change.
 *
 * @author rich
 *
 */
public class KingChecker {
   private BitSet friendlyPieces;
   private Map<PieceType, BitSet> enemyPieces;
   private Square myKing;
   private Colour myColour;
   private Colour opponentsColour;

   public KingChecker(Position chessboard, Colour opponentsColour, Square myKing) {
      this.opponentsColour = opponentsColour;
      this.myColour = Colour.oppositeColour(opponentsColour);
      friendlyPieces = chessboard.getAllPieces(myColour).getBitSet();
      enemyPieces = setupEnemyBitsets(chessboard.getPieces(opponentsColour));
      this.myKing = myKing;
   }

   private Map<PieceType, BitSet> setupEnemyBitsets(Map<PieceType, Piece> map) {
      Map<PieceType, BitSet> enemyPieces = new HashMap<>();
      for (PieceType type : PieceType.ALL_PIECE_TYPES) {
         enemyPieces.put(type, map.get(type).getBitBoard().getBitSet());
      }
      return enemyPieces;
   }

   public boolean isKingInCheck(Move move, boolean kingWasInCheck) {
      return KingCheck.isKingInCheckAfterMove(myKing, myColour, friendlyPieces, enemyPieces, move, kingWasInCheck);
   }
}
