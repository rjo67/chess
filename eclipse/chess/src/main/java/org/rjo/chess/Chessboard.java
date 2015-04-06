package org.rjo.chess;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.rjo.chess.pieces.Bishop;
import org.rjo.chess.pieces.King;
import org.rjo.chess.pieces.Knight;
import org.rjo.chess.pieces.Pawn;
import org.rjo.chess.pieces.Piece;
import org.rjo.chess.pieces.PieceType;
import org.rjo.chess.pieces.Queen;
import org.rjo.chess.pieces.Rook;
import org.rjo.chess.pieces.SlidingPiece;
import org.rjo.chess.ray.RayUtils;

public class Chessboard {

   /**
    * Stores the pieces in the game.
    * The dimension indicates the colour {white, black}.
    */
   private Map<PieceType, Piece>[] pieces;

   /**
    * bitboard of all pieces for a particular colour.
    * The dimension indicates the colour {white, black}.
    */
   private BitBoard[] allPieces;

   /**
    * bitboard of all rooks and queens for a particular colour.
    * The dimension indicates the colour {white, black}.
    */
   private BitBoard[] allRooksAndQueens;

   /**
    * bitboard of all bishops and queens for a particular colour.
    * The dimension indicates the colour {white, black}.
    */
   private BitBoard[] allBishopsAndQueens;

   /**
    * bitboard of all pieces on the board (irrespective of colour).
    */
   private BitBoard totalPieces;

   /**
    * bitboard of all empty squares on the board. Logical NOT of {@link #totalPieces}.
    */
   private BitBoard emptySquares;

   /**
    * Indicates an enpassant square; can be null.
    */
   private Square enpassantSquare;

   /**
    * Creates a chessboard with default piece settings.
    */
   public Chessboard() {
      Set<Piece>[] pieces = new HashSet[Colour.values().length];
      for (Colour col : Colour.values()) {
         pieces[col.ordinal()] = new HashSet<Piece>(Arrays.asList(new Pawn(col, true), new Rook(col, true), new Knight(
               col, true), new Bishop(col, true), new Queen(col, true), new King(col, true)));
      }
      initBoard(pieces[Colour.WHITE.ordinal()], pieces[Colour.BLACK.ordinal()]);
   }

   /**
    * Creates a chessboard with the given piece settings.
    */
   public Chessboard(Set<Piece> whitePieces, Set<Piece> blackPieces) {
      initBoard(whitePieces, blackPieces);
   }

   private void initBoard(Set<Piece> whitePieces, Set<Piece> blackPieces) {
      pieces = new HashMap[Colour.values().length];
      pieces[Colour.WHITE.ordinal()] = new HashMap<>();
      for (Piece p : whitePieces) {
         pieces[Colour.WHITE.ordinal()].put(p.getType(), p);
      }
      pieces[Colour.BLACK.ordinal()] = new HashMap<>();
      for (Piece p : blackPieces) {
         pieces[Colour.BLACK.ordinal()].put(p.getType(), p);
      }
      allPieces = new BitBoard[Colour.values().length];
      allRooksAndQueens = new BitBoard[Colour.values().length];
      allBishopsAndQueens = new BitBoard[Colour.values().length];
      for (Colour colour : Colour.values()) {
         allPieces[colour.ordinal()] = new BitBoard();
         for (PieceType p : pieces[colour.ordinal()].keySet()) {
            allPieces[colour.ordinal()].getBitSet().or(pieces[colour.ordinal()].get(p).getBitBoard().getBitSet());
         }
         allRooksAndQueens[colour.ordinal()] = updateRooksAndQueens(pieces[colour.ordinal()]);
         allBishopsAndQueens[colour.ordinal()] = updateBishopsAndQueens(pieces[colour.ordinal()]);
      }
      totalPieces = new BitBoard();
      totalPieces.getBitSet().or(allPieces[Colour.WHITE.ordinal()].getBitSet());
      totalPieces.getBitSet().or(allPieces[Colour.BLACK.ordinal()].getBitSet());
      emptySquares = new BitBoard(totalPieces.cloneBitSet());
      emptySquares.getBitSet().flip(0, 64);

      enpassantSquare = null;
   }

