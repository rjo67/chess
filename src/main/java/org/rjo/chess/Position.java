package org.rjo.chess;

import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rjo.chess.pieces.Bishop;
import org.rjo.chess.pieces.King;
import org.rjo.chess.pieces.Knight;
import org.rjo.chess.pieces.Pawn;
import org.rjo.chess.pieces.Piece;
import org.rjo.chess.pieces.PieceType;
import org.rjo.chess.pieces.Queen;
import org.rjo.chess.pieces.Rook;
import org.rjo.chess.ray.Ray;
import org.rjo.chess.ray.RayInfo;
import org.rjo.chess.ray.RayUtils;

/**
 * A (soon-to-be) immutable object which stores the board position after a
 * particular move.
 * 
 * @author rich
 * @since 2016-09-04
 *
 */
public class Position {

	private static final Logger LOG = LogManager.getLogger(Position.class);

	/**
	 * Stores the pieces in the game. The dimension indicates the colour {white,
	 * black}.
	 */
	private Map<PieceType, Piece>[] pieces;

	/**
	 * bitboard of all pieces for a particular colour. The dimension indicates
	 * the colour {white, black}.
	 */
	private BitBoard[] allEnemyPieces;

	/**
	 * bitboard of all pieces on the board (irrespective of colour).
	 */
	private BitBoard totalPieces;

	/**
	 * bitboard of all empty squares on the board. Logical NOT of
	 * {@link #totalPieces}.
	 */
	private BitBoard emptySquares;

	/** Indicates an enpassant square; can be null. */
	private Square enpassantSquare;

	/** which sides can still castle */
	private EnumSet<CastlingRights>[] castling;

	/** half-moves. Not used as yet. */
	private int halfmoveClock;

	/** which side is to move */
	private Colour sideToMove;

	public static Position startPosition() {
		Position p = new Position();
		return p;
	}

	/**
	 * Constructs a new position, pieces are initialised to the start position.
	 */
	public Position() {
		// default piece positions
		@SuppressWarnings("unchecked")
		Set<Piece>[] pieces = new HashSet[Colour.values().length];
		for (Colour col : Colour.values()) {
			pieces[col.ordinal()] = new HashSet<Piece>(Arrays.asList(new Pawn(col, true), new Rook(col, true),
					new Knight(col, true), new Bishop(col, true), new Queen(col, true), new King(col, true)));
		}
		// default castling rights
		EnumSet<CastlingRights> whiteCastlingRights = EnumSet.of(CastlingRights.KINGS_SIDE, CastlingRights.QUEENS_SIDE);
		EnumSet<CastlingRights> blackCastlingRights = EnumSet.of(CastlingRights.KINGS_SIDE, CastlingRights.QUEENS_SIDE);

		setupInternal(pieces[Colour.WHITE.ordinal()], pieces[Colour.BLACK.ordinal()], whiteCastlingRights,
				blackCastlingRights);
	}

	/**
	 * Creates a chessboard with the given piece settings. Castling rights are
	 * set to 'none'.
	 */
	public Position(Set<Piece> whitePieces, Set<Piece> blackPieces) {
		setupInternal(whitePieces, blackPieces, EnumSet.noneOf(CastlingRights.class),
				EnumSet.noneOf(CastlingRights.class));
	}

	/**
	 * Creates a chessboard with the given piece settings and castling rights.
	 */
	public Position(Set<Piece> whitePieces, Set<Piece> blackPieces, EnumSet<CastlingRights> whiteCastlingRights,
			EnumSet<CastlingRights> blackCastlingRights) {
		setupInternal(whitePieces, blackPieces, whiteCastlingRights, blackCastlingRights);
	}

	// Creates a chessboard with the given piece settings
	private void setupInternal(Set<Piece> whitePieces, Set<Piece> blackPieces,
			EnumSet<CastlingRights> whiteCastlingRights, EnumSet<CastlingRights> blackCastlingRights) {
		initBoard(whitePieces, blackPieces);
		castling = new EnumSet[Colour.values().length];
		castling[Colour.WHITE.ordinal()] = whiteCastlingRights;
		castling[Colour.BLACK.ordinal()] = blackCastlingRights;
		sideToMove = Colour.WHITE;
	}

