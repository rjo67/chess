package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rjo.chess.BitBoard;
import org.rjo.chess.Chessboard;
import org.rjo.chess.Colour;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.Square;

/**
 * Stores information about the knights (still) in the game.
 *
 * @author rich
 */
public class Knight extends Piece {

   /**
    * Stores for each square on the board the possible moves for a knight on that square.
    */
   private static BitSet[] knightMoves = new BitSet[64];

   // set up knight moves look up table
   static {
      for (int i = 0; i < 64; i++) {
         knightMoves[i] = new BitSet(64);
         knightMoves[i].set(i);
         /*
          * LHS: blank first file for -10 and +6
          * - blank first and 2nd file for -17 and +15
          * RHS: blank last file for +10 and -6
          * - blank 7th and 8th file for +17 and -15
          * 
          * Don't need to blank ranks, these just 'drop off' during the bit shift.
          */

         BitSet[] work = new BitSet[8];

         // to avoid wrapping:
         // - work[0,1] == file one blanked
         // - work[2,3] == file two blanked as well
         // - work[4,5] == file 8 blanked
         // - work[6,7] == file 7 blanked as well
         work[0] = (BitSet) knightMoves[i].clone();
         work[0].and(BitBoard.EXCEPT_FILE[0]);
         work[2] = (BitSet) work[0].clone();
         work[2].and(BitBoard.EXCEPT_FILE[1]);

         // store another copy
         work[1] = (BitSet) work[0].clone();
         work[3] = (BitSet) work[2].clone();

         work[0] = BitSetHelper.shift(work[0], 15); // file-1,rank+2
         work[1] = BitSetHelper.shift(work[1], -17);// file-1,rank-2
         work[2] = BitSetHelper.shift(work[2], 6);// file-2,rank+1
         work[3] = BitSetHelper.shift(work[3], -10);// file-2,rank-1

         work[4] = (BitSet) knightMoves[i].clone();
         work[4].and(BitBoard.EXCEPT_FILE[7]);
         work[6] = (BitSet) work[4].clone();
         work[6].and(BitBoard.EXCEPT_FILE[6]);

         // store another copy
         work[5] = (BitSet) work[4].clone();
         work[7] = (BitSet) work[6].clone();

         work[4] = BitSetHelper.shift(work[4], 17); // file+1,rank+2
         work[5] = BitSetHelper.shift(work[5], -15);// file+1,rank-2
         work[6] = BitSetHelper.shift(work[6], 10);// file+2,rank+1
         work[7] = BitSetHelper.shift(work[7], -6);// file+2,rank-1

         // store results
         knightMoves[i].clear(i); // clear the start position
         for (BitSet element : work) {
            knightMoves[i].or(element);
         }
      }
   }

   /**
    * Constructs the Knight class -- with no pieces on the board. Delegates to Knight(Colour, boolean) with parameter
    * false.
    *
    * @param colour
    *           indicates the colour of the pieces
    */
   public Knight(Colour colour) {
      this(colour, false);
   }

   /**
    * Constructs the Knight class.
    *
    * @param colour
    *           indicates the colour of the pieces
    * @param startPosition
    *           if true, the default start squares are assigned. If false, no pieces are placed on the board.
    */
   public Knight(Colour colour, boolean startPosition) {
      this(colour, startPosition, (Square[]) null);
   }

   /**
    * Constructs the Knight class, defining the start squares.
    *
    * @param colour
    *           indicates the colour of the pieces
    * @param startSquares
    *           the required starting squares of the piece(s). Can be null, in which case no pieces are placed on the
    *           board.
    */
   public Knight(Colour colour, Square... startSquares) {
      this(colour, false, startSquares);
   }

   /**
    * Constructs the Knight class with the required squares (can be null) or the default start squares.
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
   public Knight(Colour colour, boolean startPosition, Square... startSquares) {
      super(colour, PieceType.KNIGHT);
      if (startPosition) {
         initPosition();
      } else {
         initPosition(startSquares);
      }
   }

   @Override
   public void initPosition() {
      Square[] requiredSquares = null;
      requiredSquares = colour == Colour.WHITE ? new Square[] { Square.b1, Square.g1 } : new Square[] { Square.b8,
            Square.g8 };
      initPosition(requiredSquares);
   }

   @Override
   public List<Move> findMoves(Game game, boolean kingInCheck) {
      List<Move> moves = new ArrayList<>(20);
      final Square myKing = King.findKing(colour, game.getChessboard());
      /*
       * for each knight on the board, finds its moves using the lookup table
       */
      for (int i = pieces.getBitSet().nextSetBit(0); i >= 0; i = pieces.getBitSet().nextSetBit(i + 1)) {
         BitSet possibleMoves = (BitSet) knightMoves[i].clone();
         // move can't be to a square with a piece of the same colour on it
         possibleMoves.andNot(game.getChessboard().getAllPieces(colour).getBitSet());

         final Square knightStartSquare = Square.fromBitIndex(i);
         Colour oppositeColour = Colour.oppositeColour(colour);

         for (int k = possibleMoves.nextSetBit(0); k >= 0; k = possibleMoves.nextSetBit(k + 1)) {
            Square targetSquare = Square.fromBitIndex(k);
            Move move;
            if (game.getChessboard().getAllPieces(oppositeColour).getBitSet().get(k)) {
               // capture
               move = new Move(PieceType.KNIGHT, colour, knightStartSquare, targetSquare, game.getChessboard().pieceAt(
                     targetSquare, oppositeColour));
            } else {
               move = new Move(PieceType.KNIGHT, colour, knightStartSquare, targetSquare);
            }
            // make sure my king is not/no longer in check
            boolean inCheck = false;
            if (!kingInCheck) {
               // just need to check for a pinned piece, i.e. if my king is in check after the move
               inCheck = Chessboard.checkForPinnedPiece(game.getChessboard(), move, colour, myKing);
            } else {
               inCheck = Chessboard.isKingInCheck(game.getChessboard(), move, oppositeColour, myKing);
            }
            if (!inCheck) {
               moves.add(move);
            }
         }
      }

      // checks
      final Square opponentsKing = King.findOpponentsKing(colour, game.getChessboard());
      final int opponentsKingIndex = opponentsKing.bitIndex();
      /*
       * many moves have the same starting square. If we've already checked for discovered check for this square,
       * then can use the cached result. (Discovered check only looks along one ray from move.from() to the opponent's
       * king.)
       */
      Map<Square, Boolean> discoveredCheckCache = new HashMap<>(5);
      for (Move move : moves) {
         boolean isCheck = checkIfMoveAttacksSquare(move, opponentsKingIndex);
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

   /**
    * Checks whether the given move attacks the given square.
    *
    * @param move
    *           the move
    * @param targetSquareIndex
    *           index of the target square
    * @return true if the given move attacks the given square.
    */
   // also required by Pawn
   public static boolean checkIfMoveAttacksSquare(Move move, int targetSquareIndex) {
      // check if the target square is a knight move away from the destination square of the move
      return knightMoves[move.to().bitIndex()].get(targetSquareIndex);
   }

   @Override
   public boolean attacksSquare(Chessboard notUsed, Square targetSq) {
      BitSet possibleMovesFromTargetSquare = knightMoves[targetSq.bitIndex()];
      return possibleMovesFromTargetSquare.intersects(pieces.getBitSet());
   }

}
