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
    * The knights will be placed according to the default start position.
    * 
    * @param colour
    *           indicates the colour of the pieces
    */
   public Knight(Colour colour) {
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
   public Knight(Colour colour, Square... requiredSquares) {
      super(colour, colour.toString() + " Knight");
      if (requiredSquares.length == 0) {
         // use default positions
         switch (colour) {
         case White:
            requiredSquares = new Square[] { Square.b1, Square.g1 };
            break;
         case Black:
            requiredSquares = new Square[] { Square.b8, Square.g8 };
            break;
         }
      }
      pieces = new BitBoard();
      pieces.setBitsAt(requiredSquares);
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
