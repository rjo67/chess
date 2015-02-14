package org.rjo.chess;

import java.util.BitSet;

import org.rjo.chess.pieces.BitSetHelper;

/**
 * A {@link MoveHelper} implementation for a move to the Southeast.
 * 
 * @author rich
 */
public class SouthEastMoveHelper implements MoveHelper {
   private static MoveHelper instance;

   private SouthEastMoveHelper() {
   }

   public static MoveHelper instance() {
      if (instance == null) {
         instance = new SouthEastMoveHelper();
      }
      return instance;
   }

   @Override
   public int getIncrement() {
      return -7;
   }

   @Override
   public BitSet shiftBoard(BitSet startBoard) {
      return BitSetHelper.shiftOneSouthEast(startBoard);
   }

}
