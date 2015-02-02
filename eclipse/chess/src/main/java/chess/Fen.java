package chess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Piece;
import chess.pieces.Queen;
import chess.pieces.Rook;

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
      for (Piece piece : chessboard.getPieces(Colour.White)) {
         for (Square sq : piece.getLocations()) {
            board[sq.rank()][sq.file()] = piece.getFenSymbol().toCharArray()[0];
         }
      }
      for (Piece piece : chessboard.getPieces(Colour.Black)) {
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

      StringTokenizer fenTokenizer = new StringTokenizer(fen, " ");
      if (fenTokenizer.countTokens() != 6) {
         throw new IllegalArgumentException("invalid FEN string: expected 6 fields (space-separated) in input '" + fen
               + "'");
      }

      Chessboard cb = parsePosition(fenTokenizer.nextToken());
      parseActiveColour(fenTokenizer.nextToken());
      parseCastlingRights(fenTokenizer.nextToken());
      parseEnpassantSquare(fenTokenizer.nextToken());
      parseHalfmoveClock(fenTokenizer.nextToken());
      parseFullmoveNumber(fenTokenizer.nextToken());

      return cb;
   }

   /**
    * Active color. "w" means White moves next, "b" means Black.
    * 
    * @param token
    */
   private static void parseActiveColour(String token) {
      if (token.length() != 1) {
         throw new IllegalArgumentException("Invalid FEN string: expected 1 char for field 2: active colour");
      }
      switch (token) {
      case "w":
         break; // TODO
      case "b":
         break;// TODO
      default:
         throw new IllegalArgumentException("Invalid FEN string: expected w/b for field 2: active colour");
      }
   }

   /**
    * Castling availability. If neither side can castle, this is "-". Otherwise, this has one or more letters: "K"
    * (White can castle kingside), "Q" (White can castle queenside), "k" (Black can castle kingside), and/or "q" (Black
    * can castle queenside).
    * 
    * @param token
    */
   private static void parseCastlingRights(String token) {
      // TODO Auto-generated method stub

   }

   /**
    * En passant target square in algebraic notation. If there's no en passant target square, this is "-". If a pawn
    * has just made a two-square move, this is the position "behind" the pawn. This is recorded regardless of whether
    * there is a pawn in position to make an en passant capture.
    * 
    * @param token
    */
   private static void parseEnpassantSquare(String token) {
      // TODO Auto-generated method stub

   }

   /**
    * Halfmove clock: This is the number of halfmoves since the last capture or pawn advance. This is used to
    * determine if a draw can be claimed under the fifty-move rule.
    * 
    * @param token
    */
   private static void parseHalfmoveClock(String token) {
      try {
         Integer halfmoves = Integer.parseInt(token);
      } catch (NumberFormatException x) {
         throw new IllegalArgumentException("Invalid FEN string: expected a number for field 5: halfmove clock");
      }

   }

   /**
    * Fullmove number: The number of the full move. It starts at 1, and is incremented after Black's move.
    * 
    * @param token
    */
   private static void parseFullmoveNumber(String token) {
      try {
         Integer fullmoves = Integer.parseInt(token);
      } catch (NumberFormatException x) {
         throw new IllegalArgumentException("Invalid FEN string: expected a number for field 6: fullmove clock");
      }
   }

   private static Chessboard parsePosition(String fen) {
      // this array is used to reference the FEN symbols for all the pieces
      // and to store the parsed positions (at the end of the routine)
      Piece[] allPieces = new Piece[] { new Pawn(Colour.White), new Pawn(Colour.Black), new Rook(Colour.White),
            new Rook(Colour.Black), new Knight(Colour.White), new Knight(Colour.Black), new Bishop(Colour.White),
            new Bishop(Colour.Black), new Queen(Colour.White), new Queen(Colour.Black), new King(Colour.White),
            new King(Colour.Black) };
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
      return new Chessboard(pieces[Colour.White.ordinal()], pieces[Colour.Black.ordinal()]);
   }

}
