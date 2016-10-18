package org.rjo.chess;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

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
 * An immutable object which stores the board position after a particular move.<br>
 * Previously: Chessboard.java.
 * 
 * @author rich
 * @since 2016-09-04
 */
public class Position {

	private static final Logger LOG = LogManager.getLogger(Position.class);

	// thread pool for findMove()
	private ExecutorService threadPool = Executors.newFixedThreadPool(PieceType.getPieceTypes().length);

	/**
	 * Controls access to the pieces in the game.
	 */
	private PieceManager pieceMgr;

	/**
	 * bitboard of all pieces for a particular colour. The dimension indicates the colour {white,
	 * black}.
	 */
	private BitBoard[] allEnemyPieces;

	/**
	 * bitboard of all pieces on the board (irrespective of colour).
	 */
	private BitBoard totalPieces;

	/**
	 * bitboard of all empty squares on the board. Logical NOT of {@link #totalPieces}.
	 */
	private BitBoard emptySquares;

	/** Indicates an enpassant square; can be null. */
	private Square enpassantSquare;

	/** which sides can still castle */
	private EnumSet<CastlingRights>[] castling;

	/** which side is to move */
	private Colour sideToMove;

	/**
	 * if the king (of the sideToMove) is currently in check. Normally deduced from the last move but
	 * can be set delibarately for tests.
	 */
	private boolean inCheck;

	public static Position startPosition() {
		Position p = new Position();
		return p;
	}

	/**
	 * Constructs a new position with default starting positions and default castling rights.
	 */
	public Position() {
		// default piece positions
		this(new HashSet<Piece>(
				Arrays.asList(new Pawn(Colour.WHITE, true), new Rook(Colour.WHITE, true), new Knight(Colour.WHITE, true),
						new Bishop(Colour.WHITE, true), new Queen(Colour.WHITE, true), new King(Colour.WHITE, true))),
				new HashSet<Piece>(Arrays.asList(new Pawn(Colour.BLACK, true), new Rook(Colour.BLACK, true),
						new Knight(Colour.BLACK, true), new Bishop(Colour.BLACK, true), new Queen(Colour.BLACK, true),
						new King(Colour.BLACK, true))),
				// default castling rights
				EnumSet.of(CastlingRights.KINGS_SIDE, CastlingRights.QUEENS_SIDE),
				EnumSet.of(CastlingRights.KINGS_SIDE, CastlingRights.QUEENS_SIDE));
	}

	/**
	 * Creates a chessboard with the given piece settings. Castling rights are set to 'none'.
	 */
	public Position(Set<Piece> whitePieces, Set<Piece> blackPieces) {
		this(whitePieces, blackPieces, EnumSet.noneOf(CastlingRights.class), EnumSet.noneOf(CastlingRights.class));
	}

	/**
	 * Creates a chessboard with the given piece settings and castling rights.
	 */
	public Position(Set<Piece> whitePieces, Set<Piece> blackPieces, EnumSet<CastlingRights> whiteCastlingRights,
			EnumSet<CastlingRights> blackCastlingRights) {
		initBoard(whitePieces, blackPieces);
		castling = new EnumSet[Colour.values().length];
		castling[Colour.WHITE.ordinal()] = whiteCastlingRights;
		castling[Colour.BLACK.ordinal()] = blackCastlingRights;
		sideToMove = Colour.WHITE;
	}

