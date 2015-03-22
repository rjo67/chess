package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.rjo.chess.Chessboard;
import org.rjo.chess.Colour;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.NorthEastMoveHelper;
import org.rjo.chess.NorthWestMoveHelper;
import org.rjo.chess.SouthEastMoveHelper;
import org.rjo.chess.SouthWestMoveHelper;
import org.rjo.chess.Square;

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
      List<Move> moves = new ArrayList<>(14);

      /*
       * search for moves in directions NW, SW, NE, and SE
       */
      moves.addAll(search(game.getChessboard(), NorthWestMoveHelper.instance()));
      moves.addAll(search(game.getChessboard(), SouthWestMoveHelper.instance()));
      moves.addAll(search(game.getChessboard(), NorthEastMoveHelper.instance()));
      moves.addAll(search(game.getChessboard(), SouthEastMoveHelper.instance()));

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
      for (Move move : moves) {
         boolean isCheck = findDiagonalCheck(game, move, opponentsKing);
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
         attacksSquare = attacksSquareDiagonally(chessboard.getEmptySquares().getBitSet(), Square.fromBitIndex(i),
               targetSq);
         i = pieces.getBitSet().nextSetBit(i + 1);
      }
      return attacksSquare;
   }

}
