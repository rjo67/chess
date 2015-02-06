package chess.pieces;

import java.util.List;

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
    * Constructs the Knight class.
    * 
    * @param colour
    *           indicates the colour of the pieces
    */
   public Knight(Colour colour) {
      super(colour, colour.toString() + " Knight");
   }

   @Override
   public void initPosition() {
      Square[] requiredSquares = null;
      switch (colour) {
      case White:
         requiredSquares = new Square[] { Square.b1, Square.g1 };
         break;
      case Black:
         requiredSquares = new Square[] { Square.b8, Square.g8 };
         break;
      }
      initPosition(requiredSquares);
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
