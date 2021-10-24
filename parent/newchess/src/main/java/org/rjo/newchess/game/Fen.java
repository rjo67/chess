package org.rjo.newchess.game;

import java.util.StringTokenizer;

import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

/**
 * Implementation of the Forsyth-Edwards Notation to record a game's position.
 * https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation.
 * <p>
 * A FEN record contains six fields. The separator between fields is a space. The fields are:
 * <ul>
 * <li>Piece placement (from white's perspective). Each rank is described, starting with rank 8 and ending with rank 1;
 * within each rank, the contents of each square are described from file "a" through file "h". Each piece is identified
 * by a single letter taken from the standard English names (pawn = "P", knight = "N", bishop = "B", rook = "R", queen =
 * "Q" and king = "K"). White pieces are designated using upper-case letters ("PNBRQK") while black pieces use lowercase
 * ("pnbrqk"). Empty squares are noted using digits 1 through 8 (the number of empty squares), and "/" separates
 * ranks.</li>
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
    * Creates a FEN notation for the given game.
    *
    * @param  game state of the game
    * @return      a FEN string
    */
   public static String encode(Game game) {

      String fen = encode(game.getPosition()) + " " + addHalfmoveClock(game) + " " + addFullmoveNumber(game);
      return fen;
   }

   /**
    * Creates a FEN notation for the given position. NB: not a complete FEN string, since information about the move nbr /
    * halfmove clock is only available from the <code>Game</code> object.
    *
    * @param  posn state of the position
    * @return      a FEN string
    */
   public static String encode(Position posn) {
      // fen notation starts at rank 8 and works down; this fits with our board representation (a8==0)
      StringBuilder fen = new StringBuilder(100);
      for (int rank = 7; rank >= 0; rank--) {
         int emptySquares = 0;
         for (int file = 0; file < 8; file++) {
            int sq = ((7 - rank) * 8) + file;
            if (posn.isEmpty(sq)) {
               emptySquares++;
            } else {
               // print empty squares if any
               if (emptySquares != 0) {
                  fen.append(emptySquares);
                  emptySquares = 0;
               }
               fen.append(posn.pieceAt(sq).fenSymbol(posn.colourOfPieceAt(sq)));
            }
         }
         if (emptySquares != 0) { fen.append(emptySquares); }
         if (rank != 0) { fen.append("/"); }
      }

      fen.append(" ").append(addActiveColour(posn));
      fen.append(" ").append(addCastlingRights(posn));
      fen.append(" ").append(addEnpassantSquare(posn));

      return fen.toString();
   }

   /**
    * Parses a FEN notation to create a game state. Either 6 fields or 4 if the halfmove clock and full move nbr fields are
    * left out.
    * <p>
    * Whether the king is in check will be stored.
    *
    * @param  fen a FEN representation of a chess position
    * @return     a Game object, containing a Position
    */
   public static Game decode(String fenStr) {

      StringTokenizer fenTokenizer = new StringTokenizer(fenStr, " ");
      if (fenTokenizer.countTokens() < 4) {
         throw new IllegalArgumentException("invalid FEN string: expected at least 4 fields (space-separated) in input '" + fenStr + "'");
      }

      String fen = fenTokenizer.nextToken();
      Colour sideToMove = parseActiveColour(fenTokenizer.nextToken());
      boolean[][] castlingRights = parseCastlingRights(fenTokenizer.nextToken());
      Square enpassantSquare = parseEnpassantSquare(fenTokenizer.nextToken());
      Position posn = parsePosition(fen, sideToMove, castlingRights, enpassantSquare);
      posn.setKingInCheck(posn.isKingInCheck(posn.getKingsSquare(sideToMove), sideToMove, -1));

      Game game = new Game(posn);
      if (fenTokenizer.hasMoreTokens()) {
         if (fenTokenizer.countTokens() != 2) {
            throw new IllegalArgumentException("invalid FEN string: expected 6 fields (space-separated) in input '" + fen + "'");
         }
         parseHalfmoveClock(game, fenTokenizer.nextToken());
         parseFullmoveNumber(game, fenTokenizer.nextToken());
      } else {
         game.setHalfmoveClock(1);
         game.setMoveNumber(1);
      }

      return game;
   }

   /**
    * Active color. "w" means White moves next, "b" means Black.
    *
    * @param  token token repesenting the active colour
    * @return       colour of the side to move
    */
   private static Colour parseActiveColour(String token) {
      if (token.length() != 1) { throw new IllegalArgumentException("Invalid FEN string: expected 1 char for field 2: active colour"); }
      switch (token) {
      case "w":
         return Colour.WHITE;
      case "b":
         return Colour.BLACK;
      default:
         throw new IllegalArgumentException("Invalid FEN string: expected w/b for field 2: active colour");
      }
   }

   private static String addActiveColour(Position posn) {
      return posn.getSideToMove() == Colour.WHITE ? "w" : "b";
   }

   /**
    * Castling availability. If neither side can castle, this is "-". Otherwise, this has one or more letters: "K" (White
    * can castle kingside), "Q" (White can castle queenside), "k" (Black can castle kingside), and/or "q" (Black can castle
    * queenside).
    *
    * @param  token token representing castling rights
    * @return       castlingrights array keyed by colour and kings/queensside
    */
   private static boolean[][] parseCastlingRights(String token) {

      boolean[][] castlingRights = new boolean[2][2];

      if (token.contains("K")) { castlingRights[Colour.WHITE.ordinal()][0] = true; }
      if (token.contains("Q")) { castlingRights[Colour.WHITE.ordinal()][1] = true; }
      if (token.contains("k")) { castlingRights[Colour.BLACK.ordinal()][0] = true; }
      if (token.contains("q")) { castlingRights[Colour.BLACK.ordinal()][1] = true; }
      return castlingRights;
   }

   private static String addCastlingRights(Position posn) {
      StringBuilder sb = new StringBuilder(4);
      if (posn.canCastleKingsside(Colour.WHITE)) { sb.append('K'); }
      if (posn.canCastleQueensside(Colour.WHITE)) { sb.append('Q'); }
      if (posn.canCastleKingsside(Colour.BLACK)) { sb.append('k'); }
      if (posn.canCastleQueensside(Colour.BLACK)) { sb.append('q'); }
      if (sb.length() == 0) {
         return "-";
      } else {
         return sb.toString();
      }
   }

   /**
    * En passant target square in algebraic notation. If there's no en passant target square, this is "-". If a pawn has
    * just made a two-square move, this is the position "behind" the pawn. This is recorded regardless of whether there is
    * a pawn in position to make an en passant capture.
    *
    * @param  token parsed token
    * @return       enpassant square, or null
    */
   private static Square parseEnpassantSquare(String token) {
      if (!token.equals("-")) {
         return Square.valueOf(token);
      } else {
         return null;
      }
   }

   private static String addEnpassantSquare(Position posn) {
      Square sq = posn.getEnpassantSquare();
      if (sq == null) {
         return "-";
      } else {
         return sq.toString();
      }
   }

   /**
    * Halfmove clock: This is the number of halfmoves since the last capture or pawn advance. This is used to determine if
    * a draw can be claimed under the fifty-move rule.
    *
    * @param game
    * @param token
    */
   private static void parseHalfmoveClock(Game game, String token) {
      try {
         Integer halfmoves = Integer.parseInt(token);
         game.setHalfmoveClock(halfmoves);
      } catch (NumberFormatException x) {
         throw new IllegalArgumentException("Invalid FEN string: expected a number for field 5: halfmove clock");
      }
   }

   private static String addHalfmoveClock(Game game) {
      return "" + game.getHalfmoveClock();
   }

   /**
    * Fullmove number: The number of the full move. It starts at 1, and is incremented after Black's move.
    *
    * @param game
    * @param token
    */
   private static void parseFullmoveNumber(Game game, String token) {
      try {
         Integer fullmoves = Integer.parseInt(token);
         game.setMoveNumber(fullmoves);
      } catch (NumberFormatException x) {
         throw new IllegalArgumentException("Invalid FEN string: expected a number for field 6: fullmove clock");
      }
   }

   private static String addFullmoveNumber(Game game) {
      return "" + game.getMoveNumber();
   }

   private static Position parsePosition(String fen, Colour sideToMove, boolean[][] castlingRights, Square enpassantSquare) {
      StringTokenizer st = new StringTokenizer(fen, "/");
      if (st.countTokens() != 8) { throw new IllegalArgumentException("invalid FEN string: expected 8 delimiters in input '" + fen + "'"); }

      Position posn = new Position();
      int rankNr = 8;
      while (st.hasMoreTokens()) {
         String rankStr = st.nextToken();
         rankNr--;
         // index of file 1 on this rank
         int index = Square.fromRankAndFile(rankNr, 0).index();
         for (int i = 0; i < rankStr.length(); i++) {
            char ch = rankStr.charAt(i);
            // handle spaces
            if (ch > '0' && ch <= '8') {
               index += (ch - '0');
            } else {
               // find appropriate piece
               PieceType pieceType = null;
               Colour pieceColour = null;
               for (PieceType piece : PieceType.values()) {
                  if (ch == piece.fenSymbol(Colour.WHITE).charAt(0)) {
                     pieceType = piece;
                     pieceColour = Colour.WHITE;
                     break;
                  } else if (ch == piece.fenSymbol(Colour.BLACK).charAt(0)) {
                     pieceType = piece;
                     pieceColour = Colour.BLACK;
                     break;
                  }
               }
               if (pieceType == null) {
                  throw new IllegalArgumentException("invalid FEN string: symbol '" + ch + "' not recognised. Full string: '" + fen + "'");
               }
               // add to position
               posn.addPiece(pieceColour, pieceType, index);

               index++;
            }
            // safety check
            if (index > ((7 - rankNr) * 8) + 8) {
               throw new RuntimeException("parse exception, fen: '" + fen + "', index: " + index + ", rankNr: " + rankNr + ", current rank: " + rankStr);
            }
         }
         // at the end of the rank, the index must be correct (+8 since one is always added)
         if (index != ((7 - rankNr) * 8) + 8) {
            throw new IllegalArgumentException("invalid FEN string: rank '" + (rankNr + 1) + "' not completely specified: '" + rankStr + "'. Index: " + index
                  + ". Full string: '" + fen + "'");
         }
      }

      posn.setSideToMove(sideToMove);
      posn.setCastlingRights(castlingRights);
      posn.setEnpassantSquare(enpassantSquare);

      return posn;
   }

}
