package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.rjo.chess.CastlingRights;
import org.rjo.chess.Chessboard;
import org.rjo.chess.Colour;
import org.rjo.chess.Game;
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
   public List<Move> findMoves(Game game) {
      // TODO: generate the move tables statically

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
      BitSet possibleMoves = (BitSet) combined.clone();
      // now add the king's position again and shift up and down one rank
      combined.or(pieces.getBitSet());
      BitSet north = BitSetHelper.shiftOneNorth(combined);
      BitSet south = BitSetHelper.shiftOneSouth(combined);
      // add to result
      possibleMoves.or(north);
      possibleMoves.or(south);

      // move can't be to a square with a piece of the same colour on it
      possibleMoves.andNot(game.getChessboard().getAllPieces(colour).getBitSet());

      // TODO check for checks here?

      Square opponentsKingSquare = findOpponentsKing(game.getChessboard());
      Square kingPosn = Square.fromBitPosn(pieces.getBitSet().nextSetBit(0));
      for (int i = possibleMoves.nextSetBit(0); i >= 0; i = possibleMoves.nextSetBit(i + 1)) {
         Square targetSquare = Square.fromBitPosn(i);
         // make sure we're not moving king to king
         if (MoveDistance.calculateDistance(targetSquare, opponentsKingSquare) > 1) {
            /*
             * check for captures in 'possibleMoves'.
             * If any found, remove from 'possibleMoves' before next iteration.
             */
            BitSet captures = (BitSet) possibleMoves.clone();
            captures.and(game.getChessboard().getAllPieces(Colour.oppositeColour(getColour())).getBitSet());
            for (int j = captures.nextSetBit(0); j >= 0; j = captures.nextSetBit(j + 1)) {
               moves.add(new Move(this, kingPosn, targetSquare, true));
               // remove capture square
               possibleMoves.clear(j);
            }
            moves.add(new Move(this, kingPosn, targetSquare));
         }
      }

      // castling
      // TODO disallow castling over a square in check
      if (game.canCastle(colour, CastlingRights.KINGS_SIDE)) {
         BitSet bs = game.getChessboard().getEmptySquares().getBitSet();
         if (bs.get(Square.f1.bitPosn()) && bs.get(Square.g1.bitPosn())) {
            moves.add(Move.castleKingsSide(this));
         }
      }
      if (game.canCastle(colour, CastlingRights.QUEENS_SIDE)) {
         BitSet bs = game.getChessboard().getEmptySquares().getBitSet();
         if (bs.get(Square.d1.bitPosn()) && bs.get(Square.c1.bitPosn()) && bs.get(Square.b1.bitPosn())) {
            moves.add(Move.castleQueensSide(this));
         }
      }

      return moves;
   }

   private Square findOpponentsKing(Chessboard chessboard) {
      return chessboard.getPieces(Colour.oppositeColour(colour)).get(PieceType.KING).getLocations()[0];
   }
}
