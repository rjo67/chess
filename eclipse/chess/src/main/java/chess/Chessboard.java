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
    * The dimension indicates the side {white, black}.
    */
   private Set<Piece>[] pieces;

   /**
    * bitboard of all pieces for a side.
    * The dimension indicates the side {white, black}.
    */
   private BitBoard[] allPieces;

   /**
    * Creates a chessboard with default piece settings.
    */
   public Chessboard() {
      Set<Piece>[] pieces = new HashSet[Colour.values().length];
      for (Colour col : Colour.values()) {
         pieces[col.ordinal()] = new HashSet<Piece>(Arrays.asList(new Pawn(col), new Rook(col), new Knight(col),
               new Bishop(col), new Queen(col), new King(col)));
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
      for (Colour side : Colour.values()) {
         allPieces[side.ordinal()] = new BitBoard();
         for (Piece p : pieces[side.ordinal()]) {
            allPieces[side.ordinal()].getBitSet().or(p.getBitBoard().getBitSet());
         }
      }
   }

   public Set<Piece> getPieces(Colour side) {
      return pieces[side.ordinal()];
   }

   public void debug() {
      for (Colour side : Colour.values()) {
         System.out.println(side + " all pieces");
         System.out.println(allPieces[side.ordinal()].display());
         System.out.println("---");
      }
      System.out.println("pieces");
      for (Colour side : Colour.values()) {
         for (Piece p : pieces[side.ordinal()]) {
            System.out.println(p);
            System.out.println(p.getBitBoard().display());
            System.out.println("---");
         }
      }

   }
}
