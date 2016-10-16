package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.rjo.chess.Position;
import org.rjo.chess.Colour;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.Square;
import org.rjo.chess.ray.Ray;
import org.rjo.chess.ray.RayInfo;
import org.rjo.chess.ray.RayUtils;

/**
 * Represents the pieces which can move over a greater distance: rooks, bishops, queens.
 *
 * @author rich
 */
public abstract class SlidingPiece extends Piece {

   final static boolean[][] ON_SAME_DIAGONAL = new boolean[64][64];
   static {
      for (int sq1Index = 0; sq1Index < 64; sq1Index++) {
         Square sq1 = Square.fromBitIndex(sq1Index);
         for (int sq2Index = 0; sq2Index < 64; sq2Index++) {
            Square sq2 = Square.fromBitIndex(sq2Index);
            ON_SAME_DIAGONAL[sq1Index][sq2Index] = onSameDiagonal(sq1, sq2);
         }
      }
   }

   protected SlidingPiece(Colour colour, PieceType type) {
      super(colour, type);
   }

   /**
    * This checks all pieces in the given bitset to see if they can attack the given 'targetSquare' along rank or file,
    * taking into account any intervening pieces.
    *
    * @param pieces
    *           which pieces are available. This should represent the rooks and queens in the game.
    * @param emptySquares
    *           which squares are currently empty.
    * @param targetSquare
    *           which square should be attacked
    * @return true if at least one of the given pieces can attack the target square along a rank or file.
    */
   public static boolean attacksSquareOnRankOrFile(BitSet pieces, BitSet emptySquares, Square targetSquare) {
      // version using canReachTargetSquare is slower than attacksSquareRankOrFile...

      // final BitSet targetSquareBitSet = new BitSet(64);
      // targetSquareBitSet.set(targetSquare.bitIndex());
      // for (MoveHelper helper : new MoveHelper[] { NorthMoveHelper.instance(), EastMoveHelper.instance(),
      // SouthMoveHelper.instance(), WestMoveHelper.instance() }) {
      // if (canReachTargetSquare(pieces, emptySquares, helper, targetSquareBitSet)) {
      // return true;
      // }
      // }
      // return false;
      for (int i = pieces.nextSetBit(0); i >= 0; i = pieces.nextSetBit(i + 1)) {
         if (attacksSquareRankOrFile(emptySquares, Square.fromBitIndex(i), targetSquare)) {
            return true;
         }
      }
      return false;
   }

   /**
    * This checks all pieces in the given bitset to see if they can attack the given 'targetSquare' along a diagonal,
    * taking into account any intervening pieces.
    *
    * @param bishopsAndQueens
    *           which pieces are available. This should represent the bishops and queens in the game.
    * @param emptySquares
    *           which squares are currently empty.
    * @param targetSquare
    *           which square should be attacked
    * @return true if at least one of the given pieces can attack the target square along a diagonal.
    */
   public static boolean attacksSquareOnDiagonal(BitSet bishopsAndQueens, BitSet emptySquares, Square targetSquare) {
      for (int i = bishopsAndQueens.nextSetBit(0); i >= 0; i = bishopsAndQueens.nextSetBit(i + 1)) {
         if (attacksSquareDiagonally(emptySquares, Square.fromBitIndex(i), targetSquare)) {
            return true;
         }
      }
      return false;
   }

   /**
    * Searches for moves in the direction specified by the {@link Ray} implementation.
    * This is for rooks, bishops, and queens.
    *
    * @param chessboard
    *           state of the board
    * @param ray
    *           the ray (direction) in which to search
    * @return the moves found
    */
   protected List<Move> search(Position chessboard, Ray ray) {
      List<Move> moves = new ArrayList<>(30);

      final Colour opponentsColour = Colour.oppositeColour(colour);
      /*
       * for each piece, use the ray to find emptySquares / firstPiece on the ray
       */
      for (int i = pieces.getBitSet().nextSetBit(0); i >= 0; i = pieces.getBitSet().nextSetBit(i + 1)) {
         Square fromSquareIndex = Square.fromBitIndex(i);

         RayInfo info = RayUtils.findFirstPieceOnRay(colour, chessboard.getEmptySquares().getBitSet(), chessboard
               .getAllPieces(colour).getBitSet(), ray, i);
         // add 'emptySquares' from result as normal moves
         for (int emptySquareIndex : info.getEmptySquares()) {
            moves.add(new Move(this.getType(), colour, fromSquareIndex, Square.fromBitIndex(emptySquareIndex)));
         }
         // if an opponent's piece was also found, add this as capture
         if (info.foundPiece() && (info.getColour() == opponentsColour)) {
            Square sqIndex = Square.fromBitIndex(info.getIndexOfPiece());
            moves.add(new Move(this.getType(), colour, fromSquareIndex, sqIndex, chessboard.pieceAt(sqIndex,
                  opponentsColour)));
         }
      }

      return moves;
   }

