package chess.pieces;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import chess.BitBoard;
import chess.Chessboard;
import chess.Colour;
import chess.Move;
import chess.Square;

public abstract class Piece {

   // type of this piece
   private PieceType type;

   // stores position of the piece(s) of a particular kind (queen, pawns, ...)
   protected BitBoard pieces;

   // Stores for each square a bitboard containing all possible moves.
   // first dimension is the rank, 2nd the file
   protected BitBoard[][] moveBitBoards;

   // stores the colour of the piece
   protected Colour colour;

   /**
    * @return the symbol for this piece.
    */
   public String getSymbol() {
      return type.getSymbol();
   }

   abstract public void initPosition();

   abstract public List<Move> findMoves(Chessboard chessboard);

   protected Piece(Colour colour, PieceType type) {
      this.colour = colour;
      this.type = type;
   }

   public Colour getColour() {
      return colour;
   }

   public BitBoard getBitBoard() {
      return pieces;
   }

   public void initPosition(Square... requiredSquares) {
      pieces = new BitBoard();
      pieces.setBitsAt(requiredSquares);
   }

   public Set<Square> getLocations() {
      Set<Square> set = new HashSet<>();
      for (int i = pieces.getBitSet().nextSetBit(0); i >= 0; i = pieces.getBitSet().nextSetBit(i + 1)) {
         set.add(Square.fromBitPosn(i));
      }
      return set;
   }

   /**
    * Returns the FEN symbol for this piece. Delegates to {@link PieceType#getFenSymbol(Colour)}.
    * 
    * @return the FEN symbol for this piece.
    */
   public String getFenSymbol() {
      return type.getFenSymbol(colour);
   }

   // for test
   BitBoard[][] getMoveBitBoards() {
      return moveBitBoards;
   }

   @Override
   public String toString() {
      return colour.toString() + " " + type.toString();
   }

   public PieceType getType() {
      return type;
   }

}
