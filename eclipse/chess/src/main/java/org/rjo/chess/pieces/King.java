package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.rjo.chess.Chessboard;
import org.rjo.chess.Colour;
import org.rjo.chess.Move;
import org.rjo.chess.MoveDistance;
import org.rjo.chess.Square;

/**
 * Stores information about the king in the game.
 * 
 * @author rich
 * @see http://chessprogramming.wikispaces.com/King+Pattern
 */
public class King extends Piece {

   /**
    * Constructs the King class with the default start squares.
    * 
    * @param colour
    *           indicates the colour of the pieces
    */
   public King(Colour colour) {
      this(colour, (Square[]) null);
   }

   /**
    * Constructs the King class with the default start squares.
    * 
    * @param colour
    *           indicates the colour of the pieces
    * @param startSquares
    *           the required starting squares of the piece(s). Can be null, in which case the default start squares are
    *           used. (In this case see the alternative constructor {@link #King(Colour)}.)
    */
   public King(Colour colour, Square... startSquares) {
      super(colour, PieceType.KING);
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
      // TODO: generate the move tables statically
      // TODO: castling

      List<Move> moves = new ArrayList<>();

      /*
       * calculate left and right attack
       * then shift up and down one rank
       */
      BitSet west = BitSetHelper.shiftOneWest(pieces.getBitSet());
      BitSet east = BitSetHelper.shiftOneEast(pieces.getBitSet());
      BitSet combined = (BitSet) west.clone();
      combined.or(east);
      // save the current state
      BitSet result = (BitSet) combined.clone();
      // now add the king's position again and shift up and down one rank
      combined.or(pieces.getBitSet());
      BitSet north = BitSetHelper.shiftOneNorth(combined);
      BitSet south = BitSetHelper.shiftOneSouth(combined);
      // add to result
      result.or(north);
      result.or(south);

      // move can't be to a square with a piece of the same colour on it
      result.andNot(chessboard.getAllPieces(colour).getBitSet());

      // TODO check for checks here?

      Square opponentsKingSquare = findOpponentsKing(chessboard);
      Square kingPosn = Square.fromBitPosn(pieces.getBitSet().nextSetBit(0));
      for (int i = result.nextSetBit(0); i >= 0; i = result.nextSetBit(i + 1)) {
         Square targetSquare = Square.fromBitPosn(i);
         if (MoveDistance.calculateDistance(targetSquare, opponentsKingSquare) > 1) {
            // TODO capture?
            moves.add(new Move(this, kingPosn, targetSquare));
         }
      }

      return moves;
   }

   private Square findOpponentsKing(Chessboard chessboard) {
      return chessboard.getPieces(Colour.oppositeColour(colour)).get(PieceType.KING).getLocations()[0];
   }
}
