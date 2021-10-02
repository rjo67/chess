package org.rjo.newchess.game;

import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Piece;
import org.rjo.newchess.piece.PieceType;

public class Game {

   /** all pieces, keyed by colour and piece-type */
   private Piece[][] pieces;

   private Position posn;

   private Game() {
      posn = new Position();
   }

   private void addPiece(Colour colour, PieceType pieceType, int square) {
      posn.addPiece(colour, pieceType, square);

   }

   public static Game create() {
      Game g = new Game();

      g.pieces = new Piece[2][PieceType.DIFFERENT_PIECE_TYPES];

      // white pieces
      g.posn.addPiece(Colour.WHITE, PieceType.QUEEN, Square.d1);

      // black pieces
      g.posn.addPiece(Colour.BLACK, PieceType.QUEEN, Square.d8);

      return g;
   }

   public Piece[] getAllPieces(Colour colour) {
      return pieces[colour.ordinal()];
   }

   public Piece getQueenInfo(Colour colour) {
      return pieces[colour.ordinal()][PieceType.QUEEN.ordinal()];
   }

   public Position getPosition() {
      return posn;
   }
}
