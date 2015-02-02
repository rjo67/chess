package chess;

import chess.pieces.Piece;

/**
 * Implementation of the Forsyth–Edwards Notation to record a game's position.
 * https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation
 * 
 * 
 * A FEN record contains six fields. The separator between fields is a space. The fields are:
 * 
 * <ul>
 * <li>Piece placement (from white's perspective). Each rank is described, starting with rank 8 and ending with rank 1;
 * within each rank, the contents of each square are described from file "a" through file "h". Each piece is identified
 * by a single letter taken from the standard English names (pawn = "P", knight = "N", bishop = "B", rook = "R", queen =
 * "Q" and king = "K"). White pieces are designated using upper-case letters ("PNBRQK") while black pieces use lowercase
 * ("pnbrqk"). Empty squares are noted using digits 1 through 8 (the number of empty squares), and "/" separates ranks.</li>
 * <li>Active color. "w" means White moves next, "b" means Black.</li>
 * <li>Castling availability. If neither side can castle, this is "-". Otherwise, this has one or more letters: "K"
 * (White can castle kingside), "Q" (White can castle queenside), "k" (Black can castle kingside), and/or "q" (Black can
 * castle queenside).</li>
 * <li>En passant target square in algebraic notation. If there's no en passant target square, this is "-". If a pawn
 * has just made a two-square move, this is the position "behind" the pawn. This is recorded regardless of whether there
 * is a pawn in position to make an en passant capture.</li>
 * <li>Halfmove clock: This is the number of halfmoves since the last capture or pawn advance. This is used to determine
 * if a draw can be claimed under the fifty-move rule.</li>
 * <li>Fullmove number: The number of the full move. It starts at 1, and is incremented after Black's move.</li>
 * </ul>
 * 
 * @author rich
 */
public class Fen {

   /**
    * Creates a FEN notation for the given chessboard.
    * 
    * @param chessboard
    *           state of the game
    * @return a FEN string
    */
   public static String encode(Chessboard chessboard) {
      char[][] board = new char[8][];
      for (int rank = 0; rank < 8; rank++) {
         board[rank] = new char[8];
         for (int file = 0; file < 8; file++) {
            board[rank][file] = ' ';
         }
      }
      for (Piece piece : chessboard.getPieces(Colour.WHITE)) {
         for (Square sq : piece.getLocations()) {
            board[sq.rank()][sq.file()] = piece.getFenSymbol().toCharArray()[0];
         }
      }
      for (Piece piece : chessboard.getPieces(Colour.BLACK)) {
         for (Square sq : piece.getLocations()) {
            board[sq.rank()][sq.file()] = piece.getFenSymbol().toCharArray()[0];
         }
      }

      // fen notation starts at rank 8 and works down
      StringBuilder fen = new StringBuilder(100);
      for (int rank = 7; rank >= 0; rank--) {
         int emptySquares = 0;
         for (int file = 0; file < 8; file++) {
            char square = board[rank][file];
            if (square == ' ') {
               emptySquares++;
            } else {
               // print empty squares if any
               if (emptySquares != 0) {
                  fen.append(emptySquares);
                  emptySquares = 0;
               }
               fen.append(square);
            }
         }
         if (emptySquares != 0) {
            fen.append(emptySquares);
         }
         if (rank != 0) {
            fen.append("/");
         }
      }

      // TODO: Active color, Castling availability, En passant target square, Halfmove clock, Fullmove number

      return fen.toString();
   }

   /**
    * Parses a FEN notation to create a game state.
    * 
    * @param fen
    *           a FEN representation of a chess position
    * @return a Chessboard
    */
   public static Chessboard decode(String fen) {
      return null;
   }

}
