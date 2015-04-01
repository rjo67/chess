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
      updateStructures();

      enpassantSquare = null;
   }

   /**
    * update structures (after a move). Would be more efficient to incremently recalculate, see {@link Game#move(Move)}
    * and {@link Game#unmove(Move)}.
    */
   public void updateStructures() {
      for (Colour colour : Colour.values()) {
         allPieces[colour.ordinal()] = new BitBoard();
         for (PieceType p : pieces[colour.ordinal()].keySet()) {
            allPieces[colour.ordinal()].getBitSet().or(pieces[colour.ordinal()].get(p).getBitBoard().getBitSet());
         }
      }
      totalPieces = new BitBoard();
      totalPieces.getBitSet().or(allPieces[Colour.WHITE.ordinal()].getBitSet());
      totalPieces.getBitSet().or(allPieces[Colour.BLACK.ordinal()].getBitSet());

      emptySquares = new BitBoard(totalPieces.cloneBitSet());
      emptySquares.getBitSet().flip(0, 64);
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
   public static boolean checkForDiscoveredCheck(Chessboard chessboard, Move move, Colour colour, Square opponentsKing) {
      // set up the empty square bitset *after* this move
      BitSet emptySquares = chessboard.getEmptySquares().cloneBitSet();
      emptySquares.set(move.from().bitIndex());
      emptySquares.clear(move.to().bitIndex());
      // don't need to consider captures here, since we're looking for a check from OUR side

      // take care of rook's move when castling
      if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
         Move rooksMove = move.getRooksCastlingMove();
         emptySquares.set(rooksMove.from().bitIndex());
         emptySquares.clear(rooksMove.to().bitIndex());
      }
      return Chessboard.isKingInCheck(chessboard.getPieces(colour), emptySquares, opponentsKing);
   }

   /**
    * Checks if the king is in check after the move 'move'.
    * TODO NB does not cater for castling, since one can't castle out of check.
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
      Piece originalPiece = null;
      // TODO promotions
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
            }
            // achtung: changing the 'global' state here, need to reset later!
            opponentsPieces.put(move.getCapturedPiece(), clonedPiece);
         } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("cannot clone:", e);
         }
      }
      boolean inCheck = Chessboard.isKingInCheck(opponentsPieces, emptySquares, king);
      if (move.isCapture()) {
         // reset global state
         opponentsPieces.put(move.getCapturedPiece(), originalPiece);
      }
      return inCheck;

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
    * @return true if a king on the 'kingsSquare' would be in check with this configuration of pieces and empty squares
    */
   public static boolean isKingInCheck(Map<PieceType, Piece> myPieces, BitSet emptySquares, Square kingsSquare) {
      // discovered check can only be from a rook, queen, or bishop
      boolean isCheck = false;
      BitSet rooksAndQueens;
      BitSet queensBitSet;
      BitSet bishopsAndQueens;

      Piece queens = myPieces.get(PieceType.QUEEN);
      if (queens == null) {
         queensBitSet = new BitSet(64);
      } else {
         queensBitSet = queens.getBitBoard().getBitSet();
      }

      Piece rooks = myPieces.get(PieceType.ROOK);
      if (rooks == null) {
         rooksAndQueens = new BitSet(64);
      } else {
         rooksAndQueens = rooks.getBitBoard().cloneBitSet();
      }
      rooksAndQueens.or(queensBitSet);
      isCheck = SlidingPiece.attacksSquareOnRankOrFile(rooksAndQueens, emptySquares, kingsSquare);

      // check bishops/queens if not already found check
      if (!isCheck) {
         Piece bishops = myPieces.get(PieceType.BISHOP);
         if (bishops == null) {
            bishopsAndQueens = new BitSet(64);
         } else {
            bishopsAndQueens = bishops.getBitBoard().cloneBitSet();
         }
         bishopsAndQueens.or(queensBitSet);
         isCheck = SlidingPiece.attacksSquareOnDiagonal(bishopsAndQueens, emptySquares, kingsSquare);
      }

      // for discovered check not important, but this method is also used in general to see if a king is in check
      if (!isCheck) {
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
    *
    * @param targetSquare
    *           square to use
    * @return the piece at this location.
    * @throws IllegalArgumentException
    *            if no piece exists at the given square.
    */
   public PieceType pieceAt(Square targetSquare) {
      for (Colour colour : Colour.values()) {
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
