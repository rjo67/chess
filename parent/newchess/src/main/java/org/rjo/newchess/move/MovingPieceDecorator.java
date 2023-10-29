package org.rjo.newchess.move;

import org.rjo.newchess.piece.Pieces;

/**
 * Decorator for a Move, storing information of the moving piece.
 */
public class MovingPieceDecorator implements IMove {

   private final IMove move;
   private final byte movingPiece;

   public MovingPieceDecorator(IMove move, byte movingPiece) {
      this.move = move;
      this.movingPiece = movingPiece;
   }

   @Override
   public byte getMovingPiece() { return movingPiece; }

   @Override
   public int getOrigin() { return move.getOrigin(); }

   @Override
   public int getTarget() { return move.getTarget(); }

   @Override
   public boolean isCapture() { return move.isCapture(); }

   @Override
   public boolean isEnpassant() { return move.isEnpassant(); }

   @Override
   public boolean isPromotion() { return move.isPromotion(); }

   @Override
   public byte getPromotedPiece() { return move.getPromotedPiece(); }

   @Override
   public boolean isKingssideCastling() { return move.isKingssideCastling(); }

   @Override
   public boolean isQueenssideCastling() { return move.isQueenssideCastling(); }

   @Override
   public int getSquareOfPawnCapturedEnpassant() { return move.getSquareOfPawnCapturedEnpassant(); }

   @Override
   public boolean isPawnTwoSquaresForward() { return move.isPawnTwoSquaresForward(); }

   @Override
   public String toString() {
      // movingPiece is appended at the start of the move string (but not for castling)
      if (isKingssideCastling() || isQueenssideCastling()) {
         return move.toString();
      } else {
         return Pieces.symbol(movingPiece) + move.toString();
      }
   }

   @Override
   public boolean moveCapturesPiece(int captureSquare) {
      return move.moveCapturesPiece(captureSquare);
   }
}
