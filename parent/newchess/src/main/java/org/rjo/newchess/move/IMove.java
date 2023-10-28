package org.rjo.newchess.move;

import java.util.ArrayList;
import java.util.List;

import org.rjo.newchess.game.Position.PieceSquareInfo;
import org.rjo.newchess.piece.Colour;

public interface IMove {

   default boolean isCheck() { return false; }

   default List<PieceSquareInfo> getCheckSquares() { return new ArrayList<>(); }

   byte getMovingPiece();

   int getOrigin();

   int getTarget();

   boolean isCapture();

   boolean isEnpassant();

   Colour getColourOfMovingPiece();

   boolean isPromotion();

   byte getPromotedPiece();

   boolean isKingssideCastling();

   boolean isQueenssideCastling();

   int getSquareOfPawnCapturedEnpassant();

   boolean isPawnTwoSquaresForward();

}
