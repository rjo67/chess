package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.List;

import org.rjo.chess.Colour;
import org.rjo.chess.EastMoveHelper;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.MoveHelper;
import org.rjo.chess.NorthEastMoveHelper;
import org.rjo.chess.NorthMoveHelper;
import org.rjo.chess.NorthWestMoveHelper;
import org.rjo.chess.SouthEastMoveHelper;
import org.rjo.chess.SouthMoveHelper;
import org.rjo.chess.SouthWestMoveHelper;
import org.rjo.chess.Square;
import org.rjo.chess.WestMoveHelper;

/**
 * Stores information about the queens (still) in the game.
 * 
 * @author rich
 */
public class Queen extends Piece {
   private static MoveHelper NORTHWEST_MOVE_HELPER = NorthWestMoveHelper.instance();
   private static MoveHelper SOUTHWEST_MOVE_HELPER = SouthWestMoveHelper.instance();
   private static MoveHelper NORTHEAST_MOVE_HELPER = NorthEastMoveHelper.instance();
   private static MoveHelper SOUTHEAST_MOVE_HELPER = SouthEastMoveHelper.instance();
   private static MoveHelper NORTH_MOVE_HELPER = NorthMoveHelper.instance();
   private static MoveHelper SOUTH_MOVE_HELPER = SouthMoveHelper.instance();
   private static MoveHelper WEST_MOVE_HELPER = WestMoveHelper.instance();
   private static MoveHelper EAST_MOVE_HELPER = EastMoveHelper.instance();

   /**
    * Constructs the Queen class with the default start squares.
    * 
    * @param colour
    *           indicates the colour of the pieces
    */
   public Queen(Colour colour) {
      this(colour, (Square[]) null);
   }

   /**
    * Constructs the Queen class with the default start squares.
    * 
    * @param colour
    *           indicates the colour of the pieces
    * @param startSquares
    *           the required starting squares of the piece(s). Can be null, in which case the default start squares are
    *           used. (In this case see the alternative constructor {@link #Queen(Colour)}.)
    */
   public Queen(Colour colour, Square... startSquares) {
      super(colour, PieceType.QUEEN);
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
         requiredSquares = new Square[] { Square.d1 };
         break;
      case BLACK:
         requiredSquares = new Square[] { Square.d8 };
         break;
      }
      initPosition(requiredSquares);
   }

   @Override
   public List<Move> findMoves(Game game) {

      List<Move> moves = new ArrayList<>(30);

      /*
       * search for moves in all compaass directions.
       */
      moves.addAll(search(game.getChessboard(), NORTH_MOVE_HELPER));
      moves.addAll(search(game.getChessboard(), NORTHEAST_MOVE_HELPER));
      moves.addAll(search(game.getChessboard(), EAST_MOVE_HELPER));
      moves.addAll(search(game.getChessboard(), SOUTHEAST_MOVE_HELPER));
      moves.addAll(search(game.getChessboard(), SOUTH_MOVE_HELPER));
      moves.addAll(search(game.getChessboard(), SOUTHWEST_MOVE_HELPER));
      moves.addAll(search(game.getChessboard(), WEST_MOVE_HELPER));
      moves.addAll(search(game.getChessboard(), NORTHWEST_MOVE_HELPER));

      return moves;
   }

}
