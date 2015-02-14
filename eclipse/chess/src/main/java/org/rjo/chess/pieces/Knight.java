package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.List;

import org.rjo.chess.Chessboard;
import org.rjo.chess.Colour;
import org.rjo.chess.Move;
import org.rjo.chess.Square;

/**
 * Stores information about the knights (still) in the game.
 * 
 * @author rich
 */
public class Knight extends Piece {

   /**
    * Constructs the Knight class with the default start squares.
    * 
    * @param colour
    *           indicates the colour of the pieces
    */
   public Knight(Colour colour) {
      this(colour, (Square[]) null);
   }

   /**
    * Constructs the Knight class with the default start squares.
    * 
    * @param colour
    *           indicates the colour of the pieces
    * @param startSquares
    *           the required starting squares of the piece(s). Can be null, in which case the default start squares are
    *           used. (In this case see the alternative constructor {@link #Knight(Colour)}.)
    */
   public Knight(Colour colour, Square... startSquares) {
      super(colour, PieceType.KNIGHT);
      if (startSquares == null) {
         initPosition();
      } else {
         initPosition(startSquares);
      }
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
      return new ArrayList<>();
   }

}
