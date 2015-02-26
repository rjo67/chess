package org.rjo.chess;

import java.util.Arrays;
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
         pieces[col.ordinal()] = new HashSet<Piece>(Arrays.asList(new Pawn(col), new Rook(col), new Knight(col),
               new Bishop(col), new Queen(col), new King(col)));
         for (Piece piece : pieces[col.ordinal()]) {
            piece.initPosition();
         }
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
      // fill the board
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

      enpassantSquare = null;
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
}
