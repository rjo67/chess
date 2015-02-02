package chess.pieces;

import java.util.List;

import chess.BitBoard;
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
    * 
    * @param side
    *           used to determine the starting position for the pieces
    */
   public Bishop(Colour side) {
      super(side, side.toString() + " Bishop");
      pieces = new BitBoard();
      switch (side) {
      case WHITE:
         pieces.setBitsAt(Square.c1, Square.f1);
         break;
      case BLACK:
         pieces.setBitsAt(Square.c8, Square.f8);
         break;
      }
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
