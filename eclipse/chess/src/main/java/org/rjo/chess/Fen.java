package org.rjo.chess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.rjo.chess.pieces.Bishop;
import org.rjo.chess.pieces.King;
import org.rjo.chess.pieces.Knight;
import org.rjo.chess.pieces.Pawn;
import org.rjo.chess.pieces.Piece;
import org.rjo.chess.pieces.PieceType;
import org.rjo.chess.pieces.Queen;
import org.rjo.chess.pieces.Rook;

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
    * Creates a FEN notation for the given game.
    * 
    * @param game
    *           state of the game
    * @return a FEN string
    */
   public static String encode(Game game) {
      char[][] board = new char[8][];
      for (int rank = 0; rank < 8; rank++) {
         board[rank] = new char[8];
         for (int file = 0; file < 8; file++) {
            board[rank][file] = ' ';
         }
      }
      for (PieceType pieceType : game.getChessboard().getPieces(Colour.WHITE).keySet()) {
         Piece piece = game.getChessboard().getPieces(Colour.WHITE).get(pieceType);
         for (Square sq : piece.getLocations()) {
            board[sq.rank()][sq.file()] = piece.getFenSymbol().toCharArray()[0];
         }
      }
      for (PieceType pieceType : game.getChessboard().getPieces(Colour.BLACK).keySet()) {
         Piece piece = game.getChessboard().getPieces(Colour.BLACK).get(pieceType);
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

      fen.append(" ").append(addActiveColour(game));
      fen.append(" ").append(addCastlingRights(game));
      fen.append(" ").append(addEnpassantSquare(game));
      fen.append(" ").append(addHalfmoveClock(game));
      fen.append(" ").append(addFullmoveNumber(game));

      return fen.toString();
   }

   /**
    * Parses a FEN notation to create a game state.
    * 
    * @param fen
    *           a FEN representation of a chess position
    * @return a Game object
    */
   public static Game decode(String fen) {

      StringTokenizer fenTokenizer = new StringTokenizer(fen, " ");
      if (fenTokenizer.countTokens() != 6) {
         throw new IllegalArgumentException("invalid FEN string: expected 6 fields (space-separated) in input '" + fen
               + "'");
      }

      Chessboard cb = parsePosition(fenTokenizer.nextToken());
      Game game = new Game(cb);
      parseActiveColour(game, fenTokenizer.nextToken());
      parseCastlingRights(game, fenTokenizer.nextToken());
      parseEnpassantSquare(game, fenTokenizer.nextToken());
      parseHalfmoveClock(game, fenTokenizer.nextToken());
      parseFullmoveNumber(game, fenTokenizer.nextToken());

      return game;
   }

   /**
    * Active color. "w" means White moves next, "b" means Black.
    * 
    * @param game
    *           game state
    * @param token
    *           token repesenting the active colour
    */
   private static void parseActiveColour(Game game, String token) {
      if (token.length() != 1) {
         throw new IllegalArgumentException("Invalid FEN string: expected 1 char for field 2: active colour");
      }
      switch (token) {
      case "w":
         game.setSideToMove(Colour.WHITE);
         break;
      case "b":
         game.setSideToMove(Colour.BLACK);
         break;
      default:
         throw new IllegalArgumentException("Invalid FEN string: expected w/b for field 2: active colour");
      }
   }

   private static String addActiveColour(Game game) {
      return game.getSideToMove() == Colour.WHITE ? "w" : "b";
   }

   /**
    * Castling availability. If neither side can castle, this is "-". Otherwise, this has one or more letters: "K"
    * (White can castle kingside), "Q" (White can castle queenside), "k" (Black can castle kingside), and/or "q" (Black
    * can castle queenside).
    * 
    * @param game
    *           game state
    * @param token
    *           token representing castling rights
    */
   private static void parseCastlingRights(Game game, String token) {

      List<CastlingRights> whiteRights = new ArrayList<>(2);
      List<CastlingRights> blackRights = new ArrayList<>(2);
      if (token.contains("K")) {
         whiteRights.add(CastlingRights.KINGS_SIDE);
      }
      if (token.contains("Q")) {
         whiteRights.add(CastlingRights.QUEENS_SIDE);
      }
      if (token.contains("k")) {
         blackRights.add(CastlingRights.KINGS_SIDE);
      }
      if (token.contains("q")) {
         blackRights.add(CastlingRights.QUEENS_SIDE);
      }
      game.setCastlingRights(Colour.WHITE, whiteRights.toArray(new CastlingRights[2]));
      game.setCastlingRights(Colour.BLACK, blackRights.toArray(new CastlingRights[2]));
   }

   private static String addCastlingRights(Game game) {
      StringBuilder sb = new StringBuilder(4);
      if (game.canCastle(Colour.WHITE, CastlingRights.KINGS_SIDE)) {
         sb.append('K');
      }
      if (game.canCastle(Colour.WHITE, CastlingRights.QUEENS_SIDE)) {
         sb.append('Q');
      }
      if (game.canCastle(Colour.BLACK, CastlingRights.KINGS_SIDE)) {
         sb.append('k');
      }
      if (game.canCastle(Colour.BLACK, CastlingRights.QUEENS_SIDE)) {
         sb.append('q');
      }
      if (sb.length() == 0) {
         return "-";
      } else {
         return sb.toString();
      }
   }

   /**
    * En passant target square in algebraic notation. If there's no en passant target square, this is "-". If a pawn
    * has just made a two-square move, this is the position "behind" the pawn. This is recorded regardless of whether
    * there is a pawn in position to make an en passant capture.
    * 
    * @param game
    * 
    * @param token
    */
   private static void parseEnpassantSquare(Game game, String token) {
      if (!token.equals("-")) {
         game.getChessboard().setEnpassantSquare(Square.fromString(token));
      }
   }

   private static String addEnpassantSquare(Game game) {
      Square sq = game.getChessboard().getEnpassantSquare();
      if (sq == null) {
         return "-";
      } else {
         return sq.toString();
      }
   }

   /**
    * Halfmove clock: This is the number of halfmoves since the last capture or pawn advance. This is used to
    * determine if a draw can be claimed under the fifty-move rule.
    * 
    * @param game
    * 
    * @param token
    */
   private static void parseHalfmoveClock(Game game, String token) {
      try {
         Integer halfmoves = Integer.parseInt(token);
      } catch (NumberFormatException x) {
         throw new IllegalArgumentException("Invalid FEN string: expected a number for field 5: halfmove clock");
      }
      // TODO store halfmoves
   }

   private static String addHalfmoveClock(Game game) {
      // TODO Auto-generated method stub
      return "0";
   }

   /**
    * Fullmove number: The number of the full move. It starts at 1, and is incremented after Black's move.
    * 
    * @param game
    * 
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

   private static Chessboard parsePosition(String fen) {
      // this array is used to reference the FEN symbols for all the pieces
      // and to store the parsed positions (at the end of the routine)
      Piece[] allPieces = new Piece[] { new Pawn(Colour.WHITE), new Pawn(Colour.BLACK), new Rook(Colour.WHITE),
            new Rook(Colour.BLACK), new Knight(Colour.WHITE), new Knight(Colour.BLACK), new Bishop(Colour.WHITE),
            new Bishop(Colour.BLACK), new Queen(Colour.WHITE), new Queen(Colour.BLACK), new King(Colour.WHITE),
            new King(Colour.BLACK) };
      // this map stores the piece locations that get parsed from the FEN string
      Map<Piece, List<Square>> pieceMap = new HashMap<>();
      for (Piece piece : allPieces) {
         pieceMap.put(piece, new ArrayList<>());
      }
      StringTokenizer st = new StringTokenizer(fen, "/");
      if (st.countTokens() != 8) {
         throw new IllegalArgumentException("invalid FEN string: expected 8 delimiters in input '" + fen + "'");
      }
      int rankNr = 8;
      while (st.hasMoreTokens()) {
         String rank = st.nextToken();
         rankNr--;
         // bitposn of file 1 on this rank
         int bitPosn = 8 * rankNr;
         for (int i = 0; i < rank.length(); i++) {
            char ch = rank.charAt(i);
            // handle spaces
            if ((ch > '0') && (ch <= '8')) {
               bitPosn += (ch - '0');
            } else {
               // find appropriate piece
               Piece foundPiece = null;
               for (Piece piece : allPieces) {
                  if (ch == piece.getFenSymbol().charAt(0)) {
                     foundPiece = piece;
                     break;
                  }
               }
               if (foundPiece == null) {
                  throw new IllegalArgumentException("invalid FEN string: symbol '" + ch
                        + "' not allowed. Full string: '" + fen + "'");
               }
               // add to piece map
               List<Square> list = pieceMap.get(foundPiece);
               list.add(Square.fromBitPosn(bitPosn));
               pieceMap.put(foundPiece, list);

               bitPosn++;
            }
            // safety check
            if (bitPosn > ((8 * rankNr) + 8)) {
               throw new RuntimeException("parse exception, fen: '" + fen + "', bitPosn: " + bitPosn + ", rankNr: "
                     + rankNr + ", current rank: " + rank);
            }
         }
         // at the end of the rank, the bitPosn must be correct (+8 since one is always added)
         if (bitPosn != ((8 * rankNr) + 8)) {
            throw new IllegalArgumentException("invalid FEN string: rank '" + (rankNr + 1)
                  + "' not completely specified: '" + rank + "'. Bitposn: " + bitPosn + ". Full string: '" + fen + "'");
         }
      }

      // now init the pieces with the squares that have been parsed (from the map pieceMap)
      Set<Piece>[] pieces = new HashSet[Colour.values().length];
      for (Colour colour : Colour.values()) {
         pieces[colour.ordinal()] = new HashSet<>();
      }
      for (Piece piece : allPieces) {
         // check if there are any pieces at all of this type
         if (pieceMap.get(piece).size() != 0) {
            piece.initPosition(pieceMap.get(piece).toArray(new Square[1]));
            pieces[piece.getColour().ordinal()].add(piece);
         }
      }
      return new Chessboard(pieces[Colour.WHITE.ordinal()], pieces[Colour.BLACK.ordinal()]);
   }

}
