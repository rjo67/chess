package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.List;

import org.rjo.chess.Colour;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.MoveHelper;
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
public class Bishop extends Piece {
   private static MoveHelper NORTHWEST_MOVE_HELPER = NorthWestMoveHelper.instance();
   private static MoveHelper SOUTHWEST_MOVE_HELPER = SouthWestMoveHelper.instance();
   private static MoveHelper NORTHEAST_MOVE_HELPER = NorthEastMoveHelper.instance();
   private static MoveHelper SOUTHEAST_MOVE_HELPER = SouthEastMoveHelper.instance();

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
      moves.addAll(search(game.getChessboard(), NORTHWEST_MOVE_HELPER));
      moves.addAll(search(game.getChessboard(), SOUTHWEST_MOVE_HELPER));
      moves.addAll(search(game.getChessboard(), NORTHEAST_MOVE_HELPER));
      moves.addAll(search(game.getChessboard(), SOUTHEAST_MOVE_HELPER));
      return moves;
   }

}