	/**
	 * copy constructor
	 */
	@SuppressWarnings("unchecked")
	public Position(final Position posn) {
		pieces = new HashMap[Colour.values().length];
		for (Colour colour : Colour.values()) {
			int ordinal = colour.ordinal();
			pieces[ordinal] = new HashMap<>();
			for (Piece p : posn.pieces[ordinal].values()) {
				pieces[ordinal].put(p.getType(), p);
			}
		}

		totalPieces = new BitBoard(posn.totalPieces.cloneBitSet());
		emptySquares = new BitBoard(posn.emptySquares.cloneBitSet());
		enpassantSquare = posn.enpassantSquare;
		sideToMove = posn.sideToMove;
		castling[0] = posn.castling[0].clone();
		castling[1] = posn.castling[1].clone();
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

	public boolean canCastle(Colour colour, CastlingRights rights) {
		return castling[colour.ordinal()].contains(rights);
	}

	public void setCastlingRights(Colour colour, CastlingRights... rights) {
		castling[colour.ordinal()].clear();
		for (CastlingRights right : rights) {
			if (right != null) {
				castling[colour.ordinal()].add(right);
			}
		}
	}

	public int getHalfmoveClock() {
		return halfmoveClock;
	}

	public void setHalfmoveClock(int halfmoveClock) {
		this.halfmoveClock = halfmoveClock;
	}

	public Colour getSideToMove() {
		return sideToMove;
	}

	public void setSideToMove(Colour sideToMove) {
		this.sideToMove = sideToMove;
	}

	/**
	 * Sets up all pieces and related data structures corresponding to the input
	 * parameters.
	 * 
	 * @param whitePieces
	 *            layout of the white pieces
	 * @param blackPieces
	 *            layout of the black pieces
	 */
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
		allEnemyPieces = new BitBoard[Colour.values().length];
		for (Colour colour : Colour.values()) {
			allEnemyPieces[colour.ordinal()] = new BitBoard();
			for (PieceType p : pieces[colour.ordinal()].keySet()) {
				allEnemyPieces[colour.ordinal()].getBitSet()
						.or(pieces[colour.ordinal()].get(p).getBitBoard().getBitSet());
			}
		}
		totalPieces = new BitBoard();
		totalPieces.getBitSet().or(allEnemyPieces[Colour.WHITE.ordinal()].getBitSet());
		totalPieces.getBitSet().or(allEnemyPieces[Colour.BLACK.ordinal()].getBitSet());
		emptySquares = new BitBoard(totalPieces.cloneBitSet());
		emptySquares.getBitSet().flip(0, 64);

		enpassantSquare = null;
	}

