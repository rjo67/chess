package org.rjo.newchess.piece;

import java.util.List;

public interface Piece {

   /**
    * max pieces per piece type
    */
   final static int MAX_PIECES = 10;

   Colour getColour();

   List<Integer> getPositions();

   int[] getMoveOffsets();

   void add(int position);

   boolean isSlidingPiece();

   String name();

   int type();
}