   /**
    * Checks if the given move would place the opponent's king in check.
    * <p>
    * This is for bishop-type moves.
    *
    * @param game
    *           the game
    * @param move
    *           the move
    * @param opponentsKing
    *           where the opponent's king is
    * @return true if this move is a check
    */
   protected boolean findDiagonalCheck(Game game, Move move, Square opponentsKing) {
      return attacksSquareDiagonally(game.getChessboard().getEmptySquares().getBitSet(), move.to(), opponentsKing);
   }

   /**
    * Checks if a bishop/queen on the given startSquare attacks the given targetSquare, i.e. the target square can be
    * reached (diagonally) from the start square and there are no intervening pieces.
    *
    * @param emptySquares
    *           the empty squares of the board
    * @param startSquare
    *           start square
    * @param targetSquare
    *           target square
    * @return true if the target square is attacked (diagonally) from the start square.
    */
   // public, is required from Pawn
   public static boolean attacksSquareDiagonally(BitSet emptySquares, Square startSquare, Square targetSquare) {
      // give up straight away if start and target are the same
      if (startSquare == targetSquare) {
         return false;
      }
      if (!ON_SAME_DIAGONAL[startSquare.bitIndex()][targetSquare.bitIndex()]) {
         return false;
      }
      int rankOffset = startSquare.rank() > targetSquare.rank() ? -1 : 1;
      int fileOffset = startSquare.file() > targetSquare.file() ? -1 : 1;
      int bitPosn = startSquare.bitIndex();
      boolean reachedTargetSquare = false;
      boolean foundNonEmptySquare = false;
      while (!reachedTargetSquare && !foundNonEmptySquare) {
         bitPosn += (8 * rankOffset) + fileOffset;
         if (bitPosn == targetSquare.bitIndex()) {
            reachedTargetSquare = true;
         } else if (!emptySquares.get(bitPosn)) {
            foundNonEmptySquare = true;
         }
      }
      return reachedTargetSquare;
   }

   /**
    * Checks if the given move would place the opponent's king in check, i.e. the destination square of the move attacks
    * the location of the king along a rank or file.
    * <p>
    * This is for rook-type moves.
    *
    * @param game
    *           the game
    * @param move
    *           the move
    * @param opponentsKing
    *           where the opponent's king is
    * @return true if this move is a check
    */
   protected boolean findRankOrFileCheck(Game game, Move move, Square opponentsKing) {
      return attacksSquareRankOrFile(game.getChessboard().getEmptySquares().getBitSet(), move.to(), opponentsKing);
   }

   /**
    * Checks if a rook/queen on the given startSquare attacks the given targetSquare, i.e. on the same rank or file and
    * no intervening pieces.
    * This is for rook-type moves i.e. straight along files or ranks.
    *
    * @param emptySquares
    *           a bit set representing the empty squares on the board
    * @param startSquare
    *           start square
    * @param targetSquare
    *           target square
    *
    * @return true if the target square is attacked (straight-line) from the start square.
    */
   // public, since King need this too for castling
   public static boolean attacksSquareRankOrFile(BitSet emptySquares, Square startSquare, Square targetSquare) {
      // give up straight away if start and target are the same
      if (startSquare == targetSquare) {
         return false;
      }
      if (startSquare.rank() == targetSquare.rank()) {
         /*
          * Algorithm runs from smallest file to largest.
          * If current square is not empty, then give up. Otherwise keep going until hit target square.
          */
         int[] orderedNumbers = orderNumbers(startSquare.file(), targetSquare.file());
         int bitIndex = Square.fromRankAndFile(startSquare.rank(), orderedNumbers[0]).bitIndex();
         int targetBitIndex = Square.fromRankAndFile(targetSquare.rank(), orderedNumbers[1]).bitIndex();
         for (int i = bitIndex + 1; i < targetBitIndex; i++) {
            if (!emptySquares.get(i)) {
               return false;
            }
         }
         return true;
      } else if (startSquare.file() == targetSquare.file()) {
         /*
          * Algorithm runs from smallest rank to largest.
          * If current square is not empty, then give up. Otherwise keep going until hit target square.
          */
         int[] orderedNumbers = orderNumbers(startSquare.rank(), targetSquare.rank());
         int bitIndex = Square.fromRankAndFile(orderedNumbers[0], startSquare.file()).bitIndex();
         int targetBitIndex = Square.fromRankAndFile(orderedNumbers[1], targetSquare.file()).bitIndex();
         for (int i = bitIndex + 8; i < targetBitIndex; i += 8) {
            if (!emptySquares.get(i)) {
               return false;
            }
         }
         return true;
      }
      return false;
   }

   /**
    * Orders two numbers.
    *
    * @param num1
    *           first number
    * @param num2
    *           second number
    * @return an array with the first element the smaller number, and the 2nd element the larger number
    */
   private static int[] orderNumbers(int num1, int num2) {
      int[] res = new int[2];
      if (num1 < num2) {
         res[0] = num1;
         res[1] = num2;
      } else {
         res[0] = num2;
         res[1] = num1;
      }
      return res;
   }

   private static boolean onSameDiagonal(Square sq1, Square sq2) {
      return Math.abs(sq1.rank() - sq2.rank()) == Math.abs(sq1.file() - sq2.file());
   }

}
