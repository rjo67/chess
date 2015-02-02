package chess;

import chess.pieces.Piece;

/**
 * Represents a move.
 * 
 * @author rich
 */
/**
 * @author rich
 *
 */
public class Move {

   private Square from;
   private Square to;

   private Piece piece;

   // whether this move was a capture
   private boolean capture;

   // whether this move was a check
   private boolean check;

   /**
    * Constructor for normal non-capture non-check moves.
    * 
    * @param piece
    *           which piece is moving
    * @param from
    *           start square
    * @param to
    *           destination square
    */
   public Move(Piece piece, Square from, Square to) {
      this(piece, from, to, false);
   }

   /**
    * Constructor allowing specification of capture (non-check) moves.
    * 
    * @param piece
    *           which piece is moving
    * @param from
    *           start square
    * @param to
    *           destination square
    * @param capture
    *           whether this move is a capture
    */
   public Move(Piece piece, Square from, Square to, boolean capture) {
      this(piece, from, to, capture, false);
   }

   /**
    * Constructor allowing specification of capture and check moves.
    * 
    * @param piece
    *           which piece is moving
    * @param from
    *           start square
    * @param to
    *           destination square
    * @param capture
    *           whether this move is a capture
    * @param check
    *           whether this move is a check
    */
   public Move(Piece piece, Square from, Square to, boolean capture, boolean check) {
      this.piece = piece;
      this.from = from;
      this.to = to;
      this.capture = capture;
      this.check = check;
   }

   @Override
   public String toString() {
      return piece.getSymbol() + from + (capture ? "x" : "-") + to + (check ? "+" : "");
   }

}
