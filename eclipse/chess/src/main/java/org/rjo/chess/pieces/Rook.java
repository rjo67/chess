package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.rjo.chess.Chessboard;
import org.rjo.chess.Colour;
import org.rjo.chess.EastMoveHelper;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.MoveHelper;
import org.rjo.chess.NorthMoveHelper;
import org.rjo.chess.SouthMoveHelper;
import org.rjo.chess.Square;
import org.rjo.chess.WestMoveHelper;
import org.rjo.chess.ray.RayType;

/**
 * Stores information about the rooks (still) in the game.
 *
 * @author rich
 */
public class Rook extends SlidingPiece {

   private static MoveHelper NORTH_MOVE_HELPER = NorthMoveHelper.instance();
   private static MoveHelper SOUTH_MOVE_HELPER = SouthMoveHelper.instance();
   private static MoveHelper WEST_MOVE_HELPER = WestMoveHelper.instance();
   private static MoveHelper EAST_MOVE_HELPER = EastMoveHelper.instance();

   /**
    * Constructs the Rook class -- with no pieces on the board. Delegates to Rook(Colour, boolean) with parameter
    * false.
    *
    * @param colour
    *           indicates the colour of the pieces
    */
   public Rook(Colour colour) {
      this(colour, false);
   }

   /**
    * Constructs the Rook class.
    *
    * @param colour
    *           indicates the colour of the pieces
    * @param startPosition
    *           if true, the default start squares are assigned. If false, no pieces are placed on the board.
    */
   public Rook(Colour colour, boolean startPosition) {
      this(colour, startPosition, (Square[]) null);
   }

   /**
    * Constructs the Rook class, defining the start squares.
    *
    * @param colour
    *           indicates the colour of the pieces
    * @param startSquares
    *           the required starting squares of the piece(s). Can be null, in which case no pieces are placed on the
    *           board.
    */
   public Rook(Colour colour, Square... startSquares) {
      this(colour, false, startSquares);
   }

   /**
    * Constructs the Rook class with the required squares (can be null) or the default start squares.
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
   public Rook(Colour colour, boolean startPosition, Square... startSquares) {
      super(colour, PieceType.ROOK);
      if (startPosition) {
         initPosition();
      } else {
         initPosition(startSquares);
      }
   }

   @Override
   public void initPosition() {
      Square[] requiredSquares = null;
      requiredSquares = colour == Colour.WHITE ? new Square[] { Square.a1, Square.h1 } : new Square[] { Square.a8,
            Square.h8 };
      initPosition(requiredSquares);
   }

   @Override
   public List<Move> findMoves(Game game, boolean kingInCheck) {
      List<Move> moves = new ArrayList<>(30);

      /*
       * search for moves in directions N, S, W, and E
       */
      // moves.addAll(search(game.getChessboard(), NORTH_MOVE_HELPER));
      // moves.addAll(search(game.getChessboard(), SOUTH_MOVE_HELPER));
      // moves.addAll(search(game.getChessboard(), WEST_MOVE_HELPER));
      // moves.addAll(search(game.getChessboard(), EAST_MOVE_HELPER));

      for (RayType rayType : new RayType[] { RayType.NORTH, RayType.EAST, RayType.SOUTH, RayType.WEST }) {
         moves.addAll(search2(game.getChessboard(), rayType.getInstance()));
      }

      // make sure king is not/no longer in check
      Square myKing = King.findKing(colour, game.getChessboard());
      Iterator<Move> iter = moves.listIterator();
      while (iter.hasNext()) {
         Move move = iter.next();
         if (Chessboard.isKingInCheck(game.getChessboard(), move, Colour.oppositeColour(colour), myKing)) {
            iter.remove();
         }
      }

      // checks
      Square opponentsKing = King.findOpponentsKing(colour, game.getChessboard());
      /*
       * most moves have the same starting square. If we've already checked for discovered check for this square,
       * then can use the cached result. (Discovered check only looks along one ray from move.from() to the opponent's
       * king.)
       */
      Map<Square, Boolean> discoveredCheckCache = new HashMap<>(5);
      for (Move move : moves) {
         boolean isCheck = findRankOrFileCheck(game, move, opponentsKing);
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
         if (attacksSquareRankOrFile(chessboard.getEmptySquares().getBitSet(), Square.fromBitIndex(i), targetSq)) {
            return true;
         }
      }
      return false;
   }

}
