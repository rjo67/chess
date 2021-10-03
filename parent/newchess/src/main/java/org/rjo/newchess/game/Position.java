package org.rjo.newchess.game;

import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

public class Position {

   final static int BIT_7 = 0b01000000;
   final static int BIT_6 = 0b00100000;
   final static int COLOUR_MASK = 0b01100000;
   final static int PIECE_MASK = 0b00000111;

   /**
    * <ul>
    * <li>bits 6-7 - colour of occupying piece, or unoccupied, see Colour</li>
    * <li>bits 1-3 - identifies the piece, see PieceType.</li>
    * </ul>
    */
   int[] board;
   boolean[][] castlingRights; // 1st dimension: W/B, 2nd dimension: 0 - king's side, 1 - queen's side
   private Square enpassantSquare; // set if previous move was a pawn moving 2 squares forward

   public Position() {
      this(new boolean[2][2]);
   }

   public Position(boolean[][] castlingRights) {
      this.board = new int[64];
      for (int i = 0; i < 64; i++) {
         board[i] = BIT_6 * Colour.UNOCCUPIED.ordinal();
      }
      this.castlingRights = castlingRights;
   }

   public void addPiece(Colour colour, PieceType pieceType, int square) {
      int val = BIT_6 * colour.ordinal() + pieceType.ordinal();
      board[square] = val;
   }

   public void addPiece(Colour colour, PieceType pieceType, Square square) {
      this.addPiece(colour, pieceType, square.index());
   }

   public boolean isEmpty(int square) {
      return colourOfPieceAt(square) == Colour.UNOCCUPIED;
   }

   public Colour colourOfPieceAt(int square) {
      return decodeColour(board[square]);
   }

   public PieceType pieceAt(int square) {
      return decodePieceType(board[square]);
   }

   public boolean canCastleKingsside(Colour col) {
      return castlingRights[col.ordinal()][0];
   }

   public boolean canCastleQueensside(Colour col) {
      return castlingRights[col.ordinal()][1];
   }

   public void castleKingsside(Colour col) {
      this.castlingRights[col.ordinal()][0] = false;
   }

   public void castleQueensside(Colour col) {
      this.castlingRights[col.ordinal()][1] = false;
   }

   public void setEnpassantSquare(Square sq) {
      this.enpassantSquare = sq;
   }

   public Square getEnpassantSquare() {
      return enpassantSquare;
   }

   // delivers the 'raw' value of the square
   public int raw(int square) {
      return board[square];
   }

   public static Colour decodeColour(int squareValue) {
      int val = (squareValue & COLOUR_MASK) >> 5;
      return Colour.convert(val);
   }

   public static PieceType decodePieceType(int squareValue) {
      int val = squareValue & PIECE_MASK;
      return PieceType.convert(val);
   }

}
