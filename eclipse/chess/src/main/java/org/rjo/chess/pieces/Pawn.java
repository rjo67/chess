package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.rjo.chess.BitBoard;
import org.rjo.chess.Chessboard;
import org.rjo.chess.Colour;
import org.rjo.chess.Move;
import org.rjo.chess.Square;

/**
 * Stores information about the pawns (still) in the game.
 * 
 * @author rich
 */
public class Pawn extends Piece {

   private MoveHelper helper;

   /**
    * Constructs the Pawn class with the default start squares.
    * 
    * @param colour
    *           indicates the colour of the pieces
    */
   public Pawn(Colour colour) {
      this(colour, (Square[]) null);
   }

   /**
    * Constructs the Pawn class with the default start squares.
    * 
    * @param colour
    *           indicates the colour of the pieces
    * @param startSquares
    *           the required starting squares of the piece(s). Can be null, in which case the default start squares are
    *           used. (In this case see the alternative constructor {@link #Pawn(Colour)}.)
    */
   public Pawn(Colour colour, Square... startSquares) {
      super(colour, PieceType.PAWN);
      switch (colour) {
      case WHITE:
         helper = new WhiteMoveHelper();
         break;
      case BLACK:
         helper = new BlackSideHelper();
         break;
      }
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

      /*
       * The pawn move is complicated by the different directions for white and black pawns.
       * This is the only piece to have this complication.
       */

      List<Move> moves = new ArrayList<>();
      /*
       * 1) one square forward
       * 2) two squares forward
       * 3) capture left
       * 4) capture right
       * 5) enpassant
       * 6) promotion
       */
      moves.addAll(moveOneSquareForward(chessboard, helper));
      moves.addAll(moveTwoSquaresForward(chessboard, helper));
      moves.addAll(captureLeft(chessboard, helper));
      moves.addAll(captureRight(chessboard, helper));

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

   /**
    * 'Moves' the pawns set-wise one square forward.
    * 
    * @param chessboard
    *           state of the board
    * @param helper
    *           distinguishes between white and black sides, since the pawns move in different directions
    * @return list of moves found by this method
    */
   private List<Move> moveOneSquareForward(Chessboard chessboard, MoveHelper helper) {
      List<Move> moves = new ArrayList<>();
      // 1) one square forward:
      // shift by 8 and check if empty square
      // 6) promotion:
      // extra check for pawns on the 8th rank
      BitSet oneSquareForward = helper.moveOneRank(pieces.getBitSet());
      oneSquareForward.and(chessboard.getEmptySquares().getBitSet()); // move must be to an empty square
      BitSet promotedPawns = (BitSet) oneSquareForward.clone(); // copy this bitset
      promotedPawns.and(helper.lastRank().getBitSet()); // just the promoted pawns
      oneSquareForward.and(helper.lastRank().flip()); // remove promoted pawns
      int offset = helper.getColour() == Colour.WHITE ? -8 : 8;
      for (int i = oneSquareForward.nextSetBit(0); i >= 0; i = oneSquareForward.nextSetBit(i + 1)) {
         moves.add(new Move(this, Square.fromBitPosn(i + offset), Square.fromBitPosn(i)));
      }
      for (int i = promotedPawns.nextSetBit(0); i >= 0; i = promotedPawns.nextSetBit(i + 1)) {
         for (PieceType type : PieceType.values()) {
            if ((type == PieceType.KING) || (type == PieceType.PAWN)) {
               continue;
            }
            Move move = new Move(this, Square.fromBitPosn(i + offset), Square.fromBitPosn(i));
            move.setPromotionPiece(type);
            moves.add(move);
         }
      }
      return moves;
   }

   /**
    * 'Moves' the pawns set-wise two squares forward.
    * 
    * @param chessboard
    *           state of the board
    * @param helper
    *           distinguishes between white and black sides, since the pawns move in different directions
    * @return list of moves found by this method
    */
   private List<Move> moveTwoSquaresForward(Chessboard chessboard, MoveHelper helper) {
      List<Move> moves = new ArrayList<>();
      // 2) two squares forward:
      // first just take the pawns on the 2nd rank (relative to colour), since only these can still move two squares
      // then shift by 8 and check if empty square
      // shift again by 8 and check if empty square
      BitSet twoSquaresForward = pieces.cloneBitSet();
      twoSquaresForward.and(helper.startRank().getBitSet()); // only the pawns on the 2nd rank
      twoSquaresForward = helper.moveOneRank(twoSquaresForward);
      twoSquaresForward.and(chessboard.getEmptySquares().getBitSet()); // move must be to an empty square
      twoSquaresForward = helper.moveOneRank(twoSquaresForward);
      twoSquaresForward.and(chessboard.getEmptySquares().getBitSet()); // move must be to an empty square
      int offset = helper.getColour() == Colour.WHITE ? -16 : 16;
      for (int i = twoSquaresForward.nextSetBit(0); i >= 0; i = twoSquaresForward.nextSetBit(i + 1)) {
         moves.add(new Move(this, Square.fromBitPosn(i + offset), Square.fromBitPosn(i)));
      }
      return moves;
   }

   /**
    * Captures 'left' from white's POV e.g. b3xa4 or for a black move e.g. b6xa5.
    * 
    * @param chessboard
    *           state of the board
    * @param helper
    *           distinguishes between white and black sides, since the pawns move in different directions
    * @return list of moves found by this method
    */
   private List<Move> captureLeft(Chessboard chessboard, MoveHelper helper) {
      List<Move> moves = new ArrayList<>();
      // 3) capture left
      // first remove the pawns on the first file
      // then shift by 7 (for white) or 9 (for black) and AND with opposition pieces
      BitSet captureLeft = pieces.cloneBitSet();
      captureLeft.and(BitBoard.NOT_FILE_ONE.getBitSet()); // only the pawns on the 2nd to 8th files
      captureLeft = helper.pawnCaptureLeft(captureLeft);
      // move must be a capture, therefore AND with opponent's pieces
      BitSet opponentsPieces = chessboard.getAllPieces(Colour.oppositeColour(helper.getColour())).cloneBitSet();
      // 5) enpassant: add in enpassant square if available
      addEnpassantSquare(chessboard, opponentsPieces);
      captureLeft.and(opponentsPieces);
      int offset = helper.getColour() == Colour.WHITE ? -7 : 9;
      for (int i = captureLeft.nextSetBit(0); i >= 0; i = captureLeft.nextSetBit(i + 1)) {
         moves.add(new Move(this, Square.fromBitPosn(i + offset), Square.fromBitPosn(i), true));
      }
      return moves;
   }

   /**
    * Captures 'right' from white's POV e.g. b3xc4 or for a black move e.g. b6xc5.
    * 
    * @param chessboard
    *           state of the board
    * @param helper
    *           distinguishes between white and black sides, since the pawns move in different directions
    * @return list of moves found by this method
    */
   private List<Move> captureRight(Chessboard chessboard, MoveHelper helper) {
      List<Move> moves = new ArrayList<>();
      // 4) capture right
      // first remove the pawns on the eigth file
      // then shift by 9 (for white) or 7 (for black) and AND with opposition pieces
      BitSet captureRight = pieces.cloneBitSet();
      captureRight.and(BitBoard.NOT_FILE_EIGHT.getBitSet()); // only the pawns on the 1st to 7th files
      captureRight = helper.pawnCaptureRight(captureRight);
      // move must be a capture, therefore AND with opponent's pieces
      BitSet opponentsPieces = chessboard.getAllPieces(Colour.oppositeColour(this.colour)).cloneBitSet();
      // 5) enpassant: add in enpassant square if available
      addEnpassantSquare(chessboard, opponentsPieces);
      captureRight.and(opponentsPieces);
      int offset = helper.getColour() == Colour.WHITE ? -9 : 7;
      for (int i = captureRight.nextSetBit(0); i >= 0; i = captureRight.nextSetBit(i + 1)) {
         moves.add(new Move(this, Square.fromBitPosn(i + offset), Square.fromBitPosn(i), true));
      }
      return moves;
   }

   /**
    * Factors out the differences between white pawn moves (going up the board) and black pawn moves (going down).
    */
   private interface MoveHelper {
      /**
       * Shifts the given bitset one rank north or south.
       * 
       * @param bs
       *           start bitset
       * @return shifted bitset
       */
      BitSet moveOneRank(BitSet bs);

      /**
       * Given the starting bitset, returns a new bitset representing the pawn capture 'to the right' as seen from
       * white's POV, e.g. b3xc4 or for a black move e.g. b6xc5.
       * 
       * @param startPosn
       *           starting bitset
       * @return the shifted bitset
       */
      BitSet pawnCaptureRight(BitSet startPosn);

      /**
       * Given the starting bitset, returns a new bitset representing the pawn capture 'to the left' as seen from
       * white's POV, e.g. b3xa4 or for a black move e.g. b6xa5.
       * 
       * @param startPosn
       *           starting bitset
       * @return the shifted bitset
       */
      BitSet pawnCaptureLeft(BitSet captureLeft);

      /**
       * @return the colour represented by this helper class.
       */
      Colour getColour();

      /**
       * The last rank (1st or 8th) depending on the colour.
       * 
       * @return The last rank
       */
      BitBoard lastRank();

      /**
       * The starting rank for the pawns (2nd or 6th) depending on the colour.
       * 
       * @return The starting rank
       */
      BitBoard startRank();
   }

   /**
    * Implements the MoveHelper interface from white's POV.
    */
   static class WhiteMoveHelper implements MoveHelper {

      @Override
      public BitSet moveOneRank(BitSet bs) {
         return BitSetHelper.shiftOneNorth(bs);
      }

      @Override
      public BitBoard lastRank() {
         return BitBoard.RANK_EIGHT;
      }

      @Override
      public Colour getColour() {
         return Colour.WHITE;
      }

      @Override
      public BitBoard startRank() {
         return BitBoard.RANK_TWO;
      }

      @Override
      public BitSet pawnCaptureLeft(BitSet startPosn) {
         if (startPosn.isEmpty()) {
            return startPosn;
         }
         long lo = startPosn.toLongArray()[0];
         return BitSet.valueOf(new long[] { (lo << 7) });
      }

      @Override
      public BitSet pawnCaptureRight(BitSet startPosn) {
         if (startPosn.isEmpty()) {
            return startPosn;
         }
         long lo = startPosn.toLongArray()[0];
         return BitSet.valueOf(new long[] { (lo << 9) });
      }

   }

   /**
    * Implements the MoveHelper interface from black's POV.
    */
   static class BlackSideHelper implements MoveHelper {

      @Override
      public BitSet moveOneRank(BitSet bs) {
         return BitSetHelper.shiftOneSouth(bs);
      }

      @Override
      public BitSet pawnCaptureRight(BitSet startPosn) {
         if (startPosn.isEmpty()) {
            return startPosn;
         }
         long lo = startPosn.toLongArray()[0];
         return BitSet.valueOf(new long[] { (lo >>> 7) });
      }

      @Override
      public BitSet pawnCaptureLeft(BitSet startPosn) {
         if (startPosn.isEmpty()) {
            return startPosn;
         }
         long lo = startPosn.toLongArray()[0];
         return BitSet.valueOf(new long[] { (lo >>> 9) });
      }

      @Override
      public Colour getColour() {
         return Colour.BLACK;
      }

      @Override
      public BitBoard lastRank() {
         return BitBoard.RANK_ONE;
      }

      @Override
      public BitBoard startRank() {
         return BitBoard.RANK_SEVEN;
      }

   }
}
