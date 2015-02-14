package chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import chess.Chessboard;
import chess.Colour;
import chess.Move;
import chess.Square;

/**
 * Stores information about the rooks (still) in the game.
 * 
 * @author rich
 */
public class Rook extends Piece {

   private static MoveHelper NORTH_MOVE_HELPER = new NorthMoveHelper();
   private static MoveHelper SOUTH_MOVE_HELPER = new SouthMoveHelper();
   private static MoveHelper WEST_MOVE_HELPER = new WestMoveHelper();
   private static MoveHelper EAST_MOVE_HELPER = new EastMoveHelper();

   /**
    * Constructs the Rook class with the default start squares.
    * 
    * @param colour
    *           indicates the colour of the pieces
    */
   public Rook(Colour colour) {
      this(colour, (Square[]) null);
   }

   /**
    * Constructs the Rook class with the default start squares.
    * 
    * @param colour
    *           indicates the colour of the pieces
    * @param startSquares
    *           the required starting squares of the piece(s). Can be null, in which case the default start squares are
    *           used. (In this case see the alternative constructor {@link #Rook(Colour)}.)
    */
   public Rook(Colour colour, Square... startSquares) {
      super(colour, PieceType.ROOK);
      if (startSquares == null) {
         initPosition();
      } else {
         initPosition(startSquares);
      }
   }

   @Override
   public void initPosition() {
      Square[] requiredSquares = null;
      switch (colour) {
      case WHITE:
         requiredSquares = new Square[] { Square.a1, Square.h1 };
         break;
      case BLACK:
         requiredSquares = new Square[] { Square.a8, Square.h8 };
         break;
      }
      initPosition(requiredSquares);
   }

   @Override
   public List<Move> findMoves(Chessboard chessboard) {
      List<Move> moves = new ArrayList<>(14);

      /*
       * search for moves in directions N, S, W, and E
       */
      moves.addAll(search(chessboard, NORTH_MOVE_HELPER));
      moves.addAll(search(chessboard, SOUTH_MOVE_HELPER));
      moves.addAll(search(chessboard, WEST_MOVE_HELPER));
      moves.addAll(search(chessboard, EAST_MOVE_HELPER));
      return moves;
   }

   private List<Move> search(Chessboard chessboard, MoveHelper moveHelper) {
      List<Move> moves = new ArrayList<>(7);

      /*
       * in each iteration, shifts the board in the required direction and checks for friendly pieces and captures,
       */
      BitSet shiftedBoard = pieces.getBitSet();
      int offset = 0;
      final int increment = moveHelper.getIncrement();
      while (!shiftedBoard.isEmpty()) {
         offset += increment;
         shiftedBoard = moveHelper.shiftBoard(shiftedBoard);
         // move must be to an empty square or a capture of an enemy piece,
         // therefore remove squares with friendly pieces
         shiftedBoard.andNot(chessboard.getAllPieces(getColour()).getBitSet());

         /*
          * check for captures in 'shiftedBoard'.
          * If any found, remove from 'shiftedBoard' before next iteration.
          */
         BitSet captures = (BitSet) shiftedBoard.clone();
         captures.and(chessboard.getAllPieces(Colour.oppositeColour(getColour())).getBitSet());
         for (int i = captures.nextSetBit(0); i >= 0; i = captures.nextSetBit(i + 1)) {
            moves.add(new Move(this, Square.fromBitPosn(i - offset), Square.fromBitPosn(i), true));
            // remove capture square from 'shiftedBoard'
            shiftedBoard.clear(i);
         }
         /*
          * store any remaining moves.
          */
         for (int i = shiftedBoard.nextSetBit(0); i >= 0; i = shiftedBoard.nextSetBit(i + 1)) {
            moves.add(new Move(this, Square.fromBitPosn(i - offset), Square.fromBitPosn(i)));
         }
      }

      return moves;
   }

   private interface MoveHelper {

      /**
       * by how much the bitset has been shifted by a call to {@link #shiftBoard(BitSet)}.
       * 
       * @return the increment per call to shiftBoard.
       */
      int getIncrement();

      /**
       * Shifts the board in the required direction.
       * 
       * @param startBoard
       *           start board
       * @return shifted board
       */
      BitSet shiftBoard(BitSet startBoard);
   }

   private static class NorthMoveHelper implements MoveHelper {

      @Override
      public int getIncrement() {
         return 8;
      }

      @Override
      public BitSet shiftBoard(BitSet startBoard) {
         return BitSetHelper.shiftOneNorth(startBoard);
      }

   }

   private static class SouthMoveHelper implements MoveHelper {

      @Override
      public int getIncrement() {
         return -8;
      }

      @Override
      public BitSet shiftBoard(BitSet startBoard) {
         return BitSetHelper.shiftOneSouth(startBoard);
      }

   }

   private static class WestMoveHelper implements MoveHelper {

      @Override
      public int getIncrement() {
         return -1;
      }

      @Override
      public BitSet shiftBoard(BitSet startBoard) {
         return BitSetHelper.shiftOneWest(startBoard);
      }

   }

   private static class EastMoveHelper implements MoveHelper {

      @Override
      public int getIncrement() {
         return 1;
      }

      @Override
      public BitSet shiftBoard(BitSet startBoard) {
         return BitSetHelper.shiftOneEast(startBoard);
      }

   }
}
