package chess.pieces;

import java.util.List;

import chess.BitBoard;
import chess.Chessboard;
import chess.Colour;
import chess.Move;
import chess.Square;

/**
 * Stores information about the pawns (still) in the game.
 * 
 * @author rich
 */
public class Pawn extends BasePiece {

   /**
    * Creates the data structures for the starting position of the pawns.
    * The pawns will be placed according to the default start position.
    * 
    * @param colour
    *           indicates the colour of the pieces
    */
   public Pawn(Colour colour) {
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
   public Pawn(Colour colour, Square... requiredSquares) {
      super(colour, colour.toString() + " Pawn");
      if (requiredSquares.length == 0) {
         // use default positions
         switch (colour) {
         case White:
            requiredSquares = new Square[] { Square.a2, Square.b2, Square.c2, Square.d2, Square.e2, Square.f2,
                  Square.g2, Square.h2 };
            break;
         case Black:
            requiredSquares = new Square[] { Square.a7, Square.b7, Square.c7, Square.d7, Square.e7, Square.f7,
                  Square.g7, Square.h7 };
            break;
         }
      }
      pieces = new BitBoard();
      pieces.setBitsAt(requiredSquares);
   }

   @Override
   public String getSymbol() {
      return ""; // no symbol for pawns
   }

   @Override
   public String getFenSymbol() {
      return colour == Colour.White ? "P" : "p";
   }

   @Override
   public List<Move> findMoves(Chessboard chessboard) {
      // TODO Auto-generated method stub
      return null;
   }

}
