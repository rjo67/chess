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
public class Knight extends Piece {

   /**
    * Constructs the Knight class.
    * 
    * @param colour
    *           indicates the colour of the pieces
    */
   public Knight(Colour colour) {
      super(colour, PieceType.KNIGHT);
   }

   @Override
   public void initPosition() {
      Square[] requiredSquares = null;
      switch (colour) {
      case WHITE:
         requiredSquares = new Square[] { Square.b1, Square.g1 };
         break;
      case BLACK:
         requiredSquares = new Square[] { Square.b8, Square.g8 };
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
