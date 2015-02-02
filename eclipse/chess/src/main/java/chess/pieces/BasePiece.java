package chess.pieces;

import java.util.HashSet;
import java.util.Set;

import chess.BitBoard;
import chess.Colour;
import chess.Square;

public abstract class BasePiece implements Piece {

   // id of this piece (name and colour e.g. white bishop)
   private String name;

   // stores position of the piece(s) of a particular kind (queen, pawns, ...)
   protected BitBoard pieces;

   // Stores for each square a bitboard containing all possible moves.
   // first dimension is the rank, 2nd the file
   protected BitBoard[][] moveBitBoards;

   // stores the colour of the piece
   protected Colour colour;

   protected BasePiece(Colour colour, String name) {
      this.colour = colour;
      this.name = name;
   }

   @Override
   public Colour getColour() {
      return colour;
   }

   @Override
   public BitBoard getBitBoard() {
      return pieces;
   }

   @Override
   public void initPosition(Square... requiredSquares) {
      pieces = new BitBoard();
      pieces.setBitsAt(requiredSquares);
   }

   @Override
   public Set<Square> getLocations() {
      Set<Square> set = new HashSet<>();
      for (int i = pieces.getBitSet().nextSetBit(0); i >= 0; i = pieces.getBitSet().nextSetBit(i + 1)) {
         set.add(Square.fromBitPosn(i));
      }
      return set;
   }

   @Override
   public String getFenSymbol() {
      return colour == Colour.White ? getSymbol() : getSymbol().toLowerCase();
   }

   // for test
   BitBoard[][] getMoveBitBoards() {
      return moveBitBoards;
   }

   @Override
   public String toString() {
      return name;
   }

}
