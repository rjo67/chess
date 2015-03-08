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
 * Stores information about the pawns (still) in the game.
 *
 * @author rich
 */
public class Pawn extends Piece {

   private MoveHelper helper;

   /**
    * Constructs the Pawn class -- with no pawns on the board. Delegates to Pawn(Colour, boolean) with parameter false.
    *
    * @param colour
    *           indicates the colour of the pieces
    */
   public Pawn(Colour colour) {
      this(colour, false);
   }

   /**
    * Constructs the Pawn class.
    *
    * @param colour
    *           indicates the colour of the pieces
    * @param startPosition
    *           if true, the default start squares are assigned. If false, no pieces are placed on the board.
    */
   public Pawn(Colour colour, boolean startPosition) {
      this(colour, startPosition, (Square[]) null);
   }

   /**
    * Constructs the Pawn class, defining the start squares.
    *
    * @param colour
    *           indicates the colour of the pieces
    * @param startSquares
    *           the required starting squares of the piece(s). Can be null, in which case no pieces are placed on the
    *           board.
    */
   public Pawn(Colour colour, Square... startSquares) {
      this(colour, false, startSquares);
   }

   /**
    * Constructs the Pawn class with the required squares (can be null) or the default start squares.
    * Setting 'startPosition' true has precedence over 'startSquares'.
    *
    * @param colour
    *           indicates the colour of the pieces
    * @param startPosition
    *           if true, the default start squares are assigned. Value of 'startSquares' will be ignored.
    * @param startSquares
    *           the required starting squares of the piece(s). Can be null, in which case no pawns are placed on the
    *           board.
    */
   public Pawn(Colour colour, boolean startPosition, Square... startSquares) {
      super(colour, PieceType.PAWN);
      helper = colour == Colour.WHITE ? new WhiteMoveHelper() : new BlackSideHelper();
      if (startPosition) {
         initPosition();
      } else {
         initPosition(startSquares);
      }
   }

   @Override
   public void initPosition() {
      Square[] requiredSquares = null;
      // @formatter:off
      requiredSquares = colour == Colour.WHITE
            ? new Square[] { Square.a2, Square.b2, Square.c2, Square.d2, Square.e2, Square.f2, Square.g2, Square.h2 }
      : new Square[] { Square.a7, Square.b7, Square.c7, Square.d7, Square.e7, Square.f7, Square.g7, Square.h7 };
            // @formatter:on
      initPosition(requiredSquares);
   }

