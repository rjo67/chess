package org.rjo.chess;

import java.util.BitSet;

import org.rjo.chess.pieces.BitSetHelper;

/**
 * A {@link MoveHelper} implementation for a move to the East.
 * 
 * @author rich
 */
public class EastMoveHelper implements MoveHelper {

   private static MoveHelper instance;

   private EastMoveHelper() {
   }

   public static MoveHelper instance() {
      if (instance == null) {
         instance = new EastMoveHelper();
      }
      return instance;
   }

   @Override
   public int getIncrement() {
      return 1;
   }

   @Override
   public BitSet shiftBoard(BitSet startBoard) {
      return BitSetHelper.shiftOneEast(startBoard);
   }

}
