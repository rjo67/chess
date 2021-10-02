package org.rjo.newchess.move;

import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

public class Move {
   private final int origin;
   private final int target;

   private final boolean capture;
   private final boolean promotion;
   private final boolean[] castling;

   private int originSquareInfo; // in same format as stored in Position
   private int targetSquareInfo; // used in captures; in same format as stored in Position
   private final PieceType promotedPiece;

   private Move(int origin, int originSquareInfo, int target, int targetSquareInfo, PieceType promotedPiece, boolean[] castling) {
      this.origin = origin;
      this.target = target;
      this.originSquareInfo = originSquareInfo;
      this.capture = targetSquareInfo != -1;
      this.targetSquareInfo = targetSquareInfo;
      this.promotion = promotedPiece != null;
      this.promotedPiece = promotedPiece;
      this.castling = castling;
   }

   /**
    * Promotion (with optional capture).
    * 
    * @param origin           origin square
    * @param originSquareInfo info about origin square
    * @param target           target square
    * @param targetSquareInfo info about target square/captured piece or -1
    * @param promotedPiece    the promoted piece or null
    */
   public Move(int origin, int originSquareInfo, int target, int targetSquareInfo, PieceType promotedPiece) {
      this(origin, originSquareInfo, target, targetSquareInfo, promotedPiece, new boolean[2]);
   }

   /** normal move */
   public Move(int origin, int originSquareInfo, int target) {
      this(origin, originSquareInfo, target, -1, null);
   }

   /** capture */
   public Move(int origin, int originSquareInfo, int target, int targetSquareInfo) {
      this(origin, originSquareInfo, target, targetSquareInfo, null);
   }

   @Override
   public String toString() {
      if (isKingssideCastle()) {
         return "O-O";
      } else if (isQueenssideCastle()) {
         return "O-O-O";
      } else {
         StringBuilder sb = new StringBuilder(10);
         Colour col = Position.decodeColour(originSquareInfo);
         PieceType pt = Position.decodePieceType(originSquareInfo);
         sb.append(pt.symbol(col));
         sb.append(Square.toSquare(origin));
         sb.append(isCapture() ? "x" : "-");
         sb.append(Square.toSquare(target));
         if (isPromotion()) {
            sb.append("=").append(promotedPiece.symbol(col));
         }
         return sb.toString();
      }
   }

   private boolean isCapture() {
      return capture;
   }

   public boolean isPromotion() {
      return promotion;
   }

   public boolean isKingssideCastle() {
      return castling[0];
   }

   public boolean isQueenssideCastle() {
      return castling[1];
   }

   public PieceType getMovingPiece() {
      return Position.decodePieceType(originSquareInfo);
   }

   public static Move kingssideCastle(Position posn, int origin) {
      return new Move(origin, posn.raw(origin), -1, -1, null, new boolean[] { true, false });
   }

   public static Move queenssideCastle(Position posn, int origin) {
      return new Move(origin, posn.raw(origin), -1, -1, null, new boolean[] { false, true });
   }
}
