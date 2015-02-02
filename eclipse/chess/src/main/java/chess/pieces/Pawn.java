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
    * 
    * @param side
    *           used to determine the starting position, i.e. white pawns are on the 2nd rank, black pawns on the 7th.
    */
   public Pawn(Colour side) {
      super(side, side.toString() + " Pawn");

      pieces = new BitBoard();
      switch (side) {
      case WHITE:
         pieces.setBitsAt(Square.a2, Square.b2, Square.c2, Square.d2, Square.e2, Square.f2, Square.g2, Square.h2);
         break;
      case BLACK:
         pieces.setBitsAt(Square.a7, Square.b7, Square.c7, Square.d7, Square.e7, Square.f7, Square.g7, Square.h7);
         break;
      }
   }

   @Override
   public String getSymbol() {
      return ""; // no symbol for pawns
   }

   @Override
   public String getFenSymbol() {
      return colour == Colour.WHITE ? "P" : "p";
   }

   @Override
   public List<Move> findMoves(Chessboard chessboard) {
      // TODO Auto-generated method stub
      return null;
   }

}
