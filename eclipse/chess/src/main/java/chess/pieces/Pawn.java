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
public class Pawn extends Piece {

   /**
    * Constructs the Pawn class.
    * 
    * @param colour
    *           indicates the colour of the pieces
    */
   public Pawn(Colour colour) {
      super(colour, PieceType.PAWN);
   }

   @Override
   public void initPosition() {
      Square[] requiredSquares = null;
      switch (colour) {
      case WHITE:
         requiredSquares = new Square[] { Square.a2, Square.b2, Square.c2, Square.d2, Square.e2, Square.f2, Square.g2,
               Square.h2 };
         break;
      case BLACK:
         requiredSquares = new Square[] { Square.a7, Square.b7, Square.c7, Square.d7, Square.e7, Square.f7, Square.g7,
               Square.h7 };
         break;
      }
      initPosition(requiredSquares);
   }

   @Override
   public List<Move> findMoves(Chessboard chessboard) {

      // TODO: add support for black moves!

      List<Move> moves = new ArrayList<>();
      /*
       * 1) one square forward
       * 2) two squares forward
       * 3) capture left
       * 4) capture right
       * 5) enpassant
       * 6) promotion
       */

      // 1) one square forward:
      // shift by 8 and check if empty square
      // 6) promotion:
      // extra check for pawns on the 8th rank
      BitSet oneSquareForward = BitSetHelper.oneRankNorth(pieces.cloneBitSet());
      oneSquareForward.and(chessboard.getEmptySquares().getBitSet()); // move must be to an empty square
      BitSet promotedPawns = (BitSet) oneSquareForward.clone(); // copy this bitset
      promotedPawns.and(BitBoard.ONLY_RANK_EIGHT.getBitSet()); // just the promoted pawns
      oneSquareForward.and(BitBoard.ONLY_RANK_EIGHT.flip()); // remove promoted pawns
      for (int i = oneSquareForward.nextSetBit(0); i >= 0; i = oneSquareForward.nextSetBit(i + 1)) {
         moves.add(new Move(this, Square.fromBitPosn(i - 8), Square.fromBitPosn(i)));
      }
      for (int i = promotedPawns.nextSetBit(0); i >= 0; i = promotedPawns.nextSetBit(i + 1)) {
         for (PieceType type : PieceType.values()) {
            if ((type == PieceType.KING) || (type == PieceType.PAWN)) {
               continue;
            }
            Move move = new Move(this, Square.fromBitPosn(i - 8), Square.fromBitPosn(i));
            move.setPromotionPiece(type);
            moves.add(move);
         }
      }

      // 2) two squares forward:
      // first just take the pawns on the 2nd rank, since only these can still move two squares
      // then shift by 8 and check if empty square
      // shift again by 8 and check if empty square
      BitSet twoSquaresForward = pieces.cloneBitSet();
      twoSquaresForward.and(BitBoard.ONLY_RANK_TWO.getBitSet()); // only the pawns on the 2nd rank
      twoSquaresForward = BitSetHelper.oneRankNorth(twoSquaresForward);
      twoSquaresForward.and(chessboard.getEmptySquares().getBitSet()); // move must be to an empty square
      twoSquaresForward = BitSetHelper.oneRankNorth(twoSquaresForward);
      twoSquaresForward.and(chessboard.getEmptySquares().getBitSet()); // move must be to an empty square

      for (int i = twoSquaresForward.nextSetBit(0); i >= 0; i = twoSquaresForward.nextSetBit(i + 1)) {
         moves.add(new Move(this, Square.fromBitPosn(i - 16), Square.fromBitPosn(i)));
      }

      // 3) capture left
      // first remove the pawns on the first file
      // then shift by 7 and AND with opposition pieces
      BitSet captureLeft = pieces.cloneBitSet();
      captureLeft.and(BitBoard.NOT_FILE_ONE.getBitSet()); // only the pawns on the 2nd to 8th files
      captureLeft = pawnCaptureLeft(captureLeft);
      // move must be a capture, therefore AND with opponent's pieces
      BitSet opponentsPieces = chessboard.getAllPieces(Colour.oppositeColour(this.colour)).cloneBitSet();
      // 5) enpassant: add in enpassant square if available
      addEnpassantSquare(chessboard, opponentsPieces);
      captureLeft.and(opponentsPieces);
      for (int i = captureLeft.nextSetBit(0); i >= 0; i = captureLeft.nextSetBit(i + 1)) {
         moves.add(new Move(this, Square.fromBitPosn(i - 7), Square.fromBitPosn(i), true));
      }

      // 4) capture right
      // first remove the pawns on the eigth file
      // then shift by 9 and AND with opposition pieces
      BitSet captureRight = pieces.cloneBitSet();
      captureRight.and(BitBoard.NOT_FILE_EIGHT.getBitSet()); // only the pawns on the 1st to 7th files
      captureRight = pawnCaptureRight(captureRight);
      // move must be a capture, therefore AND with opponent's pieces
      opponentsPieces = chessboard.getAllPieces(Colour.oppositeColour(this.colour)).cloneBitSet();
      // 5) enpassant: add in enpassant square if available
      addEnpassantSquare(chessboard, opponentsPieces);
      captureRight.and(opponentsPieces);
      for (int i = captureRight.nextSetBit(0); i >= 0; i = captureRight.nextSetBit(i + 1)) {
         moves.add(new Move(this, Square.fromBitPosn(i - 9), Square.fromBitPosn(i), true));
      }

      return moves;
   }

   /**
    * Adds the enpassant square to the list of opponent's pieces.
    * 
    * @param chessboard
    *           state of the board
    * @param opponentsPieces
    *           bit set of opponent's pieces. **May be modified by this method**.
    */
   private void addEnpassantSquare(Chessboard chessboard, BitSet opponentsPieces) {
      Square enpassantSquare = chessboard.getEnpassantSquare();
      if (enpassantSquare != null) {
         opponentsPieces.set(enpassantSquare.bitPosn());
      }
   }

   private BitSet pawnCaptureLeft(BitSet startPosn) {
      if (startPosn.isEmpty()) {
         return startPosn;
      }
      long lo = startPosn.toLongArray()[0];
      BitSet captureLeft = BitSet.valueOf(new long[] { (lo << 7) });
      return captureLeft;
   }

   private BitSet pawnCaptureRight(BitSet startPosn) {
      if (startPosn.isEmpty()) {
         return startPosn;
      }
      long lo = startPosn.toLongArray()[0];
      BitSet captureRight = BitSet.valueOf(new long[] { (lo << 9) });
      return captureRight;
   }
}
