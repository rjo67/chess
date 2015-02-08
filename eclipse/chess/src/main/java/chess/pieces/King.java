package chess.pieces;

import java.util.List;

import chess.Chessboard;
import chess.Colour;
import chess.Move;
import chess.Square;

/**
 * Stores information about the king in the game.
 * 
 * @author rich
 */
public class King extends Piece {

   /**
    * Constructs the King class.
    * 
    * @param side
    *           used to determine the starting position for the pieces
    */
   public King(Colour colour) {
      super(colour, PieceType.KING);
   }

   @Override
   public void initPosition() {
      Square[] requiredSquares = null;
      switch (colour) {
      case WHITE:
         requiredSquares = new Square[] { Square.e1 };
         break;
      case BLACK:
         requiredSquares = new Square[] { Square.e8 };
         break;
      }
      initPosition(requiredSquares);
   }

   @Override
   public List<Move> findMoves(Chessboard chessboard) {
      // TODO Auto-generated method stub
      return null;
   }

}
