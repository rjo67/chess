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
    * Constructs the Rook class with the default start squares.
    * 
    * @param colour
    *           indicates the colour of the pieces
    */
   public Rook(Colour colour) {
      this(colour, (Square[]) null);
   }

   /**
    * Constructs the Rook class with the default start squares.
    * 
    * @param colour
    *           indicates the colour of the pieces
    * @param startSquares
    *           the required starting squares of the piece(s). Can be null, in which case the default start squares are
    *           used. (In this case see the alternative constructor {@link #Rook(Colour)}.)
    */
   public Rook(Colour colour, Square... startSquares) {
      super(colour, PieceType.ROOK);
      if (startSquares == null) {
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
         move.setCheck(findRankOrFileCheck(game, move, opponentsKing));
      }

      return moves;
   }

   @Override
   public boolean attacksSquare(Chessboard chessboard, Square targetSq) {
      boolean attacksSquare = false;
      int i = pieces.getBitSet().nextSetBit(0);
      while ((!attacksSquare) && (i >= 0)) {
         attacksSquare = attacksSquareRankOrFile(chessboard, Square.fromBitPosn(i), targetSq);
         if (!attacksSquare) {
            i = pieces.getBitSet().nextSetBit(i + 1);
         }
      }
      return attacksSquare;
   }

}
