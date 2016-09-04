package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rjo.chess.BitBoard;
import org.rjo.chess.Chessboard;
import org.rjo.chess.Colour;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.Square;
import org.rjo.chess.util.Stopwatch;

/**
 * Stores information about the pawns (still) in the game.
 *
 * @author rich
 */
public class Pawn extends Piece {
   private static final Logger LOG = LogManager.getLogger(Pawn.class);

   /** piece value in centipawns */
   private static final int PIECE_VALUE = 100;

   /**
    * Stores the piece-square values. http://chessprogramming.wikispaces.com/Simplified+evaluation+function.
    * These values (mirrored for black) should be added to VALUE to get a piece-square value for each pawn.
    *
    * Important: array value [0] corresponds to square a1; [63] == h8.
    * For black, the position as given below corresponds to the actual board, i.e. a1 is bottom RHS [63]
    */
   private static int[] SQUARE_VALUE =
// @formatter:off
         new int[] {
      0,  0,  0,  0,  0,  0,  0,  0,
      5, 10, 10,-20,-20, 10, 10,  5,
      5, -5,-10,  0,  0,-10, -5,  5,
      0,  0,  0, 20, 20,  0,  0,  0,
      5,  5, 10, 25, 25, 10,  5,  5,
      10, 10, 20, 30, 30, 20, 10, 10,
      50, 50, 50, 50, 50, 50, 50, 50,
      0,  0,  0,  0,  0,  0,  0,  0};
   // @formatter:on

   private MoveHelper helper;