   @Override
   public List<Move> findMoves(Game game) {

      /*
       * The pawn move is complicated by the different directions for white and black pawns.
       * This is the only piece to have this complication.
       * 
       * This difference is catered for by the 'MoveHelper' implementations.
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
      moves.addAll(moveOneSquareForward(game.getChessboard(), helper));
      moves.addAll(moveTwoSquaresForward(game.getChessboard(), helper));
      moves.addAll(captureLeft(game.getChessboard(), helper, false));
      moves.addAll(captureRight(game.getChessboard(), helper, false));

      // checks
      Square opponentsKing = King.findOpponentsKing(colour, game.getChessboard());
      for (Move move : moves) {
         boolean isCheck = checkIfCheck(game.getChessboard(), move, opponentsKing);
         // if it's already check, don't need to calculate discovered check
         if (!isCheck) {
            isCheck = Chessboard.checkForDiscoveredCheck(game.getChessboard(), move, colour, opponentsKing);
         }
         move.setCheck(isCheck);
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
         opponentsPieces.set(enpassantSquare.bitIndex());
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
         moves.add(new Move(PieceType.PAWN, colour, Square.fromBitIndex(i + offset), Square.fromBitIndex(i)));
      }
      for (int i = promotedPawns.nextSetBit(0); i >= 0; i = promotedPawns.nextSetBit(i + 1)) {
         for (PieceType type : PieceType.getPieceTypesForPromotion()) {
            Move move = new Move(PieceType.PAWN, colour, Square.fromBitIndex(i + offset), Square.fromBitIndex(i));
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
         moves.add(new Move(PieceType.PAWN, colour, Square.fromBitIndex(i + offset), Square.fromBitIndex(i)));
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
    * @param checkingForAttack
    *           if true, this routine returns all possible moves to the 'left'. The normal value of false returns only
    *           moves
    *           which are captures i.e. the opponent's pieces are taken into account.
    * @return list of moves found by this method
    */
   private List<Move> captureLeft(Chessboard chessboard, MoveHelper helper, boolean checkingForAttack) {
      List<Move> moves = new ArrayList<>();
      // 3) capture left
      // then shift by 7 (for white) or 9 (for black) and AND with opposition pieces

      BitSet captureLeft = helper.pawnCaptureLeft(pieces.cloneBitSet());

      if (!checkingForAttack) {
         // move must be a capture, therefore AND with opponent's pieces
         BitSet opponentsPieces = chessboard.getAllPieces(Colour.oppositeColour(helper.getColour())).cloneBitSet();
         // 5) enpassant: add in enpassant square if available
         addEnpassantSquare(chessboard, opponentsPieces);
         captureLeft.and(opponentsPieces);
      }
      int offset = helper.getColour() == Colour.WHITE ? -7 : 9;
      for (int i = captureLeft.nextSetBit(0); i >= 0; i = captureLeft.nextSetBit(i + 1)) {
         Square targetSquare = Square.fromBitIndex(i);
         PieceType capturedPiece;
         // no piece present on the attack square if 'checkingForAttack'
         if (checkingForAttack) {
            capturedPiece = PieceType.DUMMY;
         } else {
            capturedPiece = targetSquare == chessboard.getEnpassantSquare() ? PieceType.PAWN : chessboard
                  .pieceAt(targetSquare);
         }

         moves.add(new Move(PieceType.PAWN, colour, Square.fromBitIndex(i + offset), targetSquare, capturedPiece));
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
    * @param checkingForAttack
    *           if true, this routine returns all possible moves to the 'right'. The normal value of false returns only
    *           moves
    *           which are captures i.e. the opponent's pieces are taken into account.
    * @return list of moves found by this method
    */
   private List<Move> captureRight(Chessboard chessboard, MoveHelper helper, boolean checkingForAttack) {
      List<Move> moves = new ArrayList<>();
      // 4) capture right
      // shift by 9 (for white) or 7 (for black) and AND with opposition pieces
      BitSet captureRight = helper.pawnCaptureRight(pieces.cloneBitSet());
      if (!checkingForAttack) {
         // move must be a capture, therefore AND with opponent's pieces
         BitSet opponentsPieces = chessboard.getAllPieces(Colour.oppositeColour(this.colour)).cloneBitSet();
         // 5) enpassant: add in enpassant square if available
         addEnpassantSquare(chessboard, opponentsPieces);
         captureRight.and(opponentsPieces);
      }
      int offset = helper.getColour() == Colour.WHITE ? -9 : 7;
      for (int i = captureRight.nextSetBit(0); i >= 0; i = captureRight.nextSetBit(i + 1)) {
         Square targetSquare = Square.fromBitIndex(i);
         PieceType capturedPiece;
         // no piece present on the attack square if 'checkingForAttack'
         if (checkingForAttack) {
            capturedPiece = PieceType.DUMMY;
         } else {
            capturedPiece = targetSquare == chessboard.getEnpassantSquare() ? PieceType.PAWN : chessboard
                  .pieceAt(targetSquare);
         }
         moves.add(new Move(PieceType.PAWN, colour, Square.fromBitIndex(i + offset), targetSquare, capturedPiece));
      }
      return moves;
   }

   /**
    * Calculates if the given move leaves the opponent's king in check.
    *
    * @param chessboard
    *           the board
    * @param move
    *           the pawn move
    * @param opponentsKing
    *           square of the opponent's king
    * @return true if this move leaves the king in check
    */
   private boolean checkIfCheck(Chessboard chessboard, Move move, Square opponentsKing) {
      if (move.isPromotion()) {
         Map<PieceType, Piece> myPieces;
         BitSet emptySquares = chessboard.getEmptySquares().cloneBitSet();
         emptySquares.set(move.from().bitIndex());
         emptySquares.clear(move.to().bitIndex());

         PieceType promotedPiece = move.getPromotedPiece();
         // TODO could be improved ...
         switch (promotedPiece) {
         case QUEEN:
            // only interested in whether the promoted piece is giving check
            // therefore only need to supply this piece to the check-method
            myPieces = new HashMap<>();
            myPieces.put(PieceType.QUEEN, new Queen(colour, move.to()));
            return Chessboard.isOpponentsKingInCheck(myPieces, emptySquares, opponentsKing);
         case ROOK:
            myPieces = new HashMap<>();
            myPieces.put(PieceType.ROOK, new Rook(colour, move.to()));
            return Chessboard.isOpponentsKingInCheck(myPieces, emptySquares, opponentsKing);
         case BISHOP:
            myPieces = new HashMap<>();
            myPieces.put(PieceType.BISHOP, new Bishop(colour, move.to()));
            return Chessboard.isOpponentsKingInCheck(myPieces, emptySquares, opponentsKing);
         case KNIGHT:
            myPieces = new HashMap<>();
            myPieces.put(PieceType.KNIGHT, new Knight(colour, move.to()));
            return Chessboard.isOpponentsKingInCheck(myPieces, emptySquares, opponentsKing);
         default:
            throw new IllegalArgumentException("promotedPiece=" + promotedPiece);
         }
      } else {
         // set up bitset with the square the pawn moved to
         BitSet left = new BitSet(64);
         left.set(move.to().bitIndex());
         BitSet right = (BitSet) left.clone();

         left.and(BitBoard.EXCEPT_FILE[1].getBitSet()); // only the pawns on the 2nd to 8th files
         left = helper.pawnCaptureLeft(left);
         right.and(BitBoard.EXCEPT_FILE[7].getBitSet()); // only the pawns on the 1st to 7th files
         right = helper.pawnCaptureRight(right);

         // now 'and' with king's bitset
         // TODO shouldn't create every time
         BitSet king = new BitSet(64);
         king.set(opponentsKing.bitIndex());
         left.and(king);
         right.and(king);

         return (!left.isEmpty() || !right.isEmpty());
      }
   }

   @Override
   public boolean attacksSquare(Chessboard chessboard, Square targetSq) {
      List<Move> moves = new ArrayList<>();
      moves.addAll(captureLeft(chessboard, helper, true));
      moves.addAll(captureRight(chessboard, helper, true));
      for (Move move : moves) {
         if (move.to() == targetSq) {
            return true;
         }
      }
      return false;
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
         return BitBoard.RANK[7];
      }

      @Override
      public Colour getColour() {
         return Colour.WHITE;
      }

      @Override
      public BitBoard startRank() {
         return BitBoard.RANK[1];
      }

      @Override
      public BitSet pawnCaptureLeft(BitSet startPosn) {
         if (startPosn.isEmpty()) {
            return startPosn;
         }
         startPosn.and(BitBoard.EXCEPT_FILE[0].getBitSet()); // only the pawns on the 2nd to 8th files
         long[] longArray = startPosn.toLongArray();
         if (longArray.length == 0) {
            return new BitSet(64);
         }
         return BitSet.valueOf(new long[] { (longArray[0] << 7) });
      }

      @Override
      public BitSet pawnCaptureRight(BitSet startPosn) {
         if (startPosn.isEmpty()) {
            return startPosn;
         }
         startPosn.and(BitBoard.EXCEPT_FILE[7].getBitSet()); // only the pawns on the 1st to 7th files
         long[] longArray = startPosn.toLongArray();
         if (longArray.length == 0) {
            return new BitSet(64);
         }
         return BitSet.valueOf(new long[] { (longArray[0] << 9) });
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
         startPosn.and(BitBoard.EXCEPT_FILE[7].getBitSet()); // only the pawns on the 1st to 7th files
         long[] longArray = startPosn.toLongArray();
         if (longArray.length == 0) {
            return new BitSet(64);
         }
         return BitSet.valueOf(new long[] { (longArray[0] >>> 7) });
      }

      @Override
      public BitSet pawnCaptureLeft(BitSet startPosn) {
         if (startPosn.isEmpty()) {
            return startPosn;
         }
         startPosn.and(BitBoard.EXCEPT_FILE[0].getBitSet()); // only the pawns on the 2nd to 8th files
         long[] longArray = startPosn.toLongArray();
         if (longArray.length == 0) {
            return new BitSet(64);
         }
         return BitSet.valueOf(new long[] { (longArray[0] >>> 9) });
      }

      @Override
      public Colour getColour() {
         return Colour.BLACK;
      }

      @Override
      public BitBoard lastRank() {
         return BitBoard.RANK[0];
      }

      @Override
      public BitBoard startRank() {
         return BitBoard.RANK[6];
      }

   }

}