	/**
	 * update the internal structures (after a move/unmove). Incremental update
	 * for non-capture moves.
	 *
	 * @param move
	 *            the move
	 */
	public void updateStructures(Move move) {
		// @formatter:off
		// (f=flip)
		// White-Move non-capture capture
		// d3-d4 Ra3-a4 d3xe4 d7xc8=Q Ra4xRa8 d5xc6 e.p.
		// allPieces W f f f f f f f f f f f f
		// allR+Q W f f f f f
		// allB+Q W f
		// allPieces B f f f f f f f f(c7)
		// allR+Q B (when capt. f
		// allB+Q B piece!=RBQ)
		// totalPieces f f f f f f f f f f(c7)
		// emptySquares f f f f f f f f f f(c7)
		//
		// @formatter:on

		final int colourOrdinal = move.getColour().ordinal();
		final int oppositeColourOrdinal = Colour.oppositeColour(move.getColour()).ordinal();
		final int moveFromBitIndex = move.from().bitIndex();
		final int moveToBitIndex = move.to().bitIndex();

		// update incrementally
		if (!move.isCapture()) {
			updateBitSet(allEnemyPieces[colourOrdinal].getBitSet(), move);
			updateBitSet(totalPieces.getBitSet(), move);
			updateBitSet(emptySquares.getBitSet(), move);
		} else {
			// capture move
			if (!move.isEnpassant()) {
				allEnemyPieces[colourOrdinal].getBitSet().flip(moveFromBitIndex);
				allEnemyPieces[colourOrdinal].getBitSet().flip(moveToBitIndex);
				allEnemyPieces[oppositeColourOrdinal].getBitSet().flip(moveToBitIndex);
				totalPieces.getBitSet().flip(moveFromBitIndex);
				emptySquares.getBitSet().flip(moveFromBitIndex);

			} else {
				// enpassant
				int enpassantSquareBitIndex = Square.findMoveFromEnpassantSquare(move.to()).bitIndex();
				allEnemyPieces[colourOrdinal].getBitSet().flip(moveFromBitIndex);
				allEnemyPieces[colourOrdinal].getBitSet().flip(moveToBitIndex);
				allEnemyPieces[oppositeColourOrdinal].getBitSet().flip(enpassantSquareBitIndex);
				totalPieces.getBitSet().flip(moveFromBitIndex);
				totalPieces.getBitSet().flip(moveToBitIndex);
				totalPieces.getBitSet().flip(enpassantSquareBitIndex);
				emptySquares.getBitSet().flip(moveFromBitIndex);
				emptySquares.getBitSet().flip(moveToBitIndex);
				emptySquares.getBitSet().flip(enpassantSquareBitIndex);
			}
		}
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

	private BitBoard updateBishopsAndQueens(Map<PieceType, Piece> pieces) {
		Piece queens = pieces.get(PieceType.QUEEN);
		BitSet queensBitSet;
		if (queens == null) {
			queensBitSet = new BitSet(64);
		} else {
			queensBitSet = queens.getBitBoard().getBitSet();
		}

		Piece bishops = pieces.get(PieceType.BISHOP);
		BitSet bishopsAndQueens;
		if (bishops == null) {
			bishopsAndQueens = new BitSet(64);
		} else {
			bishopsAndQueens = bishops.getBitBoard().cloneBitSet();
		}
		bishopsAndQueens.or(queensBitSet);
		return new BitBoard(bishopsAndQueens);
	}

	private BitBoard updateRooksAndQueens(Map<PieceType, Piece> pieces) {
		Piece queens = pieces.get(PieceType.QUEEN);
		BitSet queensBitSet;
		if (queens == null) {
			queensBitSet = new BitSet(64);
		} else {
			queensBitSet = queens.getBitBoard().getBitSet();
		}

		Piece rooks = pieces.get(PieceType.ROOK);
		BitSet rooksAndQueens;
		if (rooks == null) {
			rooksAndQueens = new BitSet(64);
		} else {
			rooksAndQueens = rooks.getBitBoard().cloneBitSet();
		}
		rooksAndQueens.or(queensBitSet);
		return new BitBoard(rooksAndQueens);
	}

	/**
	 * Updates the given bitset to represent the move. The from and to squares
	 * will be flipped. If castling then the rook's move is also taken into a/c.
	 *
	 * @param bitset
	 *            the bitset to be updated.
	 * @param move
	 *            the move. NB only non-capture moves are supported by this
	 *            method!
	 */
	private void updateBitSet(BitSet bitset, Move move) {
		bitset.flip(move.from().bitIndex());
		bitset.flip(move.to().bitIndex());
		if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
			bitset.flip(move.getRooksCastlingMove().from().bitIndex());
			bitset.flip(move.getRooksCastlingMove().to().bitIndex());
		}
	}

	/**
	 * Access to the set of pieces of a given colour.
	 *
	 * @param colour
	 *            the required colour
	 * @return the set of pieces of this colour
	 */
	public Map<PieceType, Piece> getPieces(Colour colour) {
		return pieces[colour.ordinal()];
	}

	/**
	 * Access to a BitBoard of all the pieces of a given colour.
	 *
	 * @param colour
	 *            the required colour
	 * @return a BitBoard containing all the pieces of a given colour.
	 */
	public BitBoard getAllPieces(Colour colour) {
		return allEnemyPieces[colour.ordinal()];
	}

	/**
	 * Access to a BitBoard of all the pieces irrespective of colour.
	 *
	 * @return a BitBoard containing all the pieces irrespective of colour.
	 */
	public BitBoard getTotalPieces() {
		return totalPieces;
	}

	/**
	 * Access to a BitBoard of all the empty squares on the board.
	 *
	 * @return a BitBoard containing all the empty squares on the board.
	 */
	public BitBoard getEmptySquares() {
		return emptySquares;
	}

	public void debug() {
		for (Colour colour : Colour.values()) {
			System.out.println(colour + " all pieces");
			System.out.println(allEnemyPieces[colour.ordinal()].display());
			System.out.println("---");
		}
		System.out.println("pieces");
		for (Colour colour : Colour.values()) {
			for (PieceType p : pieces[colour.ordinal()].keySet()) {
				System.out.println(p + ", " + colour);
				System.out.println(pieces[colour.ordinal()].get(p).getBitBoard().display());
				System.out.println("---");
			}
		}
		System.out.println("totalPieces");
		System.out.println(totalPieces.display());
		System.out.println("---");
		System.out.println("emptySquares");
		System.out.println(emptySquares.display());
		System.out.println("---");

	}

