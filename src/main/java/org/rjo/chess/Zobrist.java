package org.rjo.chess;

import java.util.Random;

import org.rjo.chess.CastlingRightsSummary.CastlingRights;
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

	public static final Zobrist INSTANCE = new Zobrist();

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
	private long[][] castlingValues = new long[Colour.ALL_COLOURS.length][CastlingRights.values().length];

	/**
	 * random numbers which are used depending on the file of the enpassant square:<BR>
	 * enpassantValues[enpassantSquare.file()]
	 */
	private long[] enpassantValues = new long[8];

	/**
	 * Creates a new class with a 'randomly' seeded random number generator.
	 */
	private Zobrist() {
		this(new Random());
	}

	/**
	 * Creates a new class with the random number generator seeded with the given seed.
	 * <p>
	 * Mainly for tests -- see {@link #INSTANCE}.
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
	public long hash(Position posn) {

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
	 * Returns a new Zobrist hash after <code>move</code>. The fact that the xor-operation is own inverse and can be undone
	 * by using the same xor-operation again, allows a fast incremental update of the hash key.
	 *
	 * @param hash the zobrist hash
	 * @param move the move
	 * @param castling castling rights before <code>move</code>
	 * @param enpassantSquare enpassant square before <code>move</code>
	 * @return the updated hash
	 */
	public long update(long hash,
			Move move,
			CastlingRightsSummary[] castling,
			Square enpassantSquare) {

		// remove piece at move.from()
		PieceType movingPiece = move.getPiece();
		Colour sideToMove = move.getColour();
		hash ^= squareValues[sideToMove.ordinal()][movingPiece.ordinal()][move.from().bitIndex()];

		if (move.isCapture()) {
			// xor with captured piece at move.to()
			final PieceType capturedPiece = move.getCapturedPiece();
			final Colour opponentsColour = Colour.oppositeColour(sideToMove);
			if (move.isEnpassant()) {
				Square sq = Square.findMoveFromEnpassantSquare(move.to());
				hash ^= squareValues[opponentsColour.ordinal()][capturedPiece.ordinal()][sq.bitIndex()];
			} else {
				hash ^= squareValues[opponentsColour.ordinal()][capturedPiece.ordinal()][move.to().bitIndex()];
			}
			if (move.isPromotion()) {
				hash ^= squareValues[sideToMove.ordinal()][move.getPromotedPiece().ordinal()][move.to().bitIndex()];
			} else {
				hash ^= squareValues[sideToMove.ordinal()][movingPiece.ordinal()][move.to().bitIndex()];
			}

			// update OPPONENT's castling rights if necessary
			boolean processed = false;
			if (CastlingRightsSummary.opponentKingsSideCastlingRightsGoneAfterMove(castling[opponentsColour.ordinal()], sideToMove,
					move)) {
				hash ^= castlingValues[opponentsColour.ordinal()][CastlingRights.KINGS_SIDE.ordinal()];
				processed = true;
			}
			if (!processed) {
				if (CastlingRightsSummary.opponentQueensSideCastlingRightsGoneAfterMove(castling[opponentsColour.ordinal()],
						sideToMove, move)) {
					hash ^= castlingValues[opponentsColour.ordinal()][CastlingRights.QUEENS_SIDE.ordinal()];
				}
			}
		}
		// non-capture
		else {
			// add piece at move.to()
			if (move.isPromotion()) {
				hash ^= squareValues[sideToMove.ordinal()][move.getPromotedPiece().ordinal()][move.to().bitIndex()];
			} else {
				hash ^= squareValues[sideToMove.ordinal()][movingPiece.ordinal()][move.to().bitIndex()];
			}
		}

		// cater for rook's move if castling
		if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
			Move rooksMove = move.getRooksCastlingMove();
			hash ^= squareValues[sideToMove.ordinal()][PieceType.ROOK.ordinal()][rooksMove.from().bitIndex()];
			hash ^= squareValues[sideToMove.ordinal()][PieceType.ROOK.ordinal()][rooksMove.to().bitIndex()];

			// both king's and queen's side castling rights have now gone (if they were present)
			hash = rehashIfKingsSideCastlingPresent(hash, sideToMove, castling[sideToMove.ordinal()]);
			hash = rehashIfQueensSideCastlingPresent(hash, sideToMove, castling[sideToMove.ordinal()]);
		} else {
			// enpassant if pawn move to 4th rank from 2nd rank
			if (move.isPawnMoveTwoSquaresForward()) {
				hash ^= enpassantValues[move.to().file()];
			}
			// remove castling rights (if set) on king move
			else if (move.getPiece() == PieceType.KING) {
				hash = rehashIfKingsSideCastlingPresent(hash, sideToMove, castling[sideToMove.ordinal()]);
				hash = rehashIfQueensSideCastlingPresent(hash, sideToMove, castling[sideToMove.ordinal()]);
			}
			// remove castling rights (if set) on rook move
			else if (move.getPiece() == PieceType.ROOK) {
				if (CastlingRightsSummary.kingsSideCastlingRightsGoneAfterMove(castling[sideToMove.ordinal()], sideToMove, move)) {
					hash ^= castlingValues[sideToMove.ordinal()][CastlingRights.KINGS_SIDE.ordinal()];
				}
				if (CastlingRightsSummary.queensSideCastlingRightsGoneAfterMove(castling[sideToMove.ordinal()], sideToMove, move)) {
					hash ^= castlingValues[sideToMove.ordinal()][CastlingRights.QUEENS_SIDE.ordinal()];
				}
			}
		}

		// 'remove' enpassant square if one existed before move
		if (enpassantSquare != null) {
			hash ^= enpassantValues[enpassantSquare.file()];
		}

		// ALWAYS need to xor for black-to-move
		hash ^= blackToMove;

		return hash;
	}

	private long rehashIfKingsSideCastlingPresent(long hash,
			Colour sideToMove,
			CastlingRightsSummary castling) {
		if (castling.canCastleKingsSide()) {
			hash ^= castlingValues[sideToMove.ordinal()][CastlingRights.KINGS_SIDE.ordinal()];
		}
		return hash;
	}

	private long rehashIfQueensSideCastlingPresent(long hash,
			Colour sideToMove,
			CastlingRightsSummary castling) {
		if (castling.canCastleQueensSide()) {
			hash ^= castlingValues[sideToMove.ordinal()][CastlingRights.QUEENS_SIDE.ordinal()];
		}
		return hash;
	}

}
