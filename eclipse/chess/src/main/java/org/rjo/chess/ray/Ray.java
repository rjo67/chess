package org.rjo.chess.ray;

import java.util.Iterator;

import org.rjo.chess.Square;
import org.rjo.chess.pieces.PieceType;

public interface Ray {

   public Iterator<Integer> squaresFrom(Square startSquare);

   public Iterator<Integer> squaresFrom(int startSquareIndex);

   public boolean isRelevantPieceForDiscoveredCheck(PieceType piece);

   public RayType getRayType();

   public boolean isDiagonal();
}
