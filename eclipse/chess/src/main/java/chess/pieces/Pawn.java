package chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import chess.BitBoard;
import chess.Chessboard;
import chess.Colour;
import chess.Move;
import chess.Square;

/**
 * Stores information about the pawns (still) in the game.
 * 
 * @author rich
 */
public class Pawn extends BasePiece {

   /**
    * Constructs the Pawn class.
    * 
    * @param colour
    *           indicates the colour of the pieces
    */
   public Pawn(Colour colour) {
      super(colour, colour.toString() + " Pawn");
   }

   @Override
   public void initPosition() {
      Square[] requiredSquares = null;
      switch (colour) {
      case White:
         requiredSquares = new Square[] { Square.a2, Square.b2, Square.c2, Square.d2, Square.e2, Square.f2, Square.g2,
               Square.h2 };
         break;
      case Black:
         requiredSquares = new Square[] { Square.a7, Square.b7, Square.c7, Square.d7, Square.e7, Square.f7, Square.g7,
               Square.h7 };
         break;
      }
      initPosition(requiredSquares);
   }

   @Override
   public String getSymbol() {
      return ""; // no symbol for pawns
   }

   @Override
   public String getFenSymbol() {
      return colour == Colour.White ? "P" : "p";
   }

   @Override
   public List<Move> findMoves(Chessboard chessboard) {
      List<Move> moves = new ArrayList<>();
      /*
       * 1) one square forward
       * 2) two squares forward
       * 3) capture left
       * 4) capture right
       * TODO:
       * 5) enpassant
       * 6) promotion
       */

      // 1) one square forward:
      // shift by 8 and check if empty square
      BitSet oneSquareForward = moveOneRank(pieces.cloneBitSet());
      oneSquareForward.and(chessboard.getEmptySquares().getBitSet()); // move must be to an empty square

      for (int i = oneSquareForward.nextSetBit(0); i >= 0; i = oneSquareForward.nextSetBit(i + 1)) {
         moves.add(new Move(this, Square.fromBitPosn(i - 8), Square.fromBitPosn(i)));
      }

      // 2) two squares forward:
      // first just take the pawns on the 2nd rank, since only these can still move two squares
      // then shift by 8 and check if empty square
      // shift again by 8 and check if empty square
      BitSet twoSquaresForward = pieces.cloneBitSet();
      twoSquaresForward.and(BitBoard.RANK_TWO.getBitSet()); // only the pawns on the 2nd rank
      twoSquaresForward = moveOneRank(twoSquaresForward);
      twoSquaresForward.and(chessboard.getEmptySquares().getBitSet()); // move must be to an empty square
      twoSquaresForward = moveOneRank(twoSquaresForward);
      twoSquaresForward.and(chessboard.getEmptySquares().getBitSet()); // move must be to an empty square

      for (int i = twoSquaresForward.nextSetBit(0); i >= 0; i = twoSquaresForward.nextSetBit(i + 1)) {
         moves.add(new Move(this, Square.fromBitPosn(i - 16), Square.fromBitPosn(i)));
      }

      // 3) capture left
      // first remove the pawns on the first file
      // then shift by 7 and AND with opposition pieces
      BitSet captureLeft = pieces.cloneBitSet();
      captureLeft.and(BitBoard.FILE_ONE.getBitSet()); // only the pawns on the 2nd to 8th files
      captureLeft = captureLeft(captureLeft);
      captureLeft.and(chessboard.getAllPieces(Colour.oppositeColour(this.colour)).getBitSet()); // move must be a
                                                                                                // capture
      for (int i = captureLeft.nextSetBit(0); i >= 0; i = captureLeft.nextSetBit(i + 1)) {
         moves.add(new Move(this, Square.fromBitPosn(i - 7), Square.fromBitPosn(i), true));
      }

      return moves;
   }

   private BitSet moveOneRank(BitSet startPosn) {
      if (startPosn.isEmpty()) {
         return startPosn;
      }
      long lo = startPosn.toLongArray()[0];
      BitSet oneSquareForward = BitSet.valueOf(new long[] { (lo << 8) });
      return oneSquareForward;
   }

   private BitSet captureLeft(BitSet startPosn) {
      if (startPosn.isEmpty()) {
         return startPosn;
      }
      long lo = startPosn.toLongArray()[0];
      BitSet captureLeft = BitSet.valueOf(new long[] { (lo << 7) });
      return captureLeft;
   }
}
