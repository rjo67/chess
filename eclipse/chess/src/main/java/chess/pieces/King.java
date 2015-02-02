package chess.pieces;

import java.util.List;

import chess.BitBoard;
import chess.Chessboard;
import chess.Colour;
import chess.Move;
import chess.Square;

/**
 * Stores information about the king in the game.
 * 
 * @author rich
 */
public class King extends BasePiece {

   /**
    * Creates the data structures for the starting position of the king.
    * 
    * @param side
    *           used to determine the starting position for the pieces
    */
   public King(Colour side) {
      super(side, side.toString() + " King");

      pieces = new BitBoard();
      switch (side) {
      case WHITE:
         pieces.setBitsAt(Square.e1);
         break;
      case BLACK:
         pieces.setBitsAt(Square.e8);
         break;
      }
   }

   @Override
   public String getSymbol() {
      return "K";
   }

   @Override
   public List<Move> findMoves(Chessboard chessboard) {
      // TODO Auto-generated method stub
      return null;
   }

}
