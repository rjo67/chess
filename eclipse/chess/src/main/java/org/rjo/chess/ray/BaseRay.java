package org.rjo.chess.ray;

import java.util.Iterator;

import org.rjo.chess.Square;
import org.rjo.chess.pieces.PieceType;

public abstract class BaseRay implements Ray {

   protected PieceType[] piecesThatCanGiveCheckOnThisRay;

   protected BaseRay(PieceType[] pieceTypes) {
      this.piecesThatCanGiveCheckOnThisRay = pieceTypes;
   }

   @Override
   public Iterator<Integer> squaresFrom(Square startSquare) {
      return squaresFrom(startSquare.bitIndex());
   }

   @Override
   public boolean isRelevantPieceForDiscoveredCheck(PieceType piece) {
      for (PieceType pt : piecesThatCanGiveCheckOnThisRay) {
         if (pt == piece) {
            return true;
         }
      }
      return false;
   }

}
