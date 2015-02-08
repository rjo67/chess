package chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import chess.Chessboard;
import chess.Colour;
import chess.Move;
import chess.Square;

/**
 * Stores information about the king in the game.
 * 
 * @author rich
 * @see http://chessprogramming.wikispaces.com/King+Pattern
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
      // TODO: add support for black moves!

      List<Move> moves = new ArrayList<>();

      /*
       * calculate left and right attack
       * then shift up and down one rank
       */
      BitSet west = BitSetHelper.oneFileWest(pieces.getBitSet());
      BitSet east = BitSetHelper.oneFileEast(pieces.getBitSet());
      BitSet combined = (BitSet) west.clone();
      combined.or(east);
      // save the current state
      BitSet result = (BitSet) combined.clone();
      // now add the king's position again and shift up and down one rank
      combined.or(pieces.getBitSet());
      BitSet north = BitSetHelper.oneRankNorth(combined);
      BitSet south = BitSetHelper.oneRankSouth(combined);
      // add to result
      result.or(north);
      result.or(south);

      // move can't be to a square with the same coloured piece on it
      result.andNot(chessboard.getAllPieces(colour).getBitSet());

      Square kingPosn = Square.fromBitPosn(pieces.getBitSet().nextSetBit(0));
      for (int i = result.nextSetBit(0); i >= 0; i = result.nextSetBit(i + 1)) {
         moves.add(new Move(this, kingPosn, Square.fromBitPosn(i)));
      }

      return moves;
   }

}
