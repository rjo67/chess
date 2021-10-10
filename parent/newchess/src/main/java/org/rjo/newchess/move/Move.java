package org.rjo.newchess.move;

import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

public class Move {
   private final int originSq;
   private final int targetSq;
   private final PieceType movingPiece;
   private final Colour colourOfMovingPiece;

   private final boolean capture;
   private final boolean promotion;
   private final boolean enPassant;
   private final boolean[] castling; // kings-side or queens-side castling

   private int originSquareInfo; // in same format as stored in Position
   private int targetSquareInfo; // used in captures; in same format as stored in Position
   private final PieceType promotedPiece;
   private boolean check; // whether this move is a check

   /**
    * @param origin           origin square
    * @param originSquareInfo 'raw' info of origin square
    * @param target           target square
    * @param targetSquareInfo 'raw' info of target square; -1 if not a capture
    * @param promotedPiece    promoted piece, set if promotion
    * @param castling         whether this was O-O or O-O-O
    * @param enPassant        set if enpassant
    */
   private Move(int origin, int originSquareInfo, int target, int targetSquareInfo, PieceType promotedPiece, boolean[] castling, boolean enPassant) {
      this.originSq = origin;
      this.targetSq = target;
      this.movingPiece = Position.decodePieceType(originSquareInfo);
      this.colourOfMovingPiece = Position.decodeColour(originSquareInfo);
      this.originSquareInfo = originSquareInfo;
      this.capture = targetSquareInfo != -1;
      this.targetSquareInfo = targetSquareInfo;
      this.promotion = promotedPiece != null;
      this.promotedPiece = promotedPiece;
      this.enPassant = enPassant;
      this.castling = castling;
   }

   /** normal move */
   public Move(int origin, int originSquareInfo, int target) {
      this(origin, originSquareInfo, target, -1, null, new boolean[2], false);
   }

   /** capture */
   public Move(int origin, int originSquareInfo, int target, int targetSquareInfo) {
      this(origin, originSquareInfo, target, targetSquareInfo, null, new boolean[2], false);
   }

   /**
    * Promotion (without capture).
    * 
    * @param origin           origin square
    * @param originSquareInfo info about origin square
    * @param target           target square
    * @param promotedPiece    the promoted piece
    */
   public static Move createPromotionMove(int origin, int originSquareInfo, int target, PieceType promotedPiece) {
      return new Move(origin, originSquareInfo, target, -1, promotedPiece, new boolean[2], false);
   }

   public static Move kingssideCastle(Position posn, int origin) {
      return new Move(origin, posn.raw(origin), -1, -1, null, new boolean[] { true, false }, false);
   }

   public static Move queenssideCastle(Position posn, int origin) {
      return new Move(origin, posn.raw(origin), -1, -1, null, new boolean[] { false, true }, false);
   }

   public static Move enpassant(Position posn, int origin, int epSquare) {
      return new Move(origin, posn.raw(origin), epSquare, posn.raw(epSquare), null, new boolean[2], true);
   }

   /**
    * Promotion (with capture).
    * 
    * @param origin           origin square
    * @param originSquareInfo info about origin square
    * @param target           target square
    * @param targetSquareInfo info about target square/captured piece
    * @param promotedPiece    the promoted piece
    */
   public static Move createPromotionCaptureMove(int origin, int originSquareInfo, int target, int targetSquareInfo, PieceType promotedPiece) {
      return new Move(origin, originSquareInfo, target, targetSquareInfo, promotedPiece, new boolean[2], false);
   }

   @Override
   public String toString() {
      if (isKingssideCastle()) {
         return "O-O";
      } else if (isQueenssideCastle()) {
         return "O-O-O";
      } else {
         StringBuilder sb = new StringBuilder(10);
         sb.append(movingPiece.symbol(colourOfMovingPiece));
         sb.append(Square.toSquare(originSq));
         sb.append(isCapture() ? "x" : "-");
         sb.append(Square.toSquare(targetSq));
         if (promotion) { sb.append("=").append(promotedPiece.symbol(colourOfMovingPiece)); }
         if (enPassant) { sb.append(" ep"); }
         if (check) { sb.append("+"); }
         return sb.toString();
      }
   }

   public boolean isCapture() {
      return capture;
   }

   public boolean isPromotion() {
      return promotion;
   }

   public boolean isEnPassant() {
      return enPassant;
   }

   public boolean isKingssideCastle() {
      return castling[0];
   }

   public boolean isQueenssideCastle() {
      return castling[1];
   }

   public PieceType getMovingPiece() {
      return movingPiece;
   }

   public Colour getColourOfMovingPiece() {
      return colourOfMovingPiece;
   }

   public int getOrigin() {
      return originSq;
   }

   public int getTarget() {
      return targetSq;
   }

   public void setCheck() {
      check = true;
   }

   public boolean isCheck() {
      return check;
   }

   public PieceType getPromotedPiece() {
      return promotedPiece;
   }

}
