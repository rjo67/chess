package org.rjo.chess.ray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rjo.chess.Colour;
import org.rjo.chess.Square;

public class RayInfo {
   private List<Integer> emptySquares = new ArrayList<>(7); // list of empty squares in this ray's direction
   private int indexOfPiece = -1; // index of where the piece was found (-1 == no piece found)
   private Colour colour; // colour of the piece (only if pieceFoundAtIndex!=-1)
   private int distance; // distance from start square (only if pieceFoundAtIndex!=-1)

   public void storePiece(int sqIndex, Colour colour, int distance) {
      if (this.indexOfPiece != -1) {
         throw new IllegalStateException("cannot call storePiece twice");
      }
      this.indexOfPiece = sqIndex;
      this.colour = colour;
      this.distance = distance;
   }

   public int getIndexOfPiece() {
      return indexOfPiece;
   }

   public Colour getColour() {
      return colour;
   }

   public int getDistance() {
      return distance;
   }

   public void addEmptySquare(Square square) {
      addEmptySquare(square.bitIndex());
   }

   public void addEmptySquare(int squareIndex) {
      this.emptySquares.add(squareIndex);
   }

   public List<Integer> getEmptySquares() {
      return Collections.unmodifiableList(emptySquares);
   }

   public boolean foundPiece() {
      return this.indexOfPiece != -1;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(100);
      sb.append("emptySquares: ").append(emptySquares);
      if (foundPiece()) {
         sb.append(", piece: ").append(colour).append("@").append(indexOfPiece).append("(d=").append(distance)
               .append(")");
      }
      return sb.toString();
   }

}
