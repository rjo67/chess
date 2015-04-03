package org.rjo.chess;

import java.util.BitSet;

/**
 * An abstraction to help deal with moves in all 8 compass directions.
 *
 * @author rich
 */
public interface MoveHelper {

   /**
    * how much the bitset gets shifted by a call to {@link #shiftBoard(BitSet)}.
    *
    * @return the increment per call to shiftBoard.
    */
   int getIncrement();

   /**
    * Shifts the board in the required direction.
    *
    * @param startBoard
    *           start board. Will not be changed by this method.
    * @return shifted board
    */
   BitSet shiftBoard(BitSet startBoard);

   /**
    * Shifts the board in the required direction.
    *
    * @param startBoard
    *           start board. <b>May be changed</b> by this method.
    * @param clone
    *           whether to clone 'startBoard'
    * @return shifted board
    */
   BitSet shiftBoard(BitSet startBoard, boolean clone);
}
