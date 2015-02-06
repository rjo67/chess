package chess.pieces;

import java.util.List;

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
    * Initialises the Queen class.
    * 
    * @param colour
    *           indicates the colour of the pieces
    */
   public Queen(Colour colour) {
      super(colour, colour.toString() + " Queen");
   }

   @Override
   public void initPosition() {
      Square[] requiredSquares = null;
      switch (colour) {
      case White:
         requiredSquares = new Square[] { Square.d1 };
         break;
      case Black:
         requiredSquares = new Square[] { Square.d8 };
         break;
      }
      initPosition(requiredSquares);
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
