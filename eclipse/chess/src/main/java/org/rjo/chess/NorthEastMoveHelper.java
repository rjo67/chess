package org.rjo.chess;

import java.util.BitSet;

import org.rjo.chess.pieces.BitSetHelper;

/**
 * A {@link MoveHelper} implementation for a move to the Northeast.
 *
 * @author rich
 */
public class NorthEastMoveHelper implements MoveHelper {

   private static MoveHelper instance;

   private NorthEastMoveHelper() {
   }

   public static MoveHelper instance() {
      if (instance == null) {
         instance = new NorthEastMoveHelper();
      }
      return instance;
   }

   @Override
   public int getIncrement() {
      return 9;
   }

   @Override
   public BitSet shiftBoard(BitSet startBoard) {
      return shiftBoard(startBoard, true);
   }

   @Override
   public BitSet shiftBoard(BitSet startBoard, boolean clone) {
      return BitSetHelper.shiftOneNorthEast(startBoard);// TODO, clone);
   }

}
