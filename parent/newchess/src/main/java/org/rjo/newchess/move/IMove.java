package org.rjo.newchess.move;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.rjo.newchess.game.Position.PieceSquareInfo;

public interface IMove {

   default boolean isCheck() { return false; }

   default List<PieceSquareInfo> getCheckSquares() { return new ArrayList<>(); }

   default byte getMovingPiece() {
      throw new NotImplementedException("cannot call getMovingPiece on a normal move");
   }

   int getOrigin();

   int getTarget();

   boolean isCapture();

   boolean isEnpassant();

   boolean isPromotion();

   byte getPromotedPiece();

   boolean isKingssideCastling();

   boolean isQueenssideCastling();

   int getSquareOfPawnCapturedEnpassant();

   boolean isPawnTwoSquaresForward();

   public boolean moveCapturesPiece(int captureSquare);

}
