package org.rjo.newchess.move;

import java.util.Arrays;
import java.util.List;

import org.rjo.newchess.game.Position.PieceSquareInfo;
import org.rjo.newchess.piece.Colour;

/**
 * Wrapper for a Move, storing check information.
 */
public class CheckMove implements IMove {

   private final IMove move;
   private final List<PieceSquareInfo> checkSquares; // set to the square(s) of the piece(s) delivering a check

   public CheckMove(IMove move, PieceSquareInfo... checkSquares) {
      this(move, Arrays.asList(checkSquares));
   }

   public CheckMove(IMove move, List<PieceSquareInfo> checkSquares) {
      this.move = move;
      this.checkSquares = checkSquares;
   }

   @Override
   public boolean isCheck() { return true; }

   @Override
   public List<PieceSquareInfo> getCheckSquares() { return checkSquares; }

   @Override
   public byte getMovingPiece() { return move.getMovingPiece(); }

   @Override
   public int getOrigin() { return move.getOrigin(); }

   @Override
   public int getTarget() { return move.getTarget(); }

   @Override
   public boolean isCapture() { return move.isCapture(); }

   @Override
   public boolean isEnpassant() { return move.isEnpassant(); }

   @Override
   public Colour getColourOfMovingPiece() { return move.getColourOfMovingPiece(); }

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
      return move.toString() + "+";
   }
}
