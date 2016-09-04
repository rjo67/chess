package org.rjo.chess2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.rjo.chess.Colour;
import org.rjo.chess.Move;
import org.rjo.chess.Square;
import org.rjo.chess.pieces.Bishop;
import org.rjo.chess.pieces.King;
import org.rjo.chess.pieces.Knight;
import org.rjo.chess.pieces.Pawn;
import org.rjo.chess.pieces.Piece;
import org.rjo.chess.pieces.PieceType;
import org.rjo.chess.pieces.Queen;
import org.rjo.chess.pieces.Rook;

/**
 * an immutable object which stores the board position after a particular move.
 *
 * @author rich
 * @since 2016-09-04
 */
public class Position {

	/**
	 * Indicates an enpassant square; can be null.
	 */
	private Square enpassantSquare;

	/**
	 * Stores the pieces in the game. The dimension indicates the colour {white,
	 * black}.
	 */
	private Map<PieceType, Piece>[] pieces;

	public static Position startPosition() {
		Position p = new Position();
		return p;
	}

	public Position() {
		@SuppressWarnings("unchecked")
		Set<Piece>[] pieces = new HashSet[Colour.values().length];
		for (Colour col : Colour.values()) {
			pieces[col.ordinal()] = new HashSet<Piece>(Arrays.asList(new Pawn(col, true), new Rook(col, true),
					new Knight(col, true), new Bishop(col, true), new Queen(col, true), new King(col, true)));
		}
		initBoard(pieces[Colour.WHITE.ordinal()], pieces[Colour.BLACK.ordinal()]);
	}

	// copy constructor
	@SuppressWarnings("unchecked")
	public Position(Position posn) {
		pieces = new HashMap[Colour.values().length];
		for (Colour colour : Colour.values()) {
			int ordinal = colour.ordinal();
			pieces[ordinal] = new HashMap<>();
			for (Piece p : posn.pieces[ordinal].values()) {
				pieces[ordinal].put(p.getType(), p);
			}
		}

		enpassantSquare = posn.enpassantSquare;
	}

	/**
	 * calculates the new position after the given move.
	 * 
	 * @param move
	 *            the move
	 * @return the new position
	 */
	public Position calculateNewPosition(Move move) {
		Position newPosn = new Position(this);

		PieceType movingPiece = move.getPiece();
		Colour sideToMove = move.getColour();
		// piece must be made immutable
		try {
			Piece piece = (Piece) pieces[sideToMove.ordinal()].get(movingPiece).clone();
			piece.move(move);
			newPosn.pieces[sideToMove.ordinal()].put(movingPiece, piece);
			return newPosn;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("piece not cloneable?", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void initBoard(Set<Piece> whitePieces, Set<Piece> blackPieces) {
		pieces = new HashMap[Colour.values().length];
		pieces[Colour.WHITE.ordinal()] = new HashMap<>();
		for (Piece p : whitePieces) {
			pieces[Colour.WHITE.ordinal()].put(p.getType(), p);
		}
		pieces[Colour.BLACK.ordinal()] = new HashMap<>();
		for (Piece p : blackPieces) {
			pieces[Colour.BLACK.ordinal()].put(p.getType(), p);
		}

		enpassantSquare = null;
	}

	public Square getEnpassantSquare() {
		return enpassantSquare;
	}

	@Override
	public String toString() {
		String[][] board = new String[8][8];

		// init
		for (int rank = 7; rank >= 0; rank--) {
			for (int file = 0; file < 8; file++) {
				board[rank][file] = ".";
			}
		}
		for (Colour colour : Colour.values()) {
			for (Piece p : pieces[colour.ordinal()].values()) {
				Square[] locations = p.getLocations();
				for (Square locn : locations) {
					board[locn.rank()][locn.file()] = p.getFenSymbol();
				}
			}
		}

		StringBuilder sb = new StringBuilder(64 + 8);
		for (int rank = 7; rank >= 0; rank--) {
			for (int file = 0; file < 8; file++) {
				sb.append(board[rank][file]);
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