   @Override
   public int calculatePieceSquareValue() {
      return Piece.pieceSquareValue(pieces.getBitSet(), colour, PIECE_VALUE, SQUARE_VALUE);
   }

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
   public List<Move> findMoves(Game game, boolean kingInCheck) {
      Stopwatch stopwatch = new Stopwatch();

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

      // make sure king is not/no longer in check
      final Square myKing = King.findKing(colour, game.getChessboard());
      final Colour opponentsColour = Colour.oppositeColour(colour);
      Iterator<Move> iter = moves.listIterator();
      while (iter.hasNext()) {
         Move move = iter.next();
         // make sure my king is not/no longer in check
         boolean inCheck = Chessboard.isKingInCheck(game.getChessboard(), move, opponentsColour, myKing, kingInCheck);
         if (inCheck) {
            iter.remove();
         }
      }

      // checks
      Square opponentsKing = King.findOpponentsKing(colour, game.getChessboard());
      BitSet opponentsKingBitset = new BitSet(64);
      opponentsKingBitset.set(opponentsKing.bitIndex());
      // probably not worth caching discovered check results for pawns
      for (Move move : moves) {
         boolean isCheck = checkIfCheck(game.getChessboard(), move, opponentsKing, opponentsKingBitset);
         // if it's already check, don't need to calculate discovered check
         if (!isCheck) {
            isCheck = Chessboard.checkForDiscoveredCheck(game.getChessboard(), move, colour, opponentsKing);
         }
         move.setCheck(isCheck);
      }

      long time = stopwatch.read();
      if (time != 0) {
         LOG.debug("found " + moves.size() + " moves in " + time);
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
      int offset = helper.getColour() == Colour.WHITE ? -8 : 8;
      for (int i = oneSquareForward.nextSetBit(0); i >= 0; i = oneSquareForward.nextSetBit(i + 1)) {
         if (helper.onLastRank(i)) {
            for (PieceType type : PieceType.getPieceTypesForPromotion()) {
               Move move = new Move(PieceType.PAWN, colour, Square.fromBitIndex(i + offset), Square.fromBitIndex(i));
               move.setPromotionPiece(type);
               moves.add(move);
            }
         } else {
            moves.add(new Move(PieceType.PAWN, colour, Square.fromBitIndex(i + offset), Square.fromBitIndex(i)));
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
      twoSquaresForward.and(helper.startRank()); // only the pawns on the 2nd rank
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
    * Helper method to check for captures 'left' or 'right'.
    *
    * @param chessboard
    *           state of the board
    * @param helper
    *           distinguishes between white and black sides, since the pawns move in different directions
    * @param captureLeft
    *           if true, check for captures 'left'. Otherwise, 'right'.
    * @param checkingForAttack
    *           if true, this routine returns all possible moves to the 'left'. The normal value of false returns only
    *           moves which are captures i.e. the opponent's pieces are taken into account.
    * @return list of moves found by this method
    */
   private List<Move> capture(Chessboard chessboard, MoveHelper helper, boolean captureLeft,
         boolean checkingForAttack) {

      BitSet captures;
      if (captureLeft) {
         captures = helper.pawnCaptureLeft(pieces.cloneBitSet());
      } else {
         captures = helper.pawnCaptureRight(pieces.cloneBitSet());
      }

      if (!checkingForAttack) {
         // move must be a capture, therefore AND with opponent's pieces
         BitSet opponentsPieces = chessboard.getAllPieces(Colour.oppositeColour(helper.getColour())).cloneBitSet();
         // 5) enpassant: add in enpassant square if available
         addEnpassantSquare(chessboard, opponentsPieces);
         captures.and(opponentsPieces);
      }

      int offset;
      if (captureLeft) {
         offset = helper.getColour() == Colour.WHITE ? -7 : 9;
      } else {
         offset = helper.getColour() == Colour.WHITE ? -9 : 7;
      }

      List<Move> moves = new ArrayList<>();
      Colour oppositeColour = Colour.oppositeColour(colour);
      for (int i = captures.nextSetBit(0); i >= 0; i = captures.nextSetBit(i + 1)) {
         Square targetSquare = Square.fromBitIndex(i);
         if (helper.onLastRank(i)) {
            // capture with promotion
            Square fromSquare = Square.fromBitIndex(i + offset);
            PieceType capturedPiece = checkingForAttack ? PieceType.DUMMY
                  : chessboard.pieceAt(targetSquare, oppositeColour);
            for (PieceType type : PieceType.getPieceTypesForPromotion()) {
               Move move = new Move(PieceType.PAWN, colour, fromSquare, targetSquare, capturedPiece);
               move.setPromotionPiece(type);
               moves.add(move);
            }
         } else {
            PieceType capturedPiece;
            boolean enpassant = false;
            // no piece present on the attack square if 'checkingForAttack'
            if (checkingForAttack) {
               capturedPiece = PieceType.DUMMY;
            } else {
               if (targetSquare == chessboard.getEnpassantSquare()) {
                  capturedPiece = PieceType.PAWN;
                  enpassant = true;
               } else {
                  capturedPiece = chessboard.pieceAt(targetSquare, oppositeColour);
               }
            }
            Move move;
            if (enpassant) {
               move = Move.enpassant(colour, Square.fromBitIndex(i + offset), targetSquare);
            } else {
               move = new Move(PieceType.PAWN, colour, Square.fromBitIndex(i + offset), targetSquare, capturedPiece);
            }
            moves.add(move);
         }
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
    *           moves which are captures i.e. the opponent's pieces are taken into account.
    * @return list of moves found by this method
    */
   private List<Move> captureLeft(Chessboard chessboard, MoveHelper helper, boolean checkingForAttack) {
      return capture(chessboard, helper, true, checkingForAttack);
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
      return capture(chessboard, helper, false, checkingForAttack);
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
    * @param opponentsKingBitset
    *           bitset for the opponent's king (passed in as optimization)
    * @return true if this move leaves the king in check
    */
   private boolean checkIfCheck(Chessboard chessboard, Move move, Square opponentsKing, BitSet opponentsKingBitset) {
      if (move.isPromotion()) {
         if (move.getPromotedPiece() == PieceType.KNIGHT) {
            return Knight.checkIfMoveAttacksSquare(move, opponentsKing.bitIndex());
         } else {
            BitSet emptySquares = chessboard.getEmptySquares().cloneBitSet();
            emptySquares.set(move.from().bitIndex());
            emptySquares.clear(move.to().bitIndex());

            PieceType promotedPiece = move.getPromotedPiece();
            switch (promotedPiece) {
            case QUEEN:
               return SlidingPiece.attacksSquareRankOrFile(emptySquares, move.to(), opponentsKing)
                     || SlidingPiece.attacksSquareDiagonally(emptySquares, move.to(), opponentsKing);
            case ROOK:
               return SlidingPiece.attacksSquareRankOrFile(emptySquares, move.to(), opponentsKing);
            case BISHOP:
               return SlidingPiece.attacksSquareDiagonally(emptySquares, move.to(), opponentsKing);
            default:
               throw new IllegalArgumentException("promotedPiece=" + promotedPiece);
            }
         }
      } else {
         // set up bitset with the square the pawn moved to
         BitSet left = new BitSet(64);
         left.set(move.to().bitIndex());
         BitSet right = (BitSet) left.clone();

         left = helper.pawnCaptureLeft(left);
         right = helper.pawnCaptureRight(right);

         // now 'and' with opponent's king's bitset
         left.and(opponentsKingBitset);
         right.and(opponentsKingBitset);

         return (!left.isEmpty() || !right.isEmpty());
      }
   }

   private static boolean doWhitePawnsAttackSquare(Square targetSq, BitSet whitePawns) {
      if (targetSq.rank() < 2) {
         return false;
      }
      final int index = targetSq.bitIndex();
      // attack from left
      if ((targetSq.file() > 0) && (whitePawns.get(index - 9))) {
         return true;
      }
      // attack from right
      if ((targetSq.file() < 7) && (whitePawns.get(index - 7))) {
         return true;
      }
      return false;
   }

   private static boolean doBlackPawnsAttackSquare(Square targetSq, BitSet blackPawns) {
      if (targetSq.rank() > 5) {
         return false;
      }
      final int index = targetSq.bitIndex();
      // attack from left
      if ((targetSq.file() > 0) && (blackPawns.get(index + 7))) {
         return true;
      }
      // attack from right
      if ((targetSq.file() < 7) && (blackPawns.get(index + 9))) {
         return true;
      }
      return false;
   }

   @Override
   public boolean attacksSquare(BitSet notused, Square targetSq) {
      if (colour == Colour.WHITE) {
         return doWhitePawnsAttackSquare(targetSq, pieces.getBitSet());
      } else {
         return doBlackPawnsAttackSquare(targetSq, pieces.getBitSet());

      }
   }

   /**
    * Whether one or more of the pawns described in 'pawns' attack the square 'targetSq'.
    *
    * @param targetSq
    *           square to be attacked
    * @param colour
    *           colour of the pawns in 'pawns'.
    * @param pawns
    *           bitset describing where the pawns are
    * @return true if 'targetSq' is attacked by one or more pawns
    */
   public static boolean attacksSquare(Square targetSq, Colour colour, BitSet pawns) {
      switch (colour) {
      case WHITE:
         return doWhitePawnsAttackSquare(targetSq, pawns);
      case BLACK:
         return doBlackPawnsAttackSquare(targetSq, pawns);
      default:
         throw new IllegalArgumentException("bad value for colour!? : " + colour);
      }
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
       * returns true if the given bitIndex is on the 'last rank' of the board.
       *
       * @param bitIndex
       *           the bitIndex
       * @return true if on last rank.
       */
      boolean onLastRank(int bitIndex);

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
       * The last rank (1st or 8th depending on the colour).
       *
       * @return The last rank
       */
      BitSet lastRank();

      /**
       * All ranks apart from the last rank (1st or 8th depending on the colour).
       *
       * @return The last rank flipped, i.e. all ranks apart from the last rank.
       */
      BitSet lastRankFlipped();

      /**
       * The starting rank for the pawns (2nd or 6th) depending on the colour.
       *
       * @return The starting rank
       */
      BitSet startRank();
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
      public BitSet lastRank() {
         return BitBoard.RANK[7];
      }

      @Override
      public BitSet lastRankFlipped() {
         return BitBoard.EXCEPT_RANK[7];
      }

      @Override
      public Colour getColour() {
         return Colour.WHITE;
      }

      @Override
      public BitSet startRank() {
         return BitBoard.RANK[1];
      }

      @Override
      public BitSet pawnCaptureLeft(BitSet startPosn) {
         if (startPosn.isEmpty()) {
            return startPosn;
         }
         startPosn.and(BitBoard.EXCEPT_FILE[0]); // only the pawns on the 2nd to 8th files
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
         startPosn.and(BitBoard.EXCEPT_FILE[7]); // only the pawns on the 1st to 7th files
         long[] longArray = startPosn.toLongArray();
         if (longArray.length == 0) {
            return new BitSet(64);
         }
         return BitSet.valueOf(new long[] { (longArray[0] << 9) });
      }

      @Override
      public boolean onLastRank(int bitIndex) {
         return bitIndex > 55;
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
         startPosn.and(BitBoard.EXCEPT_FILE[7]); // only the pawns on the 1st to 7th files
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
         startPosn.and(BitBoard.EXCEPT_FILE[0]); // only the pawns on the 2nd to 8th files
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
      public BitSet lastRank() {
         return BitBoard.RANK[0];
      }

      @Override
      public BitSet lastRankFlipped() {
         return BitBoard.EXCEPT_RANK[0];
      }

      @Override
      public BitSet startRank() {
         return BitBoard.RANK[6];
      }

      @Override
      public boolean onLastRank(int bitIndex) {
         return bitIndex < 8;
      }
   }

}