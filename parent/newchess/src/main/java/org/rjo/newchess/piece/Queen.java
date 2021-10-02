package org.rjo.newchess.piece;

import java.util.ArrayList;
import java.util.List;

import org.rjo.newchess.board.Board.Square;

public class Queen implements Piece {

   private final static int[] MOVE_OFFSETS = { -11, -10, -9, -1, 1, 9, 10, 11 };

   private Colour colour;
   private List<Integer> positions;

   public Queen(Colour colour) {
      this(colour, new Integer[] {});
   }

   public Queen(Colour colour, Integer... positions) {
      this.colour = colour;
      this.positions = new ArrayList<>();
      for (int posn : positions) {
         this.positions.add(posn);
      }
   }

   public Queen(Colour colour, Square... squares) {
      this(colour, convert(squares));
   }

   private static Integer[] convert(Square[] squares) {
      List<Integer> posns = new ArrayList<>();
      for (Square sq : squares) {
         posns.add(sq.index());
      }
      return posns.toArray(new Integer[0]);
   }

   @Override
   public Colour getColour() {
      return colour;
   }

   @Override
   public List<Integer> getPositions() {
      return positions;
   }

   @Override
   public int[] getMoveOffsets() {
      return MOVE_OFFSETS;
   }

   @Override
   public void add(int position) {
      positions.add(position);
   }

   @Override
   public boolean isSlidingPiece() {
      return true;
   }

   @Override
   public String name() {
      return colour == Colour.WHITE ? "Q" : "q";
   }

   @Override
   public int type() {
      return PieceType.QUEEN.ordinal();
   }
}
