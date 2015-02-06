package chess.pieces;

import java.util.List;

import chess.Chessboard;
import chess.Colour;
import chess.Move;
import chess.Square;

/**
 * Stores information about the bishops (still) in the game.
 * 
 * @author rich
 */
public class Bishop extends BasePiece {

   /**
    * Constructs the Bishop class.
    * 
    * @param colour
    *           indicates the colour of the pieces
    */
   public Bishop(Colour colour) {
      super(colour, colour.toString() + " Bishop");
   }

   @Override
   public void initPosition() {
      Square[] requiredSquares = null;
      switch (colour) {
      case White:
         requiredSquares = new Square[] { Square.c1, Square.f1 };
         break;
      case Black:
         requiredSquares = new Square[] { Square.c8, Square.f8 };
         break;
      }
      initPosition(requiredSquares);
   }

   @Override
   public String getSymbol() {
      return "B";
   }

   @Override
   public List<Move> findMoves(Chessboard chessboard) {
      // TODO Auto-generated method stub
      return null;
   }
}
