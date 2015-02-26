package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.rjo.chess.BitBoard;
import org.rjo.chess.Chessboard;
import org.rjo.chess.Colour;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.MoveHelper;
import org.rjo.chess.Square;

/**
 * Represents the pieces which can move over a greater distance: rooks, bishops, queens.
 * 
 * @author rich
 */
public abstract class SlidingPiece extends Piece {

   protected SlidingPiece(Colour colour, PieceType type) {
      super(colour, type);
   }

   /**
    * Searches for moves in the direction specified by the {@link MoveHelper} implementation.
    * This is for rooks, bishops, and queens.
    * 
    * @param chessboard
    *           state of the board
    * @param moveHelper
    *           move helper object, see {@link MoveHelper}.
    * @return the moves found
    */
   protected List<Move> search(Chessboard chessboard, MoveHelper moveHelper) {
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

   /**
    * Checks if the given move would place the opponent's king in check, i.e. the destination square of the move attacks
    * (diagonally) the location of the king.
    * <p>
    * This is for bishop-type moves.
    * 
    * @param game
    *           the game
    * @param move
    *           the move
    * @param opponentsKing
    *           where the opponent's king is
    * @return true if this move is a check
    */
   protected boolean findDiagonalCheck(Game game, Move move, Square opponentsKing) {
      return attacksSquareDiagonally(game.getChessboard(), move.to(), opponentsKing);
   }

   /**
    * Checks if a bishop/queen on the given startSquare attacks the given targetSquare.
    * 
    * @param chessboard
    *           the board
    * @param startSquare
    *           start square
    * @param targetSquare
    *           target square
    * @return true if the target square is attacked (diagonally) from the start square.
    */
   protected boolean attacksSquareDiagonally(Chessboard chessboard, Square startSquare, Square targetSquare) {
      if (!onSameDiagonal(startSquare, targetSquare)) {
         return false;
      }
      int rankOffset = startSquare.rank() > targetSquare.rank() ? -1 : 1;
      int fileOffset = startSquare.file() > targetSquare.file() ? -1 : 1;
      int bitPosn = startSquare.bitPosn();
      boolean reachedTargetSquare = false;
      boolean foundNonEmptySquare = false;
      while (!reachedTargetSquare && !foundNonEmptySquare) {
         bitPosn += (8 * rankOffset) + fileOffset;
         if (bitPosn == targetSquare.bitPosn()) {
            reachedTargetSquare = true;
         } else if (!chessboard.getEmptySquares().getBitSet().get(bitPosn)) {
            foundNonEmptySquare = true;
         }
      }
      return reachedTargetSquare;
   }

   /**
    * Checks if the given move would place the opponent's king in check, i.e. the destination square of the move attacks
    * the location of the king along a rank or file.
    * <p>
    * This is for rook-type moves.
    * 
    * @param game
    *           the game
    * @param move
    *           the move
    * @param opponentsKing
    *           where the opponent's king is
    * @return true if this move is a check
    */
   protected boolean findRankOrFileCheck(Game game, Move move, Square opponentsKing) {
      return attacksSquareRankOrFile(game.getChessboard(), move.to(), opponentsKing);
   }

   /**
    * Checks if a rook/queen on the given startSquare attacks the given targetSquare.
    * This is for rook-type moves i.e. straight along files or ranks.
    * 
    * @param chessboard
    *           the board
    * @param startSquare
    *           start square
    * @param targetSquare
    *           target square
    * @return true if the target square is attacked (diagonally) from the start square.
    */
   protected boolean attacksSquareRankOrFile(Chessboard chessboard, Square startSquare, Square targetSquare) {
      // for a rook to give check, it must be on the same rank or file as the king
      // and there can't be any pieces in between

      BitSet squaresInBetween = new BitSet(64);
      int nbrOfSquaresInBetween = 0;
      boolean onSameRankOrFile = false;

      if (startSquare.rank() == targetSquare.rank()) {
         onSameRankOrFile = true;
         // set squares between rook and king on this rank
         int[] orderedNumbers = orderNumbers(startSquare.file(), targetSquare.file());
         int offset = Square.fromRankAndFile(startSquare.rank(), 0).bitPosn();
         for (int i = orderedNumbers[0] + 1; i < orderedNumbers[1]; i++) {
            nbrOfSquaresInBetween++;
            squaresInBetween.set(offset + i);
         }
      } else if (startSquare.file() == targetSquare.file()) {
         onSameRankOrFile = true;
         // set squares between rook and king on this rank
         int[] orderedNumbers = orderNumbers(startSquare.rank(), targetSquare.rank());
         int offset = Square.fromRankAndFile(0, startSquare.file()).bitPosn();
         for (int i = orderedNumbers[0] + 1; i < orderedNumbers[1]; i++) {
            nbrOfSquaresInBetween++;
            squaresInBetween.set(offset + i * 8);
         }
      }

      if (!onSameRankOrFile) {
         return false;
      }

      /*
       * squaresInBetween has the squares between rook and king. nbrOfSquaresInBetween == the number of set bits.
       * Now intersect with the appropriate part of the 'emptySquares' bitset to see if these squares are empty.
       */
      BitSet emptySquares = chessboard.getEmptySquares().cloneBitSet();
      // remove unwanted ranks and files from 'emptySquares'
      for (int i = 0; i < Math.min(startSquare.file(), targetSquare.file()); i++) {
         emptySquares.and(BitBoard.EXCEPT_FILE[i].getBitSet());
      }
      for (int i = 7; i > Math.max(startSquare.file(), targetSquare.file()); i--) {
         emptySquares.and(BitBoard.EXCEPT_FILE[i].getBitSet());
      }
      for (int i = 0; i < Math.min(startSquare.rank(), targetSquare.rank()); i++) {
         emptySquares.and(BitBoard.EXCEPT_RANK[i].getBitSet());
      }
      for (int i = 7; i > Math.max(startSquare.rank(), targetSquare.rank()); i--) {
         emptySquares.and(BitBoard.EXCEPT_RANK[i].getBitSet());
      }
      // remove the new position of the moved piece from 'emptySquares'
      emptySquares.clear(startSquare.bitPosn());
      squaresInBetween.and(emptySquares);
      return squaresInBetween.cardinality() == nbrOfSquaresInBetween;
   }

   /**
    * Orders two numbers.
    * 
    * @param num1
    *           first number
    * @param num2
    *           second number
    * @return an array with the first element the smaller number, and the 2nd element the larger number
    */
   private int[] orderNumbers(int num1, int num2) {
      int[] res = new int[2];
      if (num1 < num2) {
         res[0] = num1;
         res[1] = num2;
      } else {
         res[0] = num2;
         res[1] = num1;
      }
      return res;
   }

   private boolean onSameDiagonal(Square sq1, Square sq2) {
      return Math.abs(sq1.rank() - sq2.rank()) == Math.abs(sq1.file() - sq2.file());
   }

}
