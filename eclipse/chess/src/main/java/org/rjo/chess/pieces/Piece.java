package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.rjo.chess.BitBoard;
import org.rjo.chess.Chessboard;
import org.rjo.chess.Colour;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.MoveHelper;
import org.rjo.chess.Square;

public abstract class Piece {

   // type of this piece
   private PieceType type;

   // stores position of the piece(s) of a particular kind (queen, pawns, ...)
   protected BitBoard pieces;

   // Stores for each square a bitboard containing all possible moves.
   // first dimension is the rank, 2nd the file
   protected BitBoard[][] moveBitBoards;

   // stores the colour of the piece
   protected Colour colour;

   /**
    * @return the symbol for this piece.
    */
   public String getSymbol() {
      return type.getSymbol();
   }

   /**
    * Initialises data structures to the starting position of the pieces.
    * 
    * @see #initPosition(Square...).
    */
   abstract public void initPosition();

   /**
    * Finds all possible moves for this piece type in the given game.
    * 
    * @param game
    *           current game state.
    * @return a list of all possible moves.
    */
   abstract public List<Move> findMoves(Game game);

   protected Piece(Colour colour, PieceType type) {
      this.colour = colour;
      this.type = type;
   }

   public Colour getColour() {
      return colour;
   }

   public BitBoard getBitBoard() {
      return pieces;
   }

   /**
    * Sets the start squares for this piece type to the parameter(s).
    * 
    * @param requiredSquares
    *           all required squares.
    */
   public void initPosition(Square... requiredSquares) {
      pieces = new BitBoard();
      pieces.setBitsAt(requiredSquares);
   }

   /**
    * Returns all the squares currently occupied by this piece type.
    * 
    * @return the squares currently occupied by this piece type
    */
   public Square[] getLocations() {
      Set<Square> set = new HashSet<>();
      for (int i = pieces.getBitSet().nextSetBit(0); i >= 0; i = pieces.getBitSet().nextSetBit(i + 1)) {
         set.add(Square.fromBitPosn(i));
      }
      return set.toArray(new Square[set.size()]);
   }

   /**
    * Returns the FEN symbol for this piece. Delegates to {@link PieceType#getFenSymbol(Colour)}.
    * 
    * @return the FEN symbol for this piece.
    */
   public String getFenSymbol() {
      return type.getFenSymbol(colour);
   }

   // for test
   BitBoard[][] getMoveBitBoards() {
      return moveBitBoards;
   }

   @Override
   public String toString() {
      return colour.toString() + " " + type.toString();
   }

   public PieceType getType() {
      return type;
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

}
