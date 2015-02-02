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
    * Creates the data structures for the starting position of the bishops.
    * The bishops will be placed according to the default start position.
    * 
    * @param colour
    *           indicates the colour of the pieces
    */
   public Bishop(Colour colour) {
      this(colour, new Square[0]);
   }

   /**
    * Creates the data structures for the starting position of the bishops.
    * 
    * @param colour
    *           indicates the colour of the pieces
    * @param requiredSquares
    *           required starting position of the pieces (if empty, the standard default positions will be used)
    */
   public Bishop(Colour colour, Square... requiredSquares) {
      super(colour, colour.toString() + " Bishop");
      if (requiredSquares.length == 0) {
         // use default positions
         switch (colour) {
         case White:
            requiredSquares = new Square[] { Square.c1, Square.f1 };
            break;
         case Black:
            requiredSquares = new Square[] { Square.c8, Square.f8 };
            break;
         }
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
