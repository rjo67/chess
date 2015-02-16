package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.rjo.chess.BitBoard;
import org.rjo.chess.Colour;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.Square;

/**
 * Stores information about the knights (still) in the game.
 * 
 * @author rich
 */
public class Knight extends Piece {

   /**
    * Stores for each square on the board the possible moves for a knight on that square.
    */
   private static BitSet[] knightMoves = new BitSet[64];

   // set up knight moves look up table
   static {
      for (int i = 0; i < 64; i++) {
         knightMoves[i] = new BitSet(64);
         knightMoves[i].set(i);
         /*
          * LHS: blank first file for -10 and +6
          * - blank first and 2nd file for -17 and +15
          * RHS: blank last file for +10 and -6
          * - blank 7th and 8th file for +17 and -15
          * 
          * Don't need to blank ranks, these just 'drop off' during the bit shift.
          */

         BitSet[] work = new BitSet[8];

         // to avoid wrapping:
         // - work[0,1] == file one blanked
         // - work[2,3] == file two blanked as well
         // - work[4,5] == file 8 blanked
         // - work[6,7] == file 7 blanked as well
         work[0] = (BitSet) knightMoves[i].clone();
         work[0].and(BitBoard.NOT_FILE_ONE.getBitSet());
         work[2] = (BitSet) work[0].clone();
         work[2].and(BitBoard.NOT_FILE_TWO.getBitSet());

         // store another copy
         work[1] = (BitSet) work[0].clone();
         work[3] = (BitSet) work[2].clone();

         work[0] = BitSetHelper.shift(work[0], 15); // file-1,rank+2
         work[1] = BitSetHelper.shift(work[1], -17);// file-1,rank-2
         work[2] = BitSetHelper.shift(work[2], 6);// file-2,rank+1
         work[3] = BitSetHelper.shift(work[3], -10);// file-2,rank-1

         work[4] = (BitSet) knightMoves[i].clone();
         work[4].and(BitBoard.NOT_FILE_EIGHT.getBitSet());
         work[6] = (BitSet) work[4].clone();
         work[6].and(BitBoard.NOT_FILE_SEVEN.getBitSet());

         // store another copy
         work[5] = (BitSet) work[4].clone();
         work[7] = (BitSet) work[6].clone();

         work[4] = BitSetHelper.shift(work[4], 17); // file+1,rank+2
         work[5] = BitSetHelper.shift(work[5], -15);// file+1,rank-2
         work[6] = BitSetHelper.shift(work[6], 10);// file+2,rank+1
         work[7] = BitSetHelper.shift(work[7], -6);// file+2,rank-1

         // store results
         knightMoves[i].clear(i); // clear the start position
         for (int j = 0; j < work.length; j++) {
            knightMoves[i].or(work[j]);
         }
      }
   }

   /**
    * Constructs the Knight class with the default start squares.
    * 
    * @param colour
    *           indicates the colour of the pieces
    */
   public Knight(Colour colour) {
      this(colour, (Square[]) null);
   }

   /**
    * Constructs the Knight class with the default start squares.
    * 
    * @param colour
    *           indicates the colour of the pieces
    * @param startSquares
    *           the required starting squares of the piece(s). Can be null, in which case the default start squares are
    *           used. (In this case see the alternative constructor {@link #Knight(Colour)}.)
    */
   public Knight(Colour colour, Square... startSquares) {
      super(colour, PieceType.KNIGHT);
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
         requiredSquares = new Square[] { Square.b1, Square.g1 };
         break;
      case BLACK:
         requiredSquares = new Square[] { Square.b8, Square.g8 };
         break;
      }
      initPosition(requiredSquares);
   }

   @Override
   public List<Move> findMoves(Game game) {
      List<Move> moves = new ArrayList<>(20);
      /*
       * for each knight on the board, finds its moves using the lookup table
       */
      for (int i = pieces.getBitSet().nextSetBit(0); i >= 0; i = pieces.getBitSet().nextSetBit(i + 1)) {
         BitSet possibleMoves = (BitSet) knightMoves[i].clone();
         // move can't be to a square with a piece of the same colour on it
         possibleMoves.andNot(game.getChessboard().getAllPieces(colour).getBitSet());

         Square knightStartSquare = Square.fromBitPosn(i);
         /*
          * check for captures in 'possibleMoves'.
          * If any found, remove from 'possibleMoves' before next iteration.
          */
         BitSet captures = (BitSet) possibleMoves.clone();
         captures.and(game.getChessboard().getAllPieces(Colour.oppositeColour(getColour())).getBitSet());
         for (int j = captures.nextSetBit(0); j >= 0; j = captures.nextSetBit(j + 1)) {
            moves.add(new Move(this, knightStartSquare, Square.fromBitPosn(j), true));
            // remove capture square
            possibleMoves.clear(j);
         }
         /*
          * store any remaining moves.
          */
         for (int k = possibleMoves.nextSetBit(0); k >= 0; k = possibleMoves.nextSetBit(k + 1)) {
            moves.add(new Move(this, knightStartSquare, Square.fromBitPosn(k)));
         }
      }
      return moves;
   }

}
