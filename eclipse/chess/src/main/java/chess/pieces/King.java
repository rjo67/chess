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
    * Creates the data structures for the starting position of the King.
    * The King will be placed according to the default start position.
    * 
    * @param side
    *           used to determine the starting position for the pieces
    */
   public King(Colour colour) {
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
   public King(Colour colour, Square... requiredSquares) {
      super(colour, colour.toString() + " King");
      if (requiredSquares.length == 0) {
         // use default positions
         switch (colour) {
         case White:
            requiredSquares = new Square[] { Square.e1 };
            break;
         case Black:
            requiredSquares = new Square[] { Square.e8 };
            break;
         }
      }
      pieces = new BitBoard();
      pieces.setBitsAt(requiredSquares);
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