	public void setEnpassantSquare(Square enpassantSquare) {
		this.enpassantSquare = enpassantSquare;
	}

	/**
	 * The enpassant square.
	 *
	 * @return the enpassant square or null.
	 */
	public Square getEnpassantSquare() {
		return enpassantSquare;
	}

	/**
	 * Returns true if the given square is attacked by any opponent's pieces.
	 *
	 * @param game
	 *            the game
	 * @param targetSquare
	 *            the square to consider
	 * @param opponentsColour
	 *            the colour of the opponent
	 * @return true if this square is attacked by the opponent
	 */
	public boolean squareIsAttacked(Game game, Square targetSquare, Colour opponentsColour) {
		Map<PieceType, Piece> opponentsPieces = getPieces(opponentsColour);
		// iterate over the pieces
		// TODO instead of treating queens separately, should 'merge' them with
		// the rooks and the bishops
		for (PieceType type : PieceType.ALL_PIECE_TYPES) {
			Piece piece = opponentsPieces.get(type);
			if (piece != null) {
				if (piece.attacksSquare(game.getChessboard().getEmptySquares().getBitSet(), targetSquare)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks for a discovered check after the move 'move'.
	 * <p>
	 * This will not be 100% correct for moves along the same ray to the
	 * opponent's king. But these moves are already check and not discovered
	 * check.
	 *
	 * @param chessboard
	 *            the chessboard
	 * @param move
	 *            the move
	 * @param colour
	 *            which side is moving
	 * @param opponentsKing
	 *            where the opponent's king is
	 * @return true if this move leads to a discovered check
	 */
	public static boolean checkForDiscoveredCheck(Position chessboard, Move move, Colour colour, Square opponentsKing) {
		final int moveFromIndex = move.from().bitIndex();

		// optimization (see RayUtils.discoveredCheck)
		if (null == RayUtils.getRay(opponentsKing, move.from())) {
			return false;
		}

		// set up the emptySquares and myPieces bitsets *after* this move
		BitSet emptySquares = chessboard.getEmptySquares().cloneBitSet();
		BitSet myPieces = chessboard.getAllPieces(colour).cloneBitSet();

		emptySquares.set(moveFromIndex);
		myPieces.clear(moveFromIndex);

		// 1) do not need to set 'move.to()' -- if we're moving on the same ray,
		// then it will be check already
		// 2) can't get a discovered check from castling

		return RayUtils.discoveredCheck(colour, chessboard, emptySquares, myPieces, opponentsKing, move.from());
	}

	/**
	 * Returns true if a piece on 'startSquare' attacks 'targetSquare', i.e. the
	 * two squares are on the same ray and there are no intervening pieces.
	 * <p>
	 * It still depends on the piece type to determine whether there really is
	 * an attack.
	 *
	 * @param emptySquares
	 *            bitset of empty Squares
	 * @param myPieces
	 *            bitset of my pieces
	 * @param myColour
	 *            my colour
	 * @param startSquare
	 *            start square
	 * @param targetSquare
	 *            target square
	 * @return true if a piece on 'startSquare' attacks 'targetSquare'
	 */
	public static boolean checkIfPieceOnSquare1CouldAttackSquare2(BitSet emptySquares, BitSet myPieces, Colour myColour,
			Square startSquare, Square targetSquare) {
		Ray ray = RayUtils.getRay(startSquare, targetSquare);
		if (ray != null) {
			RayInfo info = RayUtils.findFirstPieceOnRay(myColour, emptySquares, myPieces, ray, startSquare.bitIndex());
			int targetSquareIndex = targetSquare.bitIndex();
			if (info.foundPiece() && info.getIndexOfPiece() == targetSquareIndex) {
				return true;
			}
			for (int sq : info.getEmptySquares()) {
				if (sq == targetSquareIndex) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if my king is in check after my move, i.e. the piece that moved
	 * was actually pinned.
	 *
	 * @param chessboard
	 *            the chessboard
	 * @param move
	 *            the move
	 * @param colour
	 *            which side is moving
	 * @param myKing
	 *            where my king is
	 * @return true if this move is illegal since the piece that moved was
	 *         pinned
	 */
	public static boolean checkForPinnedPiece(Position chessboard, Move move, Colour colour, Square myKing) {
		// set up the bitsets *after* this move
		BitSet emptySquares = chessboard.getEmptySquares().cloneBitSet();
		BitSet myPieces = chessboard.getAllPieces(colour).cloneBitSet();

		emptySquares.set(move.from().bitIndex());
		emptySquares.clear(move.to().bitIndex());
		if (move.isEnpassant()) {
			emptySquares.set(Square.findMoveFromEnpassantSquare(move.to()).bitIndex());
		}
		myPieces.clear(move.from().bitIndex());
		myPieces.set(move.to().bitIndex());

		return RayUtils.kingInCheck(colour, chessboard, emptySquares, myPieces, myKing, move.from());
	}

	/**
	 * Checks if my king is in check after the move 'move'.
	 *
	 * @param chessboard
	 *            the chessboard
	 * @param move
	 *            the move
	 * @param opponentsColour
	 *            this colour's pieces will be inspected to see if they check my
	 *            king
	 * @param king
	 *            where my king is
	 * @param kingIsAlreadyInCheck
	 *            true if the king was already in check before the 'move'
	 * @return true if this move leaves the king in check (i.e. is an illegal
	 *         move)
	 */
	public static boolean isKingInCheck(Position chessboard, Move move, Colour opponentsColour, Square king,
			boolean kingIsAlreadyInCheck) {

		// short circuit if king was not in check beforehand (therefore only
		// need to check for a pinned piece) and the
		// moving piece's original square is not on a ray to the king
		if ((move.getPiece() != PieceType.KING) && !kingIsAlreadyInCheck) {
			if (null == RayUtils.getRay(king, move.from())) {
				return false;
			}
		}

		BitSet friendlyPieces = chessboard.getAllPieces(Colour.oppositeColour(opponentsColour)).getBitSet();
		Map<PieceType, BitSet> enemyPieces = setupEnemyBitsets(chessboard.getPieces(opponentsColour));

		if (kingIsAlreadyInCheck) {
			return KingCheck.isKingInCheckAfterMove_PreviouslyWasInCheck(king, Colour.oppositeColour(opponentsColour),
					friendlyPieces, enemyPieces, move);
		} else {
			return KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(king, Colour.oppositeColour(opponentsColour),
					friendlyPieces, enemyPieces, move);
		}
	}

	private static Map<PieceType, BitSet> setupEnemyBitsets(Map<PieceType, Piece> map) {
		Map<PieceType, BitSet> enemyPieces = new HashMap<>();
		for (PieceType type : PieceType.ALL_PIECE_TYPES) {
			enemyPieces.put(type, map.get(type).getBitBoard().getBitSet());
		}
		return enemyPieces;
	}

	/**
	 * Finds the piece at the given square.
	 *
	 * @param targetSquare
	 *            square to use
	 * @return the piece at this location.
	 * @throws IllegalArgumentException
	 *             if no piece exists at the given square.
	 * @deprecated should be possible to always rewrite using
	 *             {@link #pieceAt(Square, Colour)}.
	 */
	@Deprecated
	public PieceType pieceAt(Square targetSquare) {
		return pieceAt(targetSquare, null);
	}

	/**
	 * Finds the piece at the given square. TODO optimize using Lookup?
	 *
	 * @param targetSquare
	 *            square to use
	 * @param colour
	 *            if not null, this piece's colour is expected.
	 * @return the piece at this location.
	 * @throws IllegalArgumentException
	 *             if no piece [of the given colour] exists at the given square.
	 */
	public PieceType pieceAt(Square targetSquare, Colour expectedColour) {
		for (Colour colour : Colour.values()) {
			if ((expectedColour != null) && (colour != expectedColour)) {
				continue;
			}
			for (PieceType type : PieceType.getPieceTypes()) {
				Piece p = getPieces(colour).get(type);
				// null == piece-type no longer on board
				if ((p != null) && (p.pieceAt(targetSquare))) {
					return type;
				}
			}
		}
		if (expectedColour != null) {
			throw new IllegalArgumentException("no " + expectedColour + " piece at " + targetSquare);
		} else {
			throw new IllegalArgumentException("no piece at " + targetSquare);
		}
	}

}
