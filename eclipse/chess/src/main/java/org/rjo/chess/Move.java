package org.rjo.chess;

import org.rjo.chess.pieces.PieceType;

/**
 * Represents a move.
 *
 * @author rich
 */
public class Move {

   private Square from;
   private Square to;

   /** which piece is moving */
   private PieceType piece;
   /** colour of the piece */
   private Colour colour;

   /** capture info -- if not null, implies that this move was a capture */
   private CaptureInfo captureInfo;

   /** whether this move was a check */
   private boolean check;

   /** castling info -- if not null, implies that this move was 0-0 or 0-0-0 */
   private CastlingInfo castlingInfo;

   /** if promotion info -- if not null, implies that this move was a promotion */
   private PromotionInfo promotionInfo;
   /** true if enpassant move */
   private boolean enpassant;

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
   public Move(PieceType piece, Colour colour, Square from, Square to) {
      this(piece, colour, from, to, null);
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
    * @param capturedPiece
    *           the captured piece (null if not a capture)
    */
   public Move(PieceType piece, Colour colour, Square from, Square to, PieceType capturedPiece) {
      this(piece, colour, from, to, capturedPiece, false);
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
   public Move(PieceType piece, Colour colour, Square from, Square to, PieceType capturedPiece, boolean check) {
      this.piece = piece;
      this.colour = colour;
      this.from = from;
      this.to = to;
      if (capturedPiece != null) {
         this.captureInfo = new CaptureInfo(capturedPiece);
      }
      this.check = check;
      this.castlingInfo = null;
   }

   /**
    * Sets the promotion piece i/c of a pawn promotion.
    *
    * @param type
    *           to which piece the pawn gets promoted
    */
   public void setPromotionPiece(PieceType type) {
      if (piece != PieceType.PAWN) {
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
         sb.append(isCapture() ? "x" : "-");
         sb.append(to);
         sb.append(isPromotion() ? "=" + promotionInfo.promotedPiece.getSymbol() : "");
      }
      sb.append(check ? "+" : "");
      return sb.toString();
   }

   public static Move castleKingsSide(Colour colour) {
      Move move;
      if (Colour.WHITE == colour) {
         move = new Move(PieceType.KING, Colour.WHITE, Square.e1, Square.g1);
         move.castlingInfo = new CastlingInfo(CastlingRights.KINGS_SIDE, new Move(PieceType.ROOK, Colour.WHITE,
               Square.h1, Square.f1));
      } else {
         move = new Move(PieceType.KING, Colour.BLACK, Square.e8, Square.g8);
         move.castlingInfo = new CastlingInfo(CastlingRights.KINGS_SIDE, new Move(PieceType.ROOK, Colour.BLACK,
               Square.h8, Square.f8));
      }
      return move;
   }

   public static Move castleQueensSide(Colour colour) {
      Move move;
      if (Colour.WHITE == colour) {
         move = new Move(PieceType.KING, Colour.WHITE, Square.e1, Square.c1);
         move.castlingInfo = new CastlingInfo(CastlingRights.QUEENS_SIDE, new Move(PieceType.ROOK, Colour.WHITE,
               Square.a1, Square.d1));
      } else {
         move = new Move(PieceType.KING, Colour.BLACK, Square.e8, Square.c8);
         move.castlingInfo = new CastlingInfo(CastlingRights.QUEENS_SIDE, new Move(PieceType.ROOK, Colour.BLACK,
               Square.a8, Square.d8));
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
      return captureInfo != null;
   }

   public boolean isEnpassant() {
      return enpassant;
   }

   public PieceType getCapturedPiece() {
      if (isCapture()) {
         return captureInfo.capturedPiece;
      } else {
         throw new IllegalArgumentException("move was not a capture: " + toString());
      }
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

   public PieceType getPiece() {
      return piece;
   }

   public Colour getColour() {
      return colour;
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

   private static class CastlingInfo {
      public CastlingInfo(CastlingRights direction, Move rooksMove) {
         this.direction = direction;
         this.rooksMove = rooksMove;
      }

      private CastlingRights direction;
      private Move rooksMove;
   }

   private static class CaptureInfo {
      public CaptureInfo(PieceType capturedPiece) {
         this.capturedPiece = capturedPiece;
      }

      private PieceType capturedPiece;
   }

   private static class PromotionInfo {
      public PromotionInfo(PieceType promotedPiece) {
         if ((promotedPiece == PieceType.PAWN) || (promotedPiece == PieceType.KING)) {
            throw new IllegalArgumentException("cannot promote to a pawn or king!");
         }
         this.promotedPiece = promotedPiece;
      }

      private PieceType promotedPiece;
   }

   public void setEnpassant(boolean enpassant) {
      this.enpassant = enpassant;
   }
}
