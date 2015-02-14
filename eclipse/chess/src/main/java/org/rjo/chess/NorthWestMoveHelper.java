package org.rjo.chess;

import java.util.BitSet;

import org.rjo.chess.pieces.BitSetHelper;

/**
 * A {@link MoveHelper} implementation for a move to the Northwest.
 * 
 * @author rich
 */
public class NorthWestMoveHelper implements MoveHelper {
   private static MoveHelper instance;

   private NorthWestMoveHelper() {
   }

   public static MoveHelper instance() {
      if (instance == null) {
         instance = new NorthWestMoveHelper();
      }
      return instance;
   }

   @Override
   public int getIncrement() {
      return 7;
   }

   @Override
   public BitSet shiftBoard(BitSet startBoard) {
      return BitSetHelper.shiftOneNorthWest(startBoard);
   }

}
