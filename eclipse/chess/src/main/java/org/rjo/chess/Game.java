package org.rjo.chess;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;

import org.rjo.chess.pieces.Piece;
import org.rjo.chess.pieces.PieceType;

/**
 * Encapsulates the board, the moves, castling rights, etc (clocks?).
 * 
 * @author rich
 *
 */
public class Game {

   private Chessboard chessboard;
   /** stores the moves (ply) */
   private Deque<Move> moves;
   private EnumSet<CastlingRights>[] castling;
   /** which side is to move */
   private Colour sideToMove;

   public Game() {
      chessboard = new Chessboard();
      init();
   }

   public Game(Chessboard chessboard) {
      this.chessboard = chessboard;
      init();
   }

   private void init() {
      moves = new ArrayDeque<>();
      castling = new EnumSet[Colour.values().length];
      castling[Colour.WHITE.ordinal()] = EnumSet.allOf(CastlingRights.class);
      castling[Colour.BLACK.ordinal()] = EnumSet.allOf(CastlingRights.class);
      sideToMove = Colour.WHITE;
   }

   public int getMoveNumber() {
      return (moves.size() % 2) + 1;
   }

   /**
    * Sets the move number. NO-OP at the moment.
    */
   public void setMoveNumber(int moveNbr) {
      // NO-OP
   }

   public boolean canCastle(Colour colour, CastlingRights rights) {
      return castling[colour.ordinal()].contains(rights);
   }

   public void setCastlingRights(Colour colour, CastlingRights... rights) {
      castling[colour.ordinal()].clear();
      for (CastlingRights right : rights) {
         if (right != null) {
            castling[colour.ordinal()].add(right);
         }
      }
   }

   public Chessboard getChessboard() {
      return chessboard;
   }

   public Colour getSideToMove() {
      return sideToMove;
   }

   public void setSideToMove(Colour sideToMove) {
      this.sideToMove = sideToMove;
   }

   /**
    * Find all moves for the given colour from the current position.
    * 
    * @param colour
    *           the required colour
    * @return all moves for this colour.
    */
   public List<Move> findMoves(Colour colour) {
      List<Move> moves = new ArrayList<>(60);
      for (PieceType type : PieceType.values()) {
         Piece p = chessboard.getPieces(colour).get(type);
         // null == piece no longer on board
         if (p != null) {
            moves.addAll(p.findMoves(this));
         }
      }
      return moves;
   }
}
