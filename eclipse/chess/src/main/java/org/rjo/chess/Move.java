package org.rjo.chess;

import org.rjo.chess.pieces.King;
import org.rjo.chess.pieces.Piece;
import org.rjo.chess.pieces.PieceType;

/**
 * Represents a move.
 * 
 * @author rich
 */
public class Move {

   private Square from;
   private Square to;

   private Piece piece; // maybe a bit heavyweight

   /** whether this move was a capture */
   private boolean capture;

   /** whether this move was a check */
   private boolean check;

   /** whether this move was castling king's side */
   private boolean castlingKingsSide;
   /** whether this move was castling queen's side */
   private boolean castlingQueensSide;

   /** if this move was a promotion, then this field indicates the piece and is not null */
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
      this.castlingKingsSide = false;
      this.castlingQueensSide = false;
   }

   public void setPromotionPiece(PieceType type) {
      if (piece.getType() != PieceType.PAWN) {
         throw new IllegalArgumentException("can only specify a promotion piece for a pawn move");
      }
      this.promotionPiece = type;
   }

   @Override
   public String toString() {
      if (castlingKingsSide) {
         return "O-O";
      } else if (castlingQueensSide) {
         return "O-O-O";
      } else {
         return piece.getSymbol() + from + (capture ? "x" : "-") + to
               + (isPromotion() ? "=" + promotionPiece.getSymbol() : "") + (check ? "+" : "");
      }
   }

   public static Move castleKingsSide(King king) {
      Move move = new Move(king, Square.e1, Square.g1);
      move.castlingKingsSide = true;
      return move;
   }

   public static Move castleQueensSide(King king) {
      Move move = new Move(king, Square.e1, Square.c1);
      move.castlingQueensSide = true;
      return move;
   }

   public void setCheck(boolean check) {
      this.check = check;
   }

   public boolean isCheck() {
      return check;
   }

   public boolean isCapture() {
      return capture;
   }

   public boolean isPromotion() {
      return promotionPiece != null;
   }

   public Square from() {
      return from;
   }

   public Square to() {
      return to;
   }
}
