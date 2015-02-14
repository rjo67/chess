package org.rjo.chess;

import java.util.BitSet;

import org.rjo.chess.pieces.BitSetHelper;

/**
 * A {@link MoveHelper} implementation for a move to the North.
 * 
 * @author rich
 */
public class NorthMoveHelper implements MoveHelper {
   private static MoveHelper instance;

   private NorthMoveHelper() {
   }

   public static MoveHelper instance() {
      if (instance == null) {
         instance = new NorthMoveHelper();
      }
      return instance;
   }

   @Override
   public int getIncrement() {
      return 8;
   }

   @Override
   public BitSet shiftBoard(BitSet startBoard) {
      return BitSetHelper.shiftOneNorth(startBoard);
   }

}
