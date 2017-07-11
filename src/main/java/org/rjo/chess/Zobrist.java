package org.rjo.chess;

import java.util.Random;

import org.rjo.chess.pieces.Piece;
import org.rjo.chess.pieces.PieceType;

/**
 * Functions concerning the Zobrist hash of a position. http://chessprogramming.wikispaces.com/Zobrist+Hashing.
 * <p>
 * At program initialization, we generate an array of pseudorandom numbers
 * <ul>
 * <li>One number for each piece at each square</li>
 * <li>One number to indicate the side to move is black</li>
 * <li>Four numbers to indicate the castling rights, though usually 16 (2^4) are used for speed</li>
 * <li>Eight numbers to indicate the file of a valid En passant square, if any</li>
 * </ul>
 *
 * @author rich
 * @since 2017-07-11
 */
public class Zobrist {

	/** the random number generator for all the numbers required by this class */
	private Random randomGenerator;

	/**
	 * For each piece type, random numbers which will be used if a piece is present at the appropriate square:<BR>
	 * squareValues[WHITE][ROOK][square.bitIndex()]<BR>
	 * a0 == 0 == bottom left, h8 == 63 == top right
	 */
	private long[][][] squareValues = new long[Colour.ALL_COLOURS.length][PieceType.ALL_PIECE_TYPES.length][Square.values().length];

	/** random number which will be used if it is black to move */
	private long blackToMove;

	/**
	 * random numbers which are used depending on the castling rights.<BR>
	 * castlingValues[0][0] == white short-castle<BR>
	 * castlingValies[1][1] == black long-castle
	 */
	private long[][] castlingValues = new long[Colour.ALL_COLOURS.length][4];

	/**
	 * random numbers which are used depending on the file of the enpassant square:<BR>
	 * enpassantValues[enpassantSquare.file()]
	 */
	private long[] enpassantValues = new long[8];

	/**
	 * Creates a new class with a 'randomly' seeded random number generator.
	 */
	public Zobrist() {
		this(new Random());
	}

	/**
	 * Creates a new class with the random number generator seeded with the given seed.
	 *
	 * @param seed the random number seed
	 */
	public Zobrist(long seed) {
		this(new Random(seed));
	}

	/**
	 * Creates a new class with the given random number generator.
	 *
	 * @param randomGenerator the random number generator.
	 */
	private Zobrist(Random randomGenerator) {
		this.randomGenerator = randomGenerator;

		for (int colour = 0; colour < Colour.values().length; colour++) {
			for (int piece = 0; piece < PieceType.ALL_PIECE_TYPES.length; piece++) {
				for (int square = 0; square < Square.values().length; square++) {
					squareValues[colour][piece][square] = this.randomGenerator.nextLong();
				}
			}
		}
		blackToMove = this.randomGenerator.nextLong();
		for (int colour = 0; colour < Colour.values().length; colour++) {
			for (int i = 0; i < castlingValues.length; i++) {
				castlingValues[colour][i] = this.randomGenerator.nextLong();
			}
		}
		for (int i = 0; i < enpassantValues.length; i++) {
			enpassantValues[i] = this.randomGenerator.nextLong();
		}
	}

	/**
	 * Generates the Zobrist hash for a given position.
	 * <p>
	 * We initialize the hash key by xoring all random numbers linked to the given position. E.g the starting position:<BR>
	 * [Hash for White Rook on a1] xor [White Knight on b1] xor [White Bishop on c1] xor ... ( all pieces ) ... xor [White
	 * castling short] xor [White castling long] xor ...
	 *
	 * @param posn the position
	 * @return the hash
	 */
	public long hash(
			Position posn) {

		long hash = 0;

		for (Colour colour : Colour.ALL_COLOURS) {
			Piece[] pieces = posn.getPieces(colour);
			for (Piece piece : pieces) {
				Square[] locations = piece.getLocations();
				for (Square square : locations) {
					hash ^= squareValues[colour.ordinal()][piece.getType().ordinal()][square.bitIndex()];
				}
			}
		}
		if (posn.getSideToMove() == Colour.BLACK) {
			hash ^= blackToMove;
		}
		for (Colour colour : Colour.ALL_COLOURS) {
			for (CastlingRights rights : CastlingRights.values()) {
				if (posn.canCastle(colour, rights)) {
					hash ^= castlingValues[colour.ordinal()][rights.ordinal()];
				}
			}
		}
		if (posn.getEnpassantSquare() != null) {
			hash ^= enpassantValues[posn.getEnpassantSquare().file()];
		}

		return hash;
	}

	/**
	 * Returns a new Zobrist hash after the move 'move'. The fact that the xor-operation is own inverse and can be undone by
	 * using the same xor-operation again, allows a fast incremental update of the hash key.
	 *
	 * @param hash the zobrist hash
	 * @param move the move
	 * @return the updated hash
	 */
	public long update(
			long hash,
			Move move) {

		// TODO also check for castling

		// xor original hash with move.from()
		PieceType movingPiece = move.getPiece();
		Colour movingColour = move.getColour();
		hash ^= squareValues[movingColour.ordinal()][movingPiece.ordinal()][move.from().bitIndex()];
		if (move.isCapture()) {
			// xor with captured piece at move.to()
			PieceType capturedPiece = move.getCapturedPiece();
			hash ^= squareValues[Colour.oppositeColour(movingColour).ordinal()][capturedPiece.ordinal()][move.to().bitIndex()];
		}
		// xor with piece moving to move.to()
		hash ^= squareValues[movingColour.ordinal()][movingPiece.ordinal()][move.to().bitIndex()];
		// xor for black-to-move
		if (movingColour == Colour.WHITE) {
			hash ^= blackToMove;
		}
		// enpassant if pawn move to 4th rank from 2nd rank
		if ((move.getPiece() == PieceType.PAWN) && (move.from().rank() == 1) && (move.to().rank() == 3)) {
			hash ^= enpassantValues[move.to().file()];
		}

		return hash;
	}

}
