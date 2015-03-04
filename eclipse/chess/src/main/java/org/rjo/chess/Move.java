package org.rjo.chess;

import org.rjo.chess.pieces.King;
import org.rjo.chess.pieces.Piece;
import org.rjo.chess.pieces.PieceType;
import org.rjo.chess.pieces.Rook;

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

   /** castling info -- if not null, implies that this move was 0-0 or 0-0-0 */
   private CastlingInfo castlingInfo;

   /** if promotion info -- if not null, implies that this move was a promotion */
   private PromotionInfo promotionInfo;

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
      this.castlingInfo = null;
   }

   public void setPromotionPiece(PieceType type) {
      if (piece.getType() != PieceType.PAWN) {
         throw new IllegalArgumentException("can only specify a promotion piece for a pawn move");
      }

      this.promotionInfo = new PromotionInfo(type);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(10);
      if (castlingInfo != null) {
         if (CastlingRights.KINGS_SIDE == castlingInfo.direction) {
            sb.append("O-O");
         } else {
            sb.append("O-O-O");
         }
      } else {
         sb.append(piece.getSymbol());
         sb.append(from);
         sb.append(capture ? "x" : "-");
         sb.append(to);
         sb.append(isPromotion() ? "=" + promotionInfo.promotedPiece.getSymbol() : "");
      }
      sb.append(check ? "+" : "");
      return sb.toString();
   }

   public static Move castleKingsSide(King king) {
      Move move;
      if (king.getColour() == Colour.WHITE) {
         move = new Move(king, Square.e1, Square.g1);
         move.castlingInfo = new CastlingInfo(CastlingRights.KINGS_SIDE, new Move(new Rook(Colour.WHITE), Square.h1,
               Square.f1));
      } else {
         move = new Move(king, Square.e8, Square.g8);
         move.castlingInfo = new CastlingInfo(CastlingRights.KINGS_SIDE, new Move(new Rook(Colour.BLACK), Square.h8,
               Square.f8));
      }
      return move;
   }

   public static Move castleQueensSide(King king) {
      Move move;
      if (king.getColour() == Colour.WHITE) {
         move = new Move(king, Square.e1, Square.c1);
         move.castlingInfo = new CastlingInfo(CastlingRights.QUEENS_SIDE, new Move(new Rook(Colour.WHITE), Square.a1,
               Square.d1));
      } else {
         move = new Move(king, Square.e8, Square.c8);
         move.castlingInfo = new CastlingInfo(CastlingRights.QUEENS_SIDE, new Move(new Rook(Colour.BLACK), Square.a8,
               Square.d8));
      }
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
      return promotionInfo != null;
   }

   public boolean isCastleKingsSide() {
      return ((castlingInfo != null) && (castlingInfo.direction == CastlingRights.KINGS_SIDE));
   }

   public boolean isCastleQueensSide() {
      return ((castlingInfo != null) && (castlingInfo.direction == CastlingRights.QUEENS_SIDE));
   }

   public Move getRooksCastlingMove() {
      return (castlingInfo != null) ? castlingInfo.rooksMove : null;
   }

   public PieceType getPromotedPiece() {
      return (promotionInfo != null) ? promotionInfo.promotedPiece : null;
   }

   public Square from() {
      return from;
   }

   public Square to() {
      return to;
   }

   static class CastlingInfo {
      public CastlingInfo(CastlingRights direction, Move rooksMove) {
         this.direction = direction;
         this.rooksMove = rooksMove;
      }

      private CastlingRights direction;
      private Move rooksMove;
   }

   static class PromotionInfo {
      public PromotionInfo(PieceType promotedPiece) {
         if ((promotedPiece == PieceType.PAWN) || (promotedPiece == PieceType.KING)) {
            throw new IllegalArgumentException("cannot promote to a pawn or king!");
         }
         this.promotedPiece = promotedPiece;
      }

      private PieceType promotedPiece;
   }
}
