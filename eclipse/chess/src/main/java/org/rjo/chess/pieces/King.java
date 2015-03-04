package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    * Which squares cannot be attacked when castling.
    */
   private static final Map<Colour, Map<CastlingRights, Square[]>> CASTLING_SQUARES_NOT_IN_CHECK;
   static {
      CASTLING_SQUARES_NOT_IN_CHECK = new HashMap<>();
      Map<CastlingRights, Square[]> tmp = new HashMap<>();
      tmp.put(CastlingRights.KINGS_SIDE, new Square[] { Square.f1, Square.g1 });
      tmp.put(CastlingRights.QUEENS_SIDE, new Square[] { Square.c1, Square.d1 });
      CASTLING_SQUARES_NOT_IN_CHECK.put(Colour.WHITE, tmp);
      tmp = new HashMap<>();
      tmp.put(CastlingRights.KINGS_SIDE, new Square[] { Square.f8, Square.g8 });
      tmp.put(CastlingRights.QUEENS_SIDE, new Square[] { Square.c8, Square.d8 });
      CASTLING_SQUARES_NOT_IN_CHECK.put(Colour.BLACK, tmp);
   }
   /**
    * Which squares need to be empty when castling.
    */
   private static final Map<Colour, Map<CastlingRights, Square[]>> CASTLING_SQUARES_WHICH_MUST_BE_EMPTY;
   static {
      CASTLING_SQUARES_WHICH_MUST_BE_EMPTY = new HashMap<>();
      Map<CastlingRights, Square[]> tmp = new HashMap<>();
      tmp.put(CastlingRights.KINGS_SIDE, new Square[] { Square.f1, Square.g1 });
      tmp.put(CastlingRights.QUEENS_SIDE, new Square[] { Square.b1, Square.c1, Square.d1 });
      CASTLING_SQUARES_WHICH_MUST_BE_EMPTY.put(Colour.WHITE, tmp);
      tmp = new HashMap<>();
      tmp.put(CastlingRights.KINGS_SIDE, new Square[] { Square.f8, Square.g8 });
      tmp.put(CastlingRights.QUEENS_SIDE, new Square[] { Square.b8, Square.c8, Square.d8 });
      CASTLING_SQUARES_WHICH_MUST_BE_EMPTY.put(Colour.BLACK, tmp);
   }

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
      requiredSquares = colour == Colour.WHITE ? new Square[] { Square.e1 } : new Square[] { Square.e8 };
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

      Square opponentsKingSquare = findOpponentsKing(game.getChessboard());
      Square kingPosn = Square.fromBitPosn(pieces.getBitSet().nextSetBit(0));
      for (int i = possibleMoves.nextSetBit(0); i >= 0; i = possibleMoves.nextSetBit(i + 1)) {
         Square targetSquare = Square.fromBitPosn(i);
         // make sure we're not moving king to king
         // and cannot move to a square that is being attacked
         if ((MoveDistance.calculateDistance(targetSquare, opponentsKingSquare) > 1)
               && !game.getChessboard().squareIsAttacked(game, targetSquare, Colour.oppositeColour(colour))) {
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
      if (game.canCastle(colour, CastlingRights.KINGS_SIDE)) {
         BitSet emptySquaresBitset = game.getChessboard().getEmptySquares().getBitSet();
         boolean canCastle = true;
         // check squares are empty
         for (Square sq : CASTLING_SQUARES_WHICH_MUST_BE_EMPTY.get(colour).get(CastlingRights.KINGS_SIDE)) {
            canCastle = canCastle && emptySquaresBitset.get(sq.bitPosn());
         }
         // check squares are not attacked by an enemy piece
         for (Square sq : CASTLING_SQUARES_NOT_IN_CHECK.get(colour).get(CastlingRights.KINGS_SIDE)) {
            if (canCastle) {
               canCastle = canCastle && !game.getChessboard().squareIsAttacked(game, sq, Colour.oppositeColour(colour));
            }
         }
         if (canCastle) {
            moves.add(Move.castleKingsSide(this));
         }
      }
      if (game.canCastle(colour, CastlingRights.QUEENS_SIDE)) {
         BitSet emptySquaresBitset = game.getChessboard().getEmptySquares().getBitSet();
         boolean canCastle = true;
         // check squares are empty
         for (Square sq : CASTLING_SQUARES_WHICH_MUST_BE_EMPTY.get(colour).get(CastlingRights.QUEENS_SIDE)) {
            canCastle = canCastle && emptySquaresBitset.get(sq.bitPosn());
         }
         // check squares are not attacked by an enemy piece
         for (Square sq : CASTLING_SQUARES_NOT_IN_CHECK.get(colour).get(CastlingRights.QUEENS_SIDE)) {
            if (canCastle) {
               canCastle = canCastle && !game.getChessboard().squareIsAttacked(game, sq, Colour.oppositeColour(colour));
            }
         }
         if (canCastle) {
            moves.add(Move.castleQueensSide(this));
         }
      }

      // discovered checks
      for (Move move : moves) {
         move.setCheck(Chessboard.checkForDiscoveredCheck(game.getChessboard(), move, colour, opponentsKingSquare));
      }

      return moves;
   }

   private Square findOpponentsKing(Chessboard chessboard) {
      return findOpponentsKing(colour, chessboard);
   }

   /**
    * Locates the enemy's king.
    * 
    * TODO store this value in 'Game' after each move, to make this lookup quicker.
    * 
    * @param myColour
    *           my colour
    * @param chessboard
    *           the board
    * @return location of the other colour's king.
    */
   public static Square findOpponentsKing(Colour myColour, Chessboard chessboard) {
      return chessboard.getPieces(Colour.oppositeColour(myColour)).get(PieceType.KING).getLocations()[0];
   }

   @Override
   public boolean attacksSquare(Chessboard chessboard, Square sq) {
      return MoveDistance.calculateDistance(getLocations()[0], sq) == 1;
   }
}