   /**
    * update the internal structures (after a move/unmove). Incremental update for non-capture moves.
    *
    * @param move
    *           the move
    * @param isUnmove
    *           true means this move is an 'unmove'. Otherwise a normal 'move'. Only relevant when move!=null.
    *           TODO this parameter may not be required
    */
   public void updateStructures(Move move, boolean isUnmove) {
      // @formatter:off
      // (f=flip)
      // White-Move       non-capture      capture
      //                  d3-d4   Ra3-a4   d3xe4   d7xc8=Q  Ra4xRa8  d5xc6 e.p.
      // allPieces   W    f  f     f  f    f  f    f  f      f   f   f  f
      // allR+Q      W             f  f               f      f   f
      // allB+Q      W                                f
      // allPieces   B    f  f     f  f       f       f          f      f(c7)
      // allR+Q      B                    (when capt.            f
      // allB+Q      B                      piece!=RBQ)
      // totalPieces      f  f     f  f    f       f         f       f  f f(c7)
      // emptySquares     f  f     f  f    f       f         f       f  f f(c7)
      //
      // @formatter:on

      final int colourOrdinal = move.getColour().ordinal();
      final int oppositeColourOrdinal = Colour.oppositeColour(move.getColour()).ordinal();
      final int moveFromBitIndex = move.from().bitIndex();
      final int moveToBitIndex = move.to().bitIndex();

      // update incrementally for non-capture moves
      if (!move.isCapture()) {
         updateBitSet(allPieces[colourOrdinal].getBitSet(), move);
         // rooks and queens
         if ((move.getPiece() == PieceType.ROOK) || (move.getPiece() == PieceType.QUEEN)) {
            updateBitSet(allRooksAndQueens[colourOrdinal].getBitSet(), move);
         }
         if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
            allRooksAndQueens[colourOrdinal].getBitSet().flip(move.getRooksCastlingMove().from().bitIndex());
            allRooksAndQueens[colourOrdinal].getBitSet().flip(move.getRooksCastlingMove().to().bitIndex());
         }
         // promotion to rook or queen?
         if (move.isPromotion()
               && (move.getPromotedPiece() == PieceType.QUEEN || move.getPromotedPiece() == PieceType.ROOK)) {
            allRooksAndQueens[colourOrdinal].getBitSet().flip(moveToBitIndex);
         }
         // bishops and queens
         if ((move.getPiece() == PieceType.BISHOP) || (move.getPiece() == PieceType.QUEEN)) {
            updateBitSet(allBishopsAndQueens[colourOrdinal].getBitSet(), move);
         }
         // promotion to bishop or queen?
         if (move.isPromotion()
               && (move.getPromotedPiece() == PieceType.QUEEN || move.getPromotedPiece() == PieceType.BISHOP)) {
            allBishopsAndQueens[colourOrdinal].getBitSet().flip(moveToBitIndex);
         }
         updateBitSet(totalPieces.getBitSet(), move);
         updateBitSet(emptySquares.getBitSet(), move);
      } else {
         // capture move
         if (!move.isEnpassant()) {
            allPieces[colourOrdinal].getBitSet().flip(moveFromBitIndex);
            allPieces[colourOrdinal].getBitSet().flip(moveToBitIndex);
            allPieces[oppositeColourOrdinal].getBitSet().flip(moveToBitIndex);
            totalPieces.getBitSet().flip(moveFromBitIndex);
            emptySquares.getBitSet().flip(moveFromBitIndex);

            // rooks and queens
            if ((move.getPiece() == PieceType.ROOK) || (move.getPiece() == PieceType.QUEEN)) {
               updateBitSet(allRooksAndQueens[colourOrdinal].getBitSet(), move);
            }
            // promotion to rook or queen?
            if (move.isPromotion()
                  && (move.getPromotedPiece() == PieceType.QUEEN || move.getPromotedPiece() == PieceType.ROOK)) {
               allRooksAndQueens[colourOrdinal].getBitSet().flip(moveToBitIndex);
            }
            // opponents rook or queen taken?
            if ((move.getCapturedPiece() == PieceType.ROOK) || (move.getCapturedPiece() == PieceType.QUEEN)) {
               allRooksAndQueens[oppositeColourOrdinal].getBitSet().flip(moveToBitIndex);
            }
            // bishops and queens
            if ((move.getPiece() == PieceType.BISHOP) || (move.getPiece() == PieceType.QUEEN)) {
               updateBitSet(allBishopsAndQueens[colourOrdinal].getBitSet(), move);
            }
            // promotion to bishop or queen?
            if (move.isPromotion()
                  && (move.getPromotedPiece() == PieceType.QUEEN || move.getPromotedPiece() == PieceType.BISHOP)) {
               allBishopsAndQueens[colourOrdinal].getBitSet().flip(moveToBitIndex);
            }
            // opponents bishop or queen taken?
            if ((move.getCapturedPiece() == PieceType.BISHOP) || (move.getCapturedPiece() == PieceType.QUEEN)) {
               allBishopsAndQueens[oppositeColourOrdinal].getBitSet().flip(moveToBitIndex);
            }
         } else {
            // enpassant
            int enpassantSquareBitIndex = Square.findMoveFromEnpassantSquare(move.to()).bitIndex();
            allPieces[colourOrdinal].getBitSet().flip(moveFromBitIndex);
            allPieces[colourOrdinal].getBitSet().flip(moveToBitIndex);
            allPieces[oppositeColourOrdinal].getBitSet().flip(enpassantSquareBitIndex);
            totalPieces.getBitSet().flip(moveFromBitIndex);
            totalPieces.getBitSet().flip(moveToBitIndex);
            totalPieces.getBitSet().flip(enpassantSquareBitIndex);
            emptySquares.getBitSet().flip(moveFromBitIndex);
            emptySquares.getBitSet().flip(moveToBitIndex);
            emptySquares.getBitSet().flip(enpassantSquareBitIndex);
         }
      }
   }

   private BitBoard updateBishopsAndQueens(Map<PieceType, Piece> pieces) {
      Piece queens = pieces.get(PieceType.QUEEN);
      BitSet queensBitSet;
      if (queens == null) {
         queensBitSet = new BitSet(64);
      } else {
         queensBitSet = queens.getBitBoard().getBitSet();
      }

      Piece bishops = pieces.get(PieceType.BISHOP);
      BitSet bishopsAndQueens;
      if (bishops == null) {
         bishopsAndQueens = new BitSet(64);
      } else {
         bishopsAndQueens = bishops.getBitBoard().cloneBitSet();
      }
      bishopsAndQueens.or(queensBitSet);
      return new BitBoard(bishopsAndQueens);
   }

   private BitBoard updateRooksAndQueens(Map<PieceType, Piece> pieces) {
      Piece queens = pieces.get(PieceType.QUEEN);
      BitSet queensBitSet;
      if (queens == null) {
         queensBitSet = new BitSet(64);
      } else {
         queensBitSet = queens.getBitBoard().getBitSet();
      }

      Piece rooks = pieces.get(PieceType.ROOK);
      BitSet rooksAndQueens;
      if (rooks == null) {
         rooksAndQueens = new BitSet(64);
      } else {
         rooksAndQueens = rooks.getBitBoard().cloneBitSet();
      }
      rooksAndQueens.or(queensBitSet);
      return new BitBoard(rooksAndQueens);
   }

   /**
    * Updates the given bitset to represent the move. The from and to squares will be flipped. If castling then the
    * rook's move is also taken into a/c.
    *
    * @param bitset
    *           the bitset to be updated.
    * @param move
    *           the move. NB only non-capture moves are supported by this method!
    */
   private void updateBitSet(BitSet bitset, Move move) {
      bitset.flip(move.from().bitIndex());
      bitset.flip(move.to().bitIndex());
      if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
         bitset.flip(move.getRooksCastlingMove().from().bitIndex());
         bitset.flip(move.getRooksCastlingMove().to().bitIndex());
      }
   }

   /**
    * Access to the set of pieces of a given colour.
    *
    * @param colour
    *           the required colour
    * @return the set of pieces of this colour
    */
   public Map<PieceType, Piece> getPieces(Colour colour) {
      return pieces[colour.ordinal()];
   }

   /**
    * Access to a BitBoard of all the pieces of a given colour.
    *
    * @param colour
    *           the required colour
    * @return a BitBoard containing all the pieces of a given colour.
    */
   public BitBoard getAllPieces(Colour colour) {
      return allPieces[colour.ordinal()];
   }

   /**
    * Access to a BitBoard of all the pieces irrespective of colour.
    *
    * @return a BitBoard containing all the pieces irrespective of colour.
    */
   public BitBoard getTotalPieces() {
      return totalPieces;
   }

   public BitBoard[] getAllRooksAndQueens() {
      return allRooksAndQueens;
   }

   public BitBoard[] getAllBishopsAndQueens() {
      return allBishopsAndQueens;
   }

   /**
    * Access to a BitBoard of all the empty squares on the board.
    *
    * @return a BitBoard containing all the empty squares on the board.
    */
   public BitBoard getEmptySquares() {
      return emptySquares;
   }

   public void debug() {
      for (Colour colour : Colour.values()) {
         System.out.println(colour + " all pieces");
         System.out.println(allPieces[colour.ordinal()].display());
         System.out.println("---");
      }
      System.out.println("pieces");
      for (Colour colour : Colour.values()) {
         for (PieceType p : pieces[colour.ordinal()].keySet()) {
            System.out.println(p + ", " + colour);
            System.out.println(pieces[colour.ordinal()].get(p).getBitBoard().display());
            System.out.println("---");
         }
      }
      System.out.println("totalPieces");
      System.out.println(totalPieces.display());
      System.out.println("---");
      System.out.println("emptySquares");
      System.out.println(emptySquares.display());
      System.out.println("---");

   }

   public void setEnpassantSquare(Square enpassantSquare) {
      this.enpassantSquare = enpassantSquare;
   }

   /**
    * The enpassant square.
    *
    * @return the enpassant square or null.
    */
   public Square getEnpassantSquare() {
      return enpassantSquare;
   }

   /**
    * Returns true if the given square is attacked by any opponent's pieces.
    *
    * @param game
    *           the game
    * @param targetSquare
    *           the square to consider
    * @param opponentsColour
    *           the colour of the opponent
    * @return true if this square is attacked by the opponent
    */
   public boolean squareIsAttacked(Game game, Square targetSquare, Colour opponentsColour) {
      Map<PieceType, Piece> opponentsPieces = getPieces(opponentsColour);
      // iterate over the pieces
      // TODO instead of treating queens separately, should 'merge' them with the rooks and the bishops
      for (PieceType type : new PieceType[] { PieceType.PAWN, PieceType.KNIGHT, PieceType.KING, PieceType.ROOK,
            PieceType.BISHOP, PieceType.QUEEN }) {
         Piece piece = opponentsPieces.get(type);
         if (piece != null) {
            if (piece.attacksSquare(game.getChessboard(), targetSquare)) {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Checks for a discovered check after the move 'move'.
    *
    * @param chessboard
    *           the chessboard
    * @param move
    *           the move
    * @param colour
    *           which side is moving
    * @param opponentsKing
    *           where the opponent's king is
    * @return true if this move leads to a discovered check
    */
   public static boolean unused_____checkForDiscoveredCheck(Chessboard chessboard, Move move, Colour colour,
         Square opponentsKing) {
      // set up the empty square bitset *after* this move
      BitSet emptySquares = chessboard.getEmptySquares().cloneBitSet();
      emptySquares.set(move.from().bitIndex());
      emptySquares.clear(move.to().bitIndex());
      // don't need to consider captures here, since we're looking for a check from OUR side
      BitSet clonedRooksAndQueens = chessboard.getAllRooksAndQueens()[colour.ordinal()].cloneBitSet();
      BitSet clonedBishopsAndQueens = chessboard.getAllBishopsAndQueens()[colour.ordinal()].cloneBitSet();
      updateRooksAndQueens(clonedRooksAndQueens, move);
      updateBishopsAndQueens(clonedBishopsAndQueens, move);

      // take care of rook's move when castling
      if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
         Move rooksMove = move.getRooksCastlingMove();
         emptySquares.set(rooksMove.from().bitIndex());
         emptySquares.clear(rooksMove.to().bitIndex());
      }
      return Chessboard.isKingInCheck(chessboard.getPieces(colour), clonedRooksAndQueens, clonedBishopsAndQueens,
            emptySquares, opponentsKing, true);
   }

   /**
    * Checks for a discovered check after the move 'move'.
    *
    * @param chessboard
    *           the chessboard
    * @param move
    *           the move
    * @param colour
    *           which side is moving
    * @param opponentsKing
    *           where the opponent's king is
    * @return true if this move leads to a discovered check
    */
   public static boolean checkForDiscoveredCheck(Chessboard chessboard, Move move, Colour colour, Square opponentsKing) {
      final int moveFromIndex = move.from().bitIndex();

      // set up the emptySquares and myPieces bitsets *after* this move
      BitSet emptySquares = chessboard.getEmptySquares().cloneBitSet();
      BitSet myPieces = chessboard.getAllPieces(colour).cloneBitSet();

      emptySquares.set(moveFromIndex);
      myPieces.clear(moveFromIndex);

      // can't get a discovered check from castling

      return RayUtils.discoveredCheck(colour, chessboard, emptySquares, myPieces, opponentsKing, move.from());
   }

   /**
    * Checks if my king is in check after the move 'move'.
    * NB does not cater for castling, since one can't castle out of check.
    *
    * @param chessboard
    *           the chessboard
    * @param move
    *           the move
    * @param opponentsColour
    *           this colour's pieces will be checked
    * @param king
    *           where my king is
    * @return true if this move leaves the king in check (i.e. is an illegal move)
    */
   public static boolean isKingInCheck(Chessboard chessboard, Move move, Colour opponentsColour, Square king) {
      BitSet emptySquares = chessboard.getEmptySquares().cloneBitSet();
      emptySquares.set(move.from().bitIndex());
      emptySquares.clear(move.to().bitIndex());

      Map<PieceType, Piece> opponentsPieces = chessboard.getPieces(opponentsColour);
      BitSet clonedRooksAndQueens = chessboard.getAllRooksAndQueens()[opponentsColour.ordinal()].cloneBitSet();
      BitSet clonedBishopsAndQueens = chessboard.getAllBishopsAndQueens()[opponentsColour.ordinal()].cloneBitSet();
      // this move is not relevant for the opponent's bishops, rooks or queens -- unless it's a capture

      Piece originalPiece = null;
      if (move.isCapture()) {
         // need to remove captured piece temporarily from the appropriate Piece instance
         originalPiece = opponentsPieces.get(move.getCapturedPiece());
         try {
            Piece clonedPiece = (Piece) originalPiece.clone();
            if (move.isEnpassant()) {
               Square enpassantPawnSquare = Square.findMoveFromEnpassantSquare(move.to());
               clonedPiece.removePiece(enpassantPawnSquare);
               // blank the square where the pawn is which has been taken e.p.
               emptySquares.set(enpassantPawnSquare.bitIndex());
            } else {
               clonedPiece.removePiece(move.to());
               if ((clonedPiece.getType() == PieceType.QUEEN) || (clonedPiece.getType() == PieceType.ROOK)) {
                  clonedRooksAndQueens.clear(move.to().bitIndex());
               }
               if ((clonedPiece.getType() == PieceType.QUEEN) || (clonedPiece.getType() == PieceType.BISHOP)) {
                  clonedBishopsAndQueens.clear(move.to().bitIndex());
               }
            }
            // achtung: changing the 'global' state here, need to reset later!
            opponentsPieces.put(move.getCapturedPiece(), clonedPiece);
         } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("cannot clone:", e);
         }
      }
      boolean inCheck = Chessboard.isKingInCheck(opponentsPieces, clonedRooksAndQueens, clonedBishopsAndQueens,
            emptySquares, king, false);
      if (move.isCapture()) {
         // reset global state
         opponentsPieces.put(move.getCapturedPiece(), originalPiece);
      }
      return inCheck;

   }

   private static void updateRooksAndQueens(BitSet clonedRooksAndQueens, Move move) {
      if ((move.getPiece() == PieceType.ROOK) || (move.getPiece() == PieceType.QUEEN)) {
         clonedRooksAndQueens.flip(move.from().bitIndex());
         clonedRooksAndQueens.flip(move.to().bitIndex());
      }
      if (move.isPromotion()
            && (move.getPromotedPiece() == PieceType.QUEEN || move.getPromotedPiece() == PieceType.ROOK)) {
         clonedRooksAndQueens.flip(move.to().bitIndex());
      }
      if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
         Move rooksMove = move.getRooksCastlingMove();
         clonedRooksAndQueens.flip(rooksMove.from().bitIndex());
         clonedRooksAndQueens.flip(rooksMove.to().bitIndex());
      }
   }

   private static void updateBishopsAndQueens(BitSet clonedBishopsAndQueens, Move move) {
      if ((move.getPiece() == PieceType.BISHOP) || (move.getPiece() == PieceType.QUEEN)) {
         clonedBishopsAndQueens.flip(move.from().bitIndex());
         clonedBishopsAndQueens.flip(move.to().bitIndex());
      }
      if (move.isPromotion()
            && (move.getPromotedPiece() == PieceType.QUEEN || move.getPromotedPiece() == PieceType.BISHOP)) {
         clonedBishopsAndQueens.flip(move.to().bitIndex());
      }
   }

   /**
    * Helper method to check for a discovered check using a freely definable set of pieces and empty squares.
    * Should normally not be called directly, see instead
    * {@link Chessboard#checkForDiscoveredCheck(Chessboard, Move, Colour, Square)}.
    *
    * @param myPieces
    *           my pieces on the board
    * @param emptySquares
    *           the empty squares
    * @param kingsSquare
    *           where the (opponent's) king is
    * @param justCheckForDiscoveredCheck
    *           if true, will only check for discovered check. (Knight and Pawn checks are not considered.)
    *           If false, will check checks from all piece types.
    * @return true if a king on the 'kingsSquare' would be in check with this configuration of pieces and empty squares
    */
   public static boolean isKingInCheck(Map<PieceType, Piece> myPieces, BitSet allRooksAndQueens,
         BitSet allBishopsAndQueens, BitSet emptySquares, Square kingsSquare, boolean justCheckForDiscoveredCheck) {
      // discovered check can only be from a rook, queen, or bishop
      boolean isCheck = false;

      if (!allRooksAndQueens.isEmpty()) {
         isCheck = SlidingPiece.attacksSquareOnRankOrFile(allRooksAndQueens, emptySquares, kingsSquare);
      }

      // check bishops/queens if not already found check
      if (!isCheck && !allBishopsAndQueens.isEmpty()) {
         isCheck = SlidingPiece.attacksSquareOnDiagonal(allBishopsAndQueens, emptySquares, kingsSquare);
      }

      // for discovered check not important, but this method is also used in general to see if a king is in check
      if (!justCheckForDiscoveredCheck && !isCheck) {
         for (PieceType pieceType : new PieceType[] { PieceType.KNIGHT, PieceType.PAWN }) {
            if (!isCheck) {
               Piece piece = myPieces.get(pieceType);
               if (piece != null) {
                  isCheck = piece.attacksSquare(null, kingsSquare);
               }
            }
         }
      }

      return isCheck;
   }

   /**
    * Finds the piece at the given square.
    * TODO optimize using Lookup?
    *
    * @param targetSquare
    *           square to use
    * @return the piece at this location.
    * @throws IllegalArgumentException
    *            if no piece exists at the given square.
    * @deprecated should be possible to always rewrite using {@link #pieceAt(Square, Colour)}.
    */
   @Deprecated
   public PieceType pieceAt(Square targetSquare) {
      return pieceAt(targetSquare, null);
   }

   /**
    * Finds the piece at the given square.
    * TODO optimize using Lookup?
    *
    * @param targetSquare
    *           square to use
    * @param colour
    *           if not null, this piece's colour is expected.
    * @return the piece at this location.
    * @throws IllegalArgumentException
    *            if no piece [of the given colour] exists at the given square.
    */
   public PieceType pieceAt(Square targetSquare, Colour expectedColour) {
      for (Colour colour : Colour.values()) {
         if ((expectedColour != null) && (colour != expectedColour)) {
            continue;
         }
         for (PieceType type : PieceType.values()) {
            Piece p = getPieces(colour).get(type);
            // null == piece-type no longer on board
            if (p != null) {
               if (p.pieceAt(targetSquare)) {
                  return type;
               }
            }
         }
      }
      throw new IllegalArgumentException("no piece at " + targetSquare);
   }
}
