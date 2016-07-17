package org.rjo.chess.ray;

import java.util.Iterator;

import org.rjo.chess.Square;
import org.rjo.chess.pieces.PieceType;

public abstract class BaseRay implements Ray {

   private RayType rayType;
   private boolean diagonal;
   protected PieceType[] piecesThatCanGiveCheckOnThisRay;

   protected BaseRay(RayType rayType, boolean diagonal, PieceType[] pieceTypes) {
      this.rayType = rayType;
      this.diagonal = diagonal;
      this.piecesThatCanGiveCheckOnThisRay = pieceTypes;
   }

   @Override
   public RayType getRayType() {
      return rayType;
   }

   @Override
   public boolean isDiagonal() {
      return diagonal;
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
