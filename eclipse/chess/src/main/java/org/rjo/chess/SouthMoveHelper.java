package org.rjo.chess;

import java.util.BitSet;

import org.rjo.chess.pieces.BitSetHelper;

/**
 * A {@link MoveHelper} implementation for a move to the South.
 * 
 * @author rich
 */
public class SouthMoveHelper implements MoveHelper {
   private static MoveHelper instance;

   private SouthMoveHelper() {
   }

   public static MoveHelper instance() {
      if (instance == null) {
         instance = new SouthMoveHelper();
      }
      return instance;
   }

   @Override
   public int getIncrement() {
      return -8;
   }

   @Override
   public BitSet shiftBoard(BitSet startBoard) {
      return BitSetHelper.shiftOneSouth(startBoard);
   }

}
