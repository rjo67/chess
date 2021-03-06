package org.rjo.chess.position;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.rjo.chess.base.Colour;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.CastlingRightsSummary.CastlingRights;
import org.rjo.chess.pieces.Bishop;
import org.rjo.chess.pieces.King;
import org.rjo.chess.pieces.Knight;
import org.rjo.chess.pieces.Pawn;
import org.rjo.chess.pieces.Piece;
import org.rjo.chess.pieces.Queen;
import org.rjo.chess.pieces.Rook;

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
	 * @param game state of the game
	 * @return a FEN string
	 */
	public static String encode(Game game) {

		String fen = encode(game.getPosition()) + " " + addHalfmoveClock(game) +
				" " + addFullmoveNumber(game);
		return fen;
	}

	/**
	 * Creates a FEN notation for the given position. NB: not a complete FEN string, since information about the move nbr /
	 * halfmove clock is only available from the <code>Game</code> object.
	 *
	 * @param posn state of the position
	 * @return a FEN string
	 */
	public static String encode(Position posn) {
		char[][] board = new char[8][];
		for (int rank = 0; rank < 8; rank++) {
			board[rank] = new char[8];
			for (int file = 0; file < 8; file++) {
				board[rank][file] = ' ';
			}
		}
		for (Colour colour : Colour.ALL_COLOURS) {
			for (PieceType pieceType : PieceType.ALL_PIECE_TYPES) {
				Piece piece = posn.getPieces(colour)[pieceType.ordinal()];
				for (Square sq : piece.getLocations()) {
					board[sq.rank()][sq.file()] = piece.getFenSymbol().toCharArray()[0];
				}
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
	 * @param fen a FEN representation of a chess position
	 * @return a Game object, containing a Position
	 */
	public static Game decode(String fen) {

		StringTokenizer fenTokenizer = new StringTokenizer(fen, " ");
		if (fenTokenizer.countTokens() < 4) {
			throw new IllegalArgumentException("invalid FEN string: expected at least 4 fields (space-separated) in input '" + fen + "'");
		}

		Position posn = parsePosition(fenTokenizer.nextToken(), parseActiveColour(fenTokenizer.nextToken()),
				parseCastlingRights(fenTokenizer.nextToken()), parseEnpassantSquare(fenTokenizer.nextToken()));
		boolean inCheck = posn.squareIsAttacked(posn.getKingPosition(posn.getSideToMove()), Colour.oppositeColour(posn.getSideToMove()));
		posn.setInCheck(inCheck);

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
	 * @param token token repesenting the active colour
	 * @return colour of the side to move
	 */
	private static Colour parseActiveColour(String token) {
		if (token.length() != 1) {
			throw new IllegalArgumentException("Invalid FEN string: expected 1 char for field 2: active colour");
		}
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
	 * @param token token representing castling rights
	 * @return castlingrights array
	 */
	private static EnumSet<CastlingRights>[] parseCastlingRights(String token) {

		@SuppressWarnings("unchecked")
		EnumSet<CastlingRights>[] rights = new EnumSet[Colour.ALL_COLOURS.length];
		rights[Colour.WHITE.ordinal()] = EnumSet.noneOf(CastlingRights.class);
		rights[Colour.BLACK.ordinal()] = EnumSet.noneOf(CastlingRights.class);

		if (token.contains("K")) {
			rights[Colour.WHITE.ordinal()].add(CastlingRights.KINGS_SIDE);
		}
		if (token.contains("Q")) {
			rights[Colour.WHITE.ordinal()].add(CastlingRights.QUEENS_SIDE);
		}
		if (token.contains("k")) {
			rights[Colour.BLACK.ordinal()].add(CastlingRights.KINGS_SIDE);
		}
		if (token.contains("q")) {
			rights[Colour.BLACK.ordinal()].add(CastlingRights.QUEENS_SIDE);
		}
		return rights;
	}

	private static String addCastlingRights(Position posn) {
		StringBuilder sb = new StringBuilder(4);
		if (posn.canCastle(Colour.WHITE, CastlingRights.KINGS_SIDE)) {
			sb.append('K');
		}
		if (posn.canCastle(Colour.WHITE, CastlingRights.QUEENS_SIDE)) {
			sb.append('Q');
		}
		if (posn.canCastle(Colour.BLACK, CastlingRights.KINGS_SIDE)) {
			sb.append('k');
		}
		if (posn.canCastle(Colour.BLACK, CastlingRights.QUEENS_SIDE)) {
			sb.append('q');
		}
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
	 * @param token parsed token
	 * @return enpassant square, or null
	 */
	private static Square parseEnpassantSquare(String token) {
		if (!token.equals("-")) {
			return Square.fromString(token);
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
	private static void parseHalfmoveClock(Game game,
			String token) {
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
	private static void parseFullmoveNumber(Game game,
			String token) {
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

	private static Position parsePosition(String fen,
			Colour sideToMove,
			EnumSet<CastlingRights>[] castlingRights,
			Square enpassantSquare) {
		// this array is used to reference the FEN symbols for all the pieces
		// and to store the parsed positions (at the end of the routine)
		Piece[] allPieces = new Piece[] { new Pawn(Colour.WHITE), new Pawn(Colour.BLACK), new Rook(Colour.WHITE), new Rook(Colour.BLACK),
				new Knight(Colour.WHITE), new Knight(Colour.BLACK), new Bishop(Colour.WHITE), new Bishop(Colour.BLACK), new Queen(Colour.WHITE),
				new Queen(Colour.BLACK), new King(Colour.WHITE), new King(Colour.BLACK) };
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
				if (ch > '0' && ch <= '8') {
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
						throw new IllegalArgumentException("invalid FEN string: symbol '" + ch + "' not recognised. Full string: '" + fen + "'");
					}
					// add to piece map
					List<Square> list = pieceMap.get(foundPiece);
					list.add(Square.fromBitIndex(bitPosn));
					pieceMap.put(foundPiece, list);

					bitPosn++;
				}
				// safety check
				if (bitPosn > (8 * rankNr) + 8) {
					throw new RuntimeException(
							"parse exception, fen: '" + fen + "', bitPosn: " + bitPosn + ", rankNr: " + rankNr + ", current rank: " + rank);
				}
			}
			// at the end of the rank, the bitPosn must be correct (+8 since one
			// is always added)
			if (bitPosn != (8 * rankNr) + 8) {
				throw new IllegalArgumentException("invalid FEN string: rank '" + (rankNr + 1) + "' not completely specified: '" + rank
						+ "'. Bitposn: " + bitPosn + ". Full string: '" + fen + "'");
			}
		}

		// now init the pieces with the squares that have been parsed (from the map pieceMap)
		// contains all piece types -- even if no pieces of this type exist on the board
		@SuppressWarnings("unchecked")
		Set<Piece>[] pieces = new HashSet[Colour.ALL_COLOURS.length];
		for (Colour colour : Colour.ALL_COLOURS) {
			pieces[colour.ordinal()] = new HashSet<>();
		}
		for (Piece piece : allPieces) {
			pieces[piece.getColour().ordinal()].add(piece);
			if (!pieceMap.get(piece).isEmpty()) {
				piece.initPosition(pieceMap.get(piece).toArray(new Square[1]));
				pieces[piece.getColour().ordinal()].add(piece);
			}
		}
		return new Position(pieces[Colour.WHITE.ordinal()], pieces[Colour.BLACK.ordinal()], sideToMove,
				castlingRights[Colour.WHITE.ordinal()], castlingRights[Colour.BLACK.ordinal()], enpassantSquare);
	}

}
