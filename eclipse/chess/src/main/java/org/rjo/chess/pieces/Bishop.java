package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.rjo.chess.Chessboard;
import org.rjo.chess.Colour;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.Square;
import org.rjo.chess.ray.RayType;

/**
 * Stores information about the bishops (still) in the game.
 *
 * @author rich
 */
public class Bishop extends SlidingPiece {

   /**
    * Constructs the Bishop class -- with no pieces on the board. Delegates to Bishop(Colour, boolean) with parameter
    * false.
    *
    * @param colour
    *           indicates the colour of the pieces
    */
   public Bishop(Colour colour) {
      this(colour, false);
   }

   /**
    * Constructs the Bishop class.
    *
    * @param colour
    *           indicates the colour of the pieces
    * @param startPosition
    *           if true, the default start squares are assigned. If false, no pieces are placed on the board.
    */
   public Bishop(Colour colour, boolean startPosition) {
      this(colour, startPosition, (Square[]) null);
   }

   /**
    * Constructs the Bishop class, defining the start squares.
    *
    * @param colour
    *           indicates the colour of the pieces
    * @param startSquares
    *           the required starting squares of the piece(s). Can be null, in which case no pieces are placed on the
    *           board.
    */
   public Bishop(Colour colour, Square... startSquares) {
      this(colour, false, startSquares);
   }

   /**
    * Constructs the Bishop class with the required squares (can be null) or the default start squares.
    * Setting 'startPosition' true has precedence over 'startSquares'.
    *
    * @param colour
    *           indicates the colour of the pieces
    * @param startPosition
    *           if true, the default start squares are assigned. Value of 'startSquares' will be ignored.
    * @param startSquares
    *           the required starting squares of the piece(s). Can be null, in which case no pieces are placed on the
    *           board.
    */
   public Bishop(Colour colour, boolean startPosition, Square... startSquares) {
      super(colour, PieceType.BISHOP);
      if (startPosition) {
         initPosition();
      } else {
         initPosition(startSquares);
      }
   }

   @Override
   public void initPosition() {
      Square[] requiredSquares = null;
      requiredSquares = colour == Colour.WHITE ? new Square[] { Square.c1, Square.f1 } : new Square[] { Square.c8,
            Square.f8 };
      initPosition(requiredSquares);
   }

   @Override
   public List<Move> findMoves(Game game, boolean kingInCheck) {
      List<Move> moves = new ArrayList<>(30);

      // search for moves
      for (RayType rayType : new RayType[] { RayType.NORTHWEST, RayType.NORTHEAST, RayType.SOUTHWEST, RayType.SOUTHEAST }) {
         moves.addAll(search(game.getChessboard(), rayType.getInstance()));
      }

      // make sure king is not/no longer in check
      Square myKing = King.findKing(colour, game.getChessboard());
      Iterator<Move> iter = moves.listIterator();
      while (iter.hasNext()) {
         Move move = iter.next();
         boolean inCheck = false;
         if (!kingInCheck) {
            // just need to check for a pinned piece, i.e. if my king is in check after the move
            inCheck = Chessboard.checkForPinnedPiece(game.getChessboard(), move, colour, myKing);
         } else {
            inCheck = Chessboard.isKingInCheck(game.getChessboard(), move, Colour.oppositeColour(colour), myKing);
         }
         if (inCheck) {
            iter.remove();
         }
      }
      // checks
      Square opponentsKing = King.findOpponentsKing(colour, game.getChessboard());
      /*
       * many moves have the same starting square. If we've already checked for discovered check for this square,
       * then can use the cached result. (Discovered check only looks along one ray from move.from() to the opponent's
       * king.)
       */
      Map<Square, Boolean> discoveredCheckCache = new HashMap<>(5);
      for (Move move : moves) {
         boolean isCheck = findDiagonalCheck(game, move, opponentsKing);
         // if it's already check, don't need to calculate discovered check
         if (!isCheck) {
            if (discoveredCheckCache.containsKey(move.from())) {
               isCheck = discoveredCheckCache.get(move.from());
            } else {
               isCheck = Chessboard.checkForDiscoveredCheck(game.getChessboard(), move, colour, opponentsKing);
               discoveredCheckCache.put(move.from(), isCheck);
            }
         }
         move.setCheck(isCheck);
      }
      return moves;
   }

   @Override
   public boolean attacksSquare(Chessboard chessboard, Square targetSq) {
      for (int i = pieces.getBitSet().nextSetBit(0); i >= 0; i = pieces.getBitSet().nextSetBit(i + 1)) {
         if (attacksSquareDiagonally(chessboard.getEmptySquares().getBitSet(), Square.fromBitIndex(i), targetSq)) {
            return true;
         }
      }
      return false;
   }

}
