package chess;

import java.util.HashSet;
import java.util.Set;

import chess.pieces.Piece;
import chess.pieces.Pieces;

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

   public Chessboard() {
      pieces = new HashSet[Colour.values().length];
      allPieces = new BitBoard[Colour.values().length];
      // fill the board
      for (Colour side : Colour.values()) {
         allPieces[side.ordinal()] = new BitBoard();
         Set<Piece> set = new HashSet<>();
         for (Pieces piece : Pieces.values()) {
            Piece p = piece.getPieceImpl(side);
            set.add(p);
            allPieces[side.ordinal()].getBitSet().or(p.getBitBoard().getBitSet());
         }
         pieces[side.ordinal()] = set;
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
