package chess.pieces;

import java.util.List;

import chess.BitBoard;
import chess.Chessboard;
import chess.Colour;
import chess.Move;
import chess.Square;

/**
 * Stores information about the knights (still) in the game.
 * 
 * @author rich
 */
public class Knight extends BasePiece {

   /**
    * Creates the data structures for the starting position of the knights.
    * 
    * @param side
    *           used to determine the starting position for the pieces
    */
   public Knight(Colour side) {
      super(side, side.toString() + " Knight");

      pieces = new BitBoard();
      switch (side) {
      case WHITE:
         pieces.setBitsAt(Square.b1, Square.g1);
         break;
      case BLACK:
         pieces.setBitsAt(Square.b8, Square.g8);
         break;
      }
   }

   @Override
   public String getSymbol() {
      return "N";
   }

   @Override
   public List<Move> findMoves(Chessboard chessboard) {
      // TODO Auto-generated method stub
      return null;
   }

}
