package chess.pieces;

import java.util.List;

import chess.BitBoard;
import chess.Chessboard;
import chess.Colour;
import chess.Move;
import chess.Square;

/**
 * Stores information about the queens (still) in the game.
 * 
 * @author rich
 */
public class Queen extends BasePiece {

   /**
    * Creates the data structures for the starting position of the queen.
    * 
    * @param side
    *           used to determine the starting position for the pieces
    */
   public Queen(Colour side) {
      super(side, side.toString() + " Queen");

      pieces = new BitBoard();
      switch (side) {
      case WHITE:
         pieces.setBitsAt(Square.d1);
         break;
      case BLACK:
         pieces.setBitsAt(Square.d8);
         break;
      }
   }

   @Override
   public String getSymbol() {
      return "Q";
   }

   @Override
   public List<Move> findMoves(Chessboard chessboard) {
      // TODO Auto-generated method stub
      return null;
   }

}
