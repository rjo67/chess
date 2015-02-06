package chess;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Piece;
import chess.pieces.Queen;
import chess.pieces.Rook;

public class Chessboard {

   /**
    * Stores the pieces in the game.
    * The dimension indicates the colour {white, black}.
    */
   private Set<Piece>[] pieces;

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
      initBoard(pieces[Colour.White.ordinal()], pieces[Colour.Black.ordinal()]);
   }

   /**
    * Creates a chessboard with the given piece settings.
    */
   public Chessboard(Set<Piece> whitePieces, Set<Piece> blackPieces) {
      initBoard(whitePieces, blackPieces);
   }

   private void initBoard(Set<Piece> whitePieces, Set<Piece> blackPieces) {
      pieces = new HashSet[Colour.values().length];
      pieces[Colour.White.ordinal()] = whitePieces;
      pieces[Colour.Black.ordinal()] = blackPieces;
      allPieces = new BitBoard[Colour.values().length];
      // fill the board
      for (Colour colour : Colour.values()) {
         allPieces[colour.ordinal()] = new BitBoard();
         for (Piece p : pieces[colour.ordinal()]) {
            allPieces[colour.ordinal()].getBitSet().or(p.getBitBoard().getBitSet());
         }
      }
      totalPieces = new BitBoard();
      totalPieces.getBitSet().or(allPieces[Colour.White.ordinal()].getBitSet());
      totalPieces.getBitSet().or(allPieces[Colour.Black.ordinal()].getBitSet());

      emptySquares = new BitBoard(totalPieces.cloneBitSet());
      emptySquares.getBitSet().flip(0, 64);
   }

   public Set<Piece> getPieces(Colour colour) {
      return pieces[colour.ordinal()];
   }

   public BitBoard getAllPieces(Colour colour) {
      return allPieces[colour.ordinal()];
   }

   public BitBoard getTotalPieces() {
      return totalPieces;
   }

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
         for (Piece p : pieces[colour.ordinal()]) {
            System.out.println(p);
            System.out.println(p.getBitBoard().display());
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
}
