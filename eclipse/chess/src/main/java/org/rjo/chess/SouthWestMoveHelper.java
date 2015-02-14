package org.rjo.chess;

import java.util.BitSet;

import org.rjo.chess.pieces.BitSetHelper;

/**
 * A {@link MoveHelper} implementation for a move to the Southwest.
 * 
 * @author rich
 */
public class SouthWestMoveHelper implements MoveHelper {
   private static MoveHelper instance;

   private SouthWestMoveHelper() {
   }

   public static MoveHelper instance() {
      if (instance == null) {
         instance = new SouthWestMoveHelper();
      }
      return instance;
   }

   @Override
   public int getIncrement() {
      return -9;
   }

   @Override
   public BitSet shiftBoard(BitSet startBoard) {
      return BitSetHelper.shiftOneSouthWest(startBoard);
   }

}
