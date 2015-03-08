package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.List;

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
   public List<Move> findMoves(Game game) {
      List<Move> moves = new ArrayList<>(14);

      /*
       * search for moves in directions N, S, W, and E
       */
      moves.addAll(search(game.getChessboard(), NORTH_MOVE_HELPER));
      moves.addAll(search(game.getChessboard(), SOUTH_MOVE_HELPER));
      moves.addAll(search(game.getChessboard(), WEST_MOVE_HELPER));
      moves.addAll(search(game.getChessboard(), EAST_MOVE_HELPER));

      // checks
      Square opponentsKing = King.findOpponentsKing(colour, game.getChessboard());
      for (Move move : moves) {
         boolean isCheck = findRankOrFileCheck(game, move, opponentsKing);
         // if it's already check, don't need to calculate discovered check
         if (!isCheck) {
            isCheck = Chessboard.checkForDiscoveredCheck(game.getChessboard(), move, colour, opponentsKing);
         }
         move.setCheck(isCheck);
      }

      return moves;
   }

   @Override
   public boolean attacksSquare(Chessboard chessboard, Square targetSq) {
      boolean attacksSquare = false;
      int i = pieces.getBitSet().nextSetBit(0);
      while ((!attacksSquare) && (i >= 0)) {
         attacksSquare = attacksSquareRankOrFile(chessboard.getEmptySquares().getBitSet(), Square.fromBitIndex(i),
               targetSq);
         if (!attacksSquare) {
            i = pieces.getBitSet().nextSetBit(i + 1);
         }
      }
      return attacksSquare;
   }

}