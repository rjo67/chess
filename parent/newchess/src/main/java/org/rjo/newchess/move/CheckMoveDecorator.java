package org.rjo.newchess.move;

import java.util.Arrays;
import java.util.List;

import org.rjo.newchess.game.Position.PieceSquareInfo;

/**
 * Decorator for a Move, storing check information.
 */
public class CheckMoveDecorator implements IMove {

   private final IMove move;
   private final List<PieceSquareInfo> checkSquares; // set to the square(s) of the piece(s) delivering a check

   public CheckMoveDecorator(IMove move, PieceSquareInfo... checkSquares) {
      this(move, Arrays.asList(checkSquares));
   }

   public CheckMoveDecorator(IMove move, List<PieceSquareInfo> checkSquares) {
      this.move = move;
      this.checkSquares = checkSquares;
   }

   @Override
   public boolean isCheck() { return true; }

   @Override
   public List<PieceSquareInfo> getCheckSquares() { return checkSquares; }

   // public byte getMovingPiece() { return move.getMovingPiece(); }

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
      // check symbol is appended at the end of the move string
      return move.toString() + "+";
   }

   @Override
   public boolean moveCapturesPiece(int captureSquare) {
      return move.moveCapturesPiece(captureSquare);
   }
}
