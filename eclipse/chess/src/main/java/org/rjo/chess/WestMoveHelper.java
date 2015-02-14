package org.rjo.chess;

import java.util.BitSet;

import org.rjo.chess.pieces.BitSetHelper;

/**
 * A {@link MoveHelper} implementation for a move to the West.
 * 
 * @author rich
 */
public class WestMoveHelper implements MoveHelper {
   private static MoveHelper instance;

   private WestMoveHelper() {
   }

   public static MoveHelper instance() {
      if (instance == null) {
         instance = new WestMoveHelper();
      }
      return instance;
   }

   @Override
   public int getIncrement() {
      return -1;
   }

   @Override
   public BitSet shiftBoard(BitSet startBoard) {
      return BitSetHelper.shiftOneWest(startBoard);
   }

}