	/**
	 * copy constructor
	 */
	public Position(final Position posn) {
		pieceMgr = new PieceManager();
		for (Colour colour : Colour.values()) {
			for (Piece p : posn.getPieces(colour).values()) {
				// references the same 'pieces' as before.
				// Need to clone iff these objects get changed
				pieceMgr.getPiecesForColour(colour).put(p.getType(), p);
			}
		}

		// TODO no need for cloning here!?

		totalPieces = new BitBoard(posn.totalPieces.cloneBitSet());
		emptySquares = new BitBoard(posn.emptySquares.cloneBitSet());

		allEnemyPieces = new BitBoard[Colour.values().length];

		for (Colour colour : Colour.values()) {
			// TODO: is it necessary to clone here?
			allEnemyPieces[colour.ordinal()] = new BitBoard(posn.allEnemyPieces[colour.ordinal()].cloneBitSet());
		}

		enpassantSquare = posn.enpassantSquare;
		sideToMove = posn.sideToMove;
		castling = new EnumSet[Colour.values().length];
		castling[0] = posn.castling[0].clone();
		castling[1] = posn.castling[1].clone();
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

	public Colour getSideToMove() {
		return sideToMove;
	}

	public void setSideToMove(Colour sideToMove) {
		this.sideToMove = sideToMove;
	}

	/**
	 * Sets up all pieces and related data structures corresponding to the input parameters.
	 * 
	 * @param whitePieces layout of the white pieces
	 * @param blackPieces layout of the black pieces
	 */
	private void initBoard(Set<Piece> whitePieces, Set<Piece> blackPieces) {
		this.pieceMgr = new PieceManager(whitePieces, blackPieces);
		allEnemyPieces = new BitBoard[Colour.values().length];
		for (Colour colour : Colour.values()) {
			allEnemyPieces[colour.ordinal()] = new BitBoard();
			for (PieceType p : pieceMgr.getPiecesForColour(colour).keySet()) {
				allEnemyPieces[colour.ordinal()].getBitSet().or(pieceMgr.getPiece(colour, p).getBitBoard().getBitSet());
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
	 * update the internal structures (after a move). Incremental update for non-capture moves.
	 *
	 * @param move the move
	 */
	// package private for tests
	void updateStructures(Move move) {
		// @formatter:off
      // (f=flip)
      // White-Move       non-capture      capture
      //                  d3-d4   Ra3-a4   d3xe4   d7xc8=Q  Ra4xRa8  d5xc6 e.p.
      // allPieces   W    f  f     f  f    f  f    f  f      f   f   f  f
      // allR+Q      W             f  f               f      f   f
      // allB+Q      W                                f
      // allPieces   B    f  f     f  f       f       f          f      f(c7)
      // allR+Q      B                    (when capt.            f
      // allB+Q      B                      piece!=RBQ)
      // totalPieces      f  f     f  f    f       f         f       f  f f(c7)
      // emptySquares     f  f     f  f    f       f         f       f  f f(c7)
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

	/**
	 * Find all moves for the given colour from the current position.
	 *
	 * @param colour the required colour
	 * @return all moves for this colour.
	 */
	public List<Move> findMoves(Colour colour) {
		// return findMovesParallel(colour);
		List<Move> moves = new ArrayList<>(60);
		for (PieceType type : PieceType.getPieceTypes()) {
			Piece p = getPieces(colour).get(type);
			moves.addAll(p.findMoves(this, inCheck));
		}
		return moves;
	}

	public List<Move> findMovesParallel(Colour colour) {
		// set up tasks
		List<Callable<List<Move>>> tasks = new ArrayList<>();
		for (PieceType type : PieceType.getPieceTypes()) {
			tasks.add(new Callable<List<Move>>() {

				@Override
				public List<Move> call() throws Exception {
					Piece p = getPieces(colour).get(type);
					return p.findMoves(Position.this, inCheck);
				}

			});
		}

		// and execute
		List<Move> moves = new ArrayList<>(60);
		try {
			List<Future<List<Move>>> results = threadPool.invokeAll(tasks);

			for (Future<List<Move>> f : results) {
				moves.addAll(f.get());
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("got InterruptedException from future (findMovesParallel)", e);
		} catch (ExecutionException e) {
			throw new RuntimeException("got ExecutionException from future (findMovesParallel)", e);
		}

		return moves;
	}

	private void writeDebug(Writer debugWriter, String string) {
		if (debugWriter != null) {
			try {
				debugWriter.write(string + System.lineSeparator());
			} catch (IOException e) {
				throw new RuntimeException("could not write debug info", e);
			}
		}
	}

	/**
	 * return the new position after the given move, without debug.
	 *
	 * @param move the move
	 * @return a new Position object with the position after the given move
	 */
	public Position move(Move move) {
		return move(move, null);
	}

	/**
	 * returns the new position after the given move.
	 *
	 * @param move the move
	 * @param debugWriter if not null, debug info will be written here
	 * @return a new Position object with the position after the given move
	 */
	public Position move(Move move, Writer debugWriter) {
		Position newPosn = new Position(this);
		newPosn.internalMove(move, debugWriter);
		return newPosn;
	}

	/**
	 * Performs the given move, updating internal data structures.
	 * 
	 * @param move the move
	 * @param debugWriter if not null, debug info will be written here
	 */
	private void internalMove(Move move, Writer debugWriter) {
		if (move.getColour() != sideToMove) {
			throw new IllegalArgumentException("move is for '" + move.getColour() + "' but sideToMove=" + sideToMove);
		}
		PieceType movingPiece = move.getPiece();

		if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
			Move rooksMove = move.getRooksCastlingMove();
			pieceMgr.getClonedPiece(sideToMove, movingPiece).move(move);
			pieceMgr.getClonedPiece(sideToMove, PieceType.ROOK).move(move);
			// castling rights are reset later on
		} else {
			if (!move.isCapture() && !getEmptySquares().getBitSet().get(move.to().bitIndex())) {
				throw new IllegalArgumentException("square " + move.to() + " is not empty. Move=" + move);
			}
			// update structures for the moving piece
			pieceMgr.getClonedPiece(sideToMove, movingPiece).move(move);
			// capture: remove the captured piece
			if (move.isCapture()) {
				if (move.isEnpassant()) {
					pieceMgr.getClonedPiece(Colour.oppositeColour(sideToMove), move.getCapturedPiece())
							.removePiece(Square.findMoveFromEnpassantSquare(move.to()));
				} else {
					pieceMgr.getClonedPiece(Colour.oppositeColour(sideToMove), move.getCapturedPiece())
							.removePiece(move.to());
				}
			}
			// promotion: add the promoted piece
			if (move.isPromotion()) {
				pieceMgr.getClonedPiece(sideToMove, move.getPromotedPiece()).addPiece(move.to());
			}
		}
		updateStructures(move);

		updateCastlingRightsAfterMove(move, debugWriter);
		if (move.isPawnMoveTwoSquaresForward()) {
			setEnpassantSquare(Square.findEnpassantSquareFromMove(move.to()));
		} else {
			setEnpassantSquare(null);
		}
		setSideToMove(Colour.oppositeColour(sideToMove));
		inCheck = move.isCheck();
	}

	/**
	 * Just for tests: indicate that in this position the king of the side to move is in check.
	 *
	 * @param inCheck true when the king is in check.
	 */
	public void setInCheck(boolean inCheck) {
		this.inCheck = inCheck;
	}

	public boolean isInCheck() {
		return inCheck;
	}

	/**
	 * Calculates a static value for the position after the given move.
	 *
	 * @param move the move
	 * @return a value in centipawns
	 */
	public int evaluate(Move move) {
		Position newPosn = move(move);
		return newPosn.evaluate();
	}

	/**
	 * Calculates a static value for the current position. In order for NegaMax to work, it is
	 * important to return the score relative to the side being evaluated.
	 *
	 * @return a value in centipawns
	 */
	public int evaluate() {
		/*
		 * materialScore = kingWt * (wK-bK) + queenWt * (wQ-bQ) + rookWt * (wR-bR) + knightWt* (wN-bN)
		 * + bishopWt* (wB-bB) + pawnWt * (wP-bP) mobilityScore = mobilityWt * (wMobility-bMobility)
		 */
		int materialScore = 0;
		for (PieceType type : PieceType.getPieceTypes()) {
			int pieceScore = 0;
			Piece piece = getPieces(Colour.WHITE).get(type);
			if (piece != null) {
				pieceScore += piece.calculatePieceSquareValue();
			}
			piece = getPieces(Colour.BLACK).get(type);
			if (piece != null) {
				pieceScore -= piece.calculatePieceSquareValue();
			}
			materialScore += pieceScore;
		}

		// mobility
		// the sidetomove could be in check; for simplicity this is assumed,
		// i.e. 'kingInCheck'==TRUE
		// the other side (who has just moved) cannot be in check
		// if enpassant square is set, this can only apply to the sidetomove
		int whiteMobility, blackMobility;
		Square enpassantSquare = null;
		List<Move> moves = new ArrayList<>(60);
		if (getSideToMove() != Colour.WHITE) {
			enpassantSquare = getEnpassantSquare();
			setEnpassantSquare(null);
		}
		for (PieceType type : PieceType.getPieceTypes()) {
			Piece p = getPieces(Colour.WHITE).get(type);
			moves.addAll(p.findMoves(this, (getSideToMove() == Colour.WHITE ? true : false)));
		}
		if (getSideToMove() != Colour.WHITE) {
			setEnpassantSquare(enpassantSquare);
		}
		whiteMobility = moves.size();
		moves = new ArrayList<>(60);
		if (getSideToMove() != Colour.BLACK) {
			enpassantSquare = getEnpassantSquare();
			setEnpassantSquare(null);
		}
		for (PieceType type : PieceType.getPieceTypes()) {
			Piece p = getPieces(Colour.BLACK).get(type);
			moves.addAll(p.findMoves(this, (getSideToMove() == Colour.BLACK ? true : false)));
		}
		if (getSideToMove() != Colour.BLACK) {
			setEnpassantSquare(enpassantSquare);
		}
		blackMobility = moves.size();

		final int MOBILITY_WEIGHTING = 2;
		int mobilityScore = MOBILITY_WEIGHTING * (whiteMobility - blackMobility);
		return (mobilityScore + materialScore) * (getSideToMove() == Colour.WHITE ? 1 : -1);
	}

	private void updateCastlingRightsAfterMove(Move move, Writer debugWriter) {
		if (PieceType.KING == move.getPiece()) {
			move.setPreviousCastlingRights(castling[sideToMove.ordinal()]);
			castling[sideToMove.ordinal()].clear();
			// writeDebug(debugWriter,
			// "move: " + move + ", sideToMove: " + sideToMove + ", castling=" +
			// castling[sideToMove.ordinal()]);
		} else if (PieceType.ROOK == move.getPiece()) {
			// remove castling rights if rook has moved
			move.setPreviousCastlingRights(castling[sideToMove.ordinal()]);
			if (castling[sideToMove.ordinal()].contains(CastlingRights.KINGS_SIDE)) {
				Square targetSquare = (sideToMove == Colour.WHITE) ? Square.h1 : Square.h8;
				if (move.from() == targetSquare) {
					castling[sideToMove.ordinal()].remove(CastlingRights.KINGS_SIDE);
				}
			}
			if (castling[sideToMove.ordinal()].contains(CastlingRights.QUEENS_SIDE)) {
				Square targetSquare = (sideToMove == Colour.WHITE) ? Square.a1 : Square.a8;
				if (move.from() == targetSquare) {
					castling[sideToMove.ordinal()].remove(CastlingRights.QUEENS_SIDE);
				}
			}
			// writeDebug(debugWriter,
			// "move: " + move + ", sideToMove: " + sideToMove + ", castling=" +
			// castling[sideToMove.ordinal()]);
		}
		// update OPPONENT's castling rights if necessary
		if (move.isCapture()) {
			final Colour opponentsColour = Colour.oppositeColour(sideToMove);
			Square targetSquare = (sideToMove == Colour.WHITE) ? Square.h8 : Square.h1;
			boolean processed = false;
			if (move.to().equals(targetSquare)) {
				move.setPreviousCastlingRightsOpponent(castling[opponentsColour.ordinal()]);
				castling[opponentsColour.ordinal()].remove(CastlingRights.KINGS_SIDE);
				processed = true;
				// writeDebug(debugWriter, "move: " + move + ", removed kings
				// side castling for " + opponentsColour);
			}
			if (!processed) {
				targetSquare = (sideToMove == Colour.WHITE) ? Square.a8 : Square.a1;
				if (move.to().equals(targetSquare)) {
					move.setPreviousCastlingRightsOpponent(castling[opponentsColour.ordinal()]);
					castling[opponentsColour.ordinal()].remove(CastlingRights.QUEENS_SIDE);
					// writeDebug(debugWriter, "move: " + move + ", removed
					// queens side castling for " + opponentsColour);
				}
			}
		}

	}

	// displays the board (always from white POV, a1 in bottom LHS)
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
			for (Piece p : pieceMgr.getPiecesForColour(colour).values()) {
				Square[] locations = p.getLocations();
				for (Square locn : locations) {
					board[locn.rank()][locn.file()] = p.getFenSymbol();
				}
			}
		}

		StringBuilder sb = new StringBuilder(80);
		for (int rank = 7; rank >= 0; rank--) {
			for (int file = 0; file < 8; file++) {
				sb.append(board[rank][file]);
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	// private BitBoard updateBishopsAndQueens(Map<PieceType, Piece> pieces) {
	// Piece queens = pieces.get(PieceType.QUEEN);
	// BitSet queensBitSet;
	// if (queens == null) {
	// queensBitSet = new BitSet(64);
	// } else {
	// queensBitSet = queens.getBitBoard().getBitSet();
	// }
	//
	// Piece bishops = pieces.get(PieceType.BISHOP);
	// BitSet bishopsAndQueens;
	// if (bishops == null) {
	// bishopsAndQueens = new BitSet(64);
	// } else {
	// bishopsAndQueens = bishops.getBitBoard().cloneBitSet();
	// }
	// bishopsAndQueens.or(queensBitSet);
	// return new BitBoard(bishopsAndQueens);
	// }
	//
	// private BitBoard updateRooksAndQueens(Map<PieceType, Piece> pieces) {
	// Piece queens = pieces.get(PieceType.QUEEN);
	// BitSet queensBitSet;
	// if (queens == null) {
	// queensBitSet = new BitSet(64);
	// } else {
	// queensBitSet = queens.getBitBoard().getBitSet();
	// }
	//
	// Piece rooks = pieces.get(PieceType.ROOK);
	// BitSet rooksAndQueens;
	// if (rooks == null) {
	// rooksAndQueens = new BitSet(64);
	// } else {
	// rooksAndQueens = rooks.getBitBoard().cloneBitSet();
	// }
	// rooksAndQueens.or(queensBitSet);
	// return new BitBoard(rooksAndQueens);
	// }

	/**
	 * Updates the given bitset to represent the move. The from and to squares will be flipped. If
	 * castling then the rook's move is also taken into a/c.
	 *
	 * @param bitset the bitset to be updated.
	 * @param move the move. NB only non-capture moves are supported by this method!
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
	 * @param colour the required colour
	 * @return the set of pieces of this colour
	 */
	public Map<PieceType, Piece> getPieces(Colour colour) {
		return pieceMgr.getPiecesForColour(colour);
	}

	/**
	 * Access to a BitBoard of all the pieces of a given colour.
	 *
	 * @param colour the required colour
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

	// just for tests
	PieceManager getPieceManager() {
		return pieceMgr;
	}

	public void debug() {
		for (Colour colour : Colour.values()) {
			System.out.println(colour + " all pieces");
			System.out.println(allEnemyPieces[colour.ordinal()].display());
			System.out.println("---");
		}
		System.out.println("pieces");
		for (Colour colour : Colour.values()) {
			for (PieceType p : pieceMgr.getPiecesForColour(colour).keySet()) {
				System.out.println(p + ", " + colour);
				System.out.println(pieceMgr.getPiece(colour, p).getBitBoard().display());
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
	 * @param game the game
	 * @param targetSquare the square to consider
	 * @param opponentsColour the colour of the opponent
	 * @return true if this square is attacked by the opponent
	 */
	public boolean squareIsAttacked(Square targetSquare, Colour opponentsColour) {
		Map<PieceType, Piece> opponentsPieces = getPieces(opponentsColour);
		// iterate over the pieces
		// TODO instead of treating queens separately, should 'merge' them with
		// the rooks and the bishops
		for (PieceType type : PieceType.ALL_PIECE_TYPES) {
			Piece piece = opponentsPieces.get(type);
			if (piece != null) {
				if (piece.attacksSquare(getEmptySquares().getBitSet(), targetSquare)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks for a discovered check after the move 'move'.
	 * <p>
	 * This will not be 100% correct for moves along the same ray to the opponent's king. But these
	 * moves are already check and not discovered check.
	 *
	 * @param posn the chessboard
	 * @param move the move
	 * @param colour which side is moving
	 * @param opponentsKing where the opponent's king is
	 * @return true if this move leads to a discovered check
	 */
	public static boolean checkForDiscoveredCheck(Position posn, Move move, Colour colour, Square opponentsKing) {
		final int moveFromIndex = move.from().bitIndex();

		// optimization (see RayUtils.discoveredCheck)
		if (null == RayUtils.getRay(opponentsKing, move.from())) {
			return false;
		}

		// set up the emptySquares and myPieces bitsets *after* this move
		BitSet emptySquares = posn.getEmptySquares().cloneBitSet();
		BitSet myPieces = posn.getAllPieces(colour).cloneBitSet();

		emptySquares.set(moveFromIndex);
		myPieces.clear(moveFromIndex);

		// 1) do not need to set 'move.to()' -- if we're moving on the same ray,
		// then it will be check already
		// 2) can't get a discovered check from castling

		return RayUtils.discoveredCheck(colour, posn, emptySquares, myPieces, opponentsKing, move.from());
	}

	/**
	 * Returns true if a piece on 'startSquare' attacks 'targetSquare', i.e. the two squares are on
	 * the same ray and there are no intervening pieces.
	 * <p>
	 * It still depends on the piece type to determine whether there really is an attack.
	 *
	 * @param emptySquares bitset of empty Squares
	 * @param myPieces bitset of my pieces
	 * @param myColour my colour
	 * @param startSquare start square
	 * @param targetSquare target square
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
	 * Checks if my king is in check after my move, i.e. the piece that moved was actually pinned.
	 *
	 * @param posn the current posn
	 * @param move the move
	 * @param colour which side is moving
	 * @param myKing where my king is
	 * @return true if this move is illegal since the piece that moved was pinned
	 */
	public static boolean checkForPinnedPiece(Position posn, Move move, Colour colour, Square myKing) {
		// set up the bitsets *after* this move
		BitSet emptySquares = posn.getEmptySquares().cloneBitSet();
		BitSet myPieces = posn.getAllPieces(colour).cloneBitSet();

		emptySquares.set(move.from().bitIndex());
		emptySquares.clear(move.to().bitIndex());
		if (move.isEnpassant()) {
			emptySquares.set(Square.findMoveFromEnpassantSquare(move.to()).bitIndex());
		}
		myPieces.clear(move.from().bitIndex());
		myPieces.set(move.to().bitIndex());

		return RayUtils.kingInCheck(colour, posn, emptySquares, myPieces, myKing, move.from());
	}

	/**
	 * Checks if my king is in check after the move 'move'.
	 *
	 * @param posn the chessboard
	 * @param move the move
	 * @param opponentsColour this colour's pieces will be inspected to see if they check my king
	 * @param king where my king is
	 * @param kingIsAlreadyInCheck true if the king was already in check before the 'move'
	 * @return true if this move leaves the king in check (i.e. is an illegal move)
	 */
	public static boolean isKingInCheck(Position posn, Move move, Colour opponentsColour, Square king,
			boolean kingIsAlreadyInCheck) {

		// short circuit if king was not in check beforehand (therefore only
		// need to check for a pinned piece) and the
		// moving piece's original square is not on a ray to the king
		if ((move.getPiece() != PieceType.KING) && !kingIsAlreadyInCheck) {
			if (null == RayUtils.getRay(king, move.from())) {
				return false;
			}
		}

		BitSet friendlyPieces = posn.getAllPieces(Colour.oppositeColour(opponentsColour)).getBitSet();
		Map<PieceType, BitSet> enemyPieces = setupEnemyBitsets(posn.getPieces(opponentsColour));

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
	 * @param targetSquare square to use
	 * @return the piece at this location.
	 * @throws IllegalArgumentException if no piece exists at the given square.
	 * @deprecated should be possible to always rewrite using {@link #pieceAt(Square, Colour)}.
	 */
	@Deprecated
	public PieceType pieceAt(Square targetSquare) {
		return pieceAt(targetSquare, null);
	}

	/**
	 * Finds the piece at the given square. TODO optimize using Lookup?
	 *
	 * @param targetSquare square to use
	 * @param colour if not null, this piece's colour is expected.
	 * @return the piece at this location.
	 * @throws IllegalArgumentException if no piece [of the given colour] exists at the given square.
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

	static class PieceManager {
		/**
		 * Stores the pieces in the game. The dimension indicates the colour {white, black}.
		 */
		private Map<PieceType, Piece>[] pieces;
		/**
		 * whether this piece type has already been cloned
		 */
		private Map<PieceType, Boolean>[] alreadyCloned;

		/**
		 * Constructor. The pieces map will be initialised to null values.
		 */
		@SuppressWarnings("unchecked")
		public PieceManager() {
			pieces = new HashMap[Colour.values().length];
			alreadyCloned = new HashMap[Colour.values().length];

			for (Colour col : Colour.values()) {
				pieces[col.ordinal()] = new HashMap<>();
				alreadyCloned[col.ordinal()] = new HashMap<>();
				for (PieceType pt : PieceType.ALL_PIECE_TYPES) {
					alreadyCloned[col.ordinal()].put(pt, Boolean.FALSE);
				}
			}
		}

		/**
		 * Constructor. The pieces map will be initialised to the values of the parameters.
		 * 
		 * @param whitePieces the white pieces
		 * @param blackPieces the black pieces
		 */
		public PieceManager(Set<Piece> whitePieces, Set<Piece> blackPieces) {
			this();
			for (Piece p : whitePieces) {
				getPiecesForColour(Colour.WHITE).put(p.getType(), p);
			}
			for (Piece p : blackPieces) {
				getPiecesForColour(Colour.BLACK).put(p.getType(), p);
			}
		}

		/**
		 * returns a particular Piece object. The Piece <b>will be cloned</b> and inserted into the
		 * 'pieces' hashmap the first time.
		 * 
		 * @param colour the required colour
		 * @param pieceType the required piece type
		 * @return a Piece object
		 */
		public Piece getClonedPiece(Colour colour, PieceType pieceType) {
			if (alreadyCloned[colour.ordinal()].get(pieceType)) {
				return getPiece(colour, pieceType);
			}
			try {
				Piece cloned = (Piece) getPiece(colour, pieceType).clone();
				pieces[colour.ordinal()].put(pieceType, cloned);
				alreadyCloned[colour.ordinal()].put(pieceType, Boolean.TRUE);
				return cloned;
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException("could not clone piece!?");
			}
		}

		/**
		 * returns the piece map for the given colour.
		 * 
		 * @param colour the required colour
		 * @return all pieces for the given colour
		 */
		public Map<PieceType, Piece> getPiecesForColour(Colour colour) {
			return pieces[colour.ordinal()];
		}

		/**
		 * returns a particular Piece object.
		 * 
		 * @param colour the required colour
		 * @param pieceType the required piece type
		 * @return a Piece object
		 */
		public Piece getPiece(Colour colour, PieceType pieceType) {
			return getPiecesForColour(colour).get(pieceType);
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer(300);
			sb.append("PieceManager@").append(Integer.toHexString(System.identityHashCode(this)));
			sb.append("[");
			List<String> tempList = new ArrayList<>();
			for (Colour col : Colour.values()) {
				// sb.append(col).append("{");
				List<String> tempList1 = new ArrayList<>();
				pieces[col.ordinal()].entrySet().stream().forEach(p -> {
					tempList1.add(p.getValue().toString());
				});
				tempList.add(tempList1.stream().collect(Collectors.joining(",")));
			}
			sb.append(tempList.stream().collect(Collectors.joining(",", "{", "}")));
			sb.append("]");
			return sb.toString();
		}
	}

}
