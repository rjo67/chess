package org.rjo.chess.pieces;

import java.util.ArrayList;
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
    * Constructs the Bishop class with the default start squares.
    * 
    * @param colour
    *           indicates the colour of the pieces
    */
   public Bishop(Colour colour) {
      this(colour, (Square[]) null);
   }

   /**
    * Constructs the Bishop class with the default start squares.
    * 
    * @param colour
    *           indicates the colour of the pieces
    * @param startSquares
    *           the required starting squares of the piece(s). Can be null, in which case the default start squares are
    *           used. (In this case see the alternative constructor {@link #Bishop(Colour)}.)
    */
   public Bishop(Colour colour, Square... startSquares) {
      super(colour, PieceType.BISHOP);
      if (startSquares == null) {
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
   public List<Move> findMoves(Game game) {
      List<Move> moves = new ArrayList<>(14);

      /*
       * search for moves in directions NW, SW, NE, and SE
       */
      moves.addAll(search(game.getChessboard(), NorthWestMoveHelper.instance()));
      moves.addAll(search(game.getChessboard(), SouthWestMoveHelper.instance()));
      moves.addAll(search(game.getChessboard(), NorthEastMoveHelper.instance()));
      moves.addAll(search(game.getChessboard(), SouthEastMoveHelper.instance()));

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
         attacksSquare = attacksSquareDiagonally(chessboard.getEmptySquares().getBitSet(), Square.fromBitPosn(i),
               targetSq);
         i = pieces.getBitSet().nextSetBit(i + 1);
      }
      return attacksSquare;
   }

}
