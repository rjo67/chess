package chess;

import chess.pieces.Piece;
import chess.pieces.PieceType;

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

   private Piece piece; // maybe a bit heavyweight

   // whether this move was a capture
   private boolean capture;

   // whether this move was a check
   private boolean check;

   // if this move was a promotion, then this field indicates the piece and is not null
   private PieceType promotionPiece;

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

   public void setPromotionPiece(PieceType type) {
      if (piece.getType() != PieceType.PAWN) {
         throw new IllegalArgumentException("can only specify a promotion piece for a pawn move");
      }
      this.promotionPiece = type;
   }

   @Override
   public String toString() {
      return piece.getSymbol() + from + (capture ? "x" : "-") + to
            + (promotionPiece != null ? "=" + promotionPiece.getSymbol() : "") + (check ? "+" : "");
   }

}
