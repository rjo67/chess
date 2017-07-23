package org.rjo.chess;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rjo.chess.pieces.AbstractPiece.SquareCache;
import org.rjo.chess.pieces.Bishop;
import org.rjo.chess.pieces.King;
import org.rjo.chess.pieces.Knight;
import org.rjo.chess.pieces.Pawn;
import org.rjo.chess.pieces.Piece;
import org.rjo.chess.pieces.PieceManager;
import org.rjo.chess.pieces.PieceType;
import org.rjo.chess.pieces.Queen;
import org.rjo.chess.pieces.Rook;
import org.rjo.chess.ray.RayUtils;

/**
 * An immutable object which stores the board position after a particular move.<br>
 * Previously: Chessboard.java.
 *
 * @author rich
 * @since 2016-09-04
 */
public class Position {

	public static int NBR_INSTANCES_CREATED = 0;

	private static final Logger LOG = LogManager.getLogger(Position.class);

	// thread pool for findMove()
	private static ExecutorService threadPool = Executors.newFixedThreadPool(PieceType.getPieceTypes().length);

	/**
	 * if TRUE, finds all moves (potentially non-legal) and removes illegal moves later. If FALSE, just finds legal moves.
	 */
	private static final boolean GENERATE_ILLEGAL_MOVES = Boolean.parseBoolean(System.getProperty("generateIllegalMoves", "false"));

	/**
	 * Controls access to the pieces in the game.
	 */
	private PieceManager pieceMgr;

	/**
	 * bitboard of all pieces for a particular colour. The dimension indicates the colour {white, black}.
	 */
	private BitBoard[] allEnemyPieces;

	/**
	 * bitboard of all pieces on the board (irrespective of colour). Logical NOT of this BitBoard gives a bitboard of all
	 * empty squares.
	 */
	private BitBoard totalPieces;

	/** Indicates an enpassant square; can be null. */
	private Square enpassantSquare;

	/** which sides can still castle */
	private EnumSet<CastlingRights>[] castling;

	/** which side is to move */
	private Colour sideToMove;

	/**
	 * if the king (of the sideToMove) is currently in check. Normally deduced from the last move but can be set
	 * delibarately for tests.
	 */
	private boolean inCheck;

	/** zobrist value of this position */
	private long zobristHash;

	/** stores the fen of this position */
	private String fen;

	/** the evaluated score for this position */
	private PositionScore positionScore;

	public static Position startPosition() {
		return new Position(Colour.WHITE);
	}

	public static void debugLog(String str) {
		if (System.getProperty("CHECK-DEBUG") != null) {
			System.out.println(str);
		}
	}

	/**
	 * Constructs a new position with default starting positions and default castling rights.
	 */
	public Position(Colour sideToMove) {
		// default piece positions
		this(new HashSet<Piece>(Arrays.asList(new Pawn(Colour.WHITE, true), new Rook(Colour.WHITE, true), new Knight(Colour.WHITE, true),
				new Bishop(Colour.WHITE, true), new Queen(Colour.WHITE, true), new King(Colour.WHITE, true))),
				new HashSet<Piece>(Arrays.asList(new Pawn(Colour.BLACK, true), new Rook(Colour.BLACK, true), new Knight(Colour.BLACK, true),
						new Bishop(Colour.BLACK, true), new Queen(Colour.BLACK, true), new King(Colour.BLACK, true))),
				sideToMove,
				// default castling rights
				EnumSet.of(CastlingRights.KINGS_SIDE, CastlingRights.QUEENS_SIDE),
				EnumSet.of(CastlingRights.KINGS_SIDE, CastlingRights.QUEENS_SIDE), null);
	}

	/**
	 * Creates a chessboard with the given piece settings. Castling rights are set to 'none'. Enpassant square is set to
	 * 'null'.
	 */
	public Position(Set<Piece> whitePieces, Set<Piece> blackPieces, Colour sideToMove) {
		this(whitePieces, blackPieces, sideToMove, EnumSet.noneOf(CastlingRights.class), EnumSet.noneOf(CastlingRights.class), null);
	}

	/**
	 * Creates a chessboard with the given piece settings, side to move, castling rights, and enpassant square.
	 *
	 * @param whitePieces the white pieces
	 * @param blackPieces the black pieces
	 * @param sideToMove side to move
	 * @param whiteCastlingRights white's castling rights
	 * @param blackCastlingRights black's castling rights
	 * @param enpassantSquare enpassant square (or null)
	 */
	@SuppressWarnings("unchecked")
	public Position(Set<Piece> whitePieces, Set<Piece> blackPieces, Colour sideToMove, EnumSet<CastlingRights> whiteCastlingRights,
			EnumSet<CastlingRights> blackCastlingRights, Square enpassantSquare) {
		initBoard(whitePieces, blackPieces);
		castling = new EnumSet[2];
		castling[Colour.WHITE.ordinal()] = whiteCastlingRights;
		castling[Colour.BLACK.ordinal()] = blackCastlingRights;
		this.sideToMove = sideToMove;
		this.enpassantSquare = enpassantSquare;

		NBR_INSTANCES_CREATED++;

		this.zobristHash = Zobrist.INSTANCE.hash(this);
		this.fen = Fen.encode(this);
	}

	/**
	 * copy constructor
	 */
	public Position(final Position posn) {
		pieceMgr = new PieceManager(posn.getPieceManager());

		// split into a new method for profiling
		internInit(posn);

		NBR_INSTANCES_CREATED++;
		this.zobristHash = posn.zobristHash;
		// fen is not set here, since will be making a move and then should create it
	}

	@SuppressWarnings("unchecked")
	private void internInit(final Position posn) {
		// need to clone here, since these structures are changed incrementally in updateStructures()

		totalPieces = new BitBoard(posn.totalPieces);

		allEnemyPieces = new BitBoard[2];
		castling = new EnumSet[2];
		for (int i = 0; i < 2; i++) {
			allEnemyPieces[i] = new BitBoard(posn.allEnemyPieces[i]);
			castling[i] = posn.castling[i].clone();
		}
		enpassantSquare = posn.enpassantSquare;
		sideToMove = posn.sideToMove;
	}

	/**
	 * Returns true if the king of the given colour can castle on the given side (king's or queen's).
	 *
	 * @param colour the colour
	 * @param rights whether king's or queen's side
	 * @return true if can castle
	 */
	public boolean canCastle(Colour colour,
			CastlingRights rights) {
		return castling[colour.ordinal()].contains(rights);
	}

	/**
	 * return the castling rights of the current posn. this is for the zobrist calculation.
	 *
	 * @return
	 */
	public EnumSet<CastlingRights>[] getCastlingRights() {
		return castling;
	}

	public Colour getSideToMove() {
		return sideToMove;
	}

	public String getFen() {
		if (fen == null) {
			fen = Fen.encode(this);
		}
		return fen;
	}

	/**
	 * Sets up all pieces and related data structures corresponding to the input parameters.
	 *
	 * @param whitePieces layout of the white pieces
	 * @param blackPieces layout of the black pieces
	 */
	private void initBoard(Set<Piece> whitePieces,
			Set<Piece> blackPieces) {
		this.pieceMgr = new PieceManager(whitePieces, blackPieces);
		allEnemyPieces = new BitBoard[2];
		for (Colour colour : Colour.ALL_COLOURS) {
			allEnemyPieces[colour.ordinal()] = new BitBoard();
			for (PieceType p : PieceType.ALL_PIECE_TYPES) {
				Piece piece = pieceMgr.getPiece(colour, p);
				if (piece != null) {
					allEnemyPieces[colour.ordinal()].getBitSet().or(piece.getBitBoard().getBitSet());
				}
			}
		}
		totalPieces = new BitBoard();
		totalPieces.getBitSet().or(allEnemyPieces[Colour.WHITE.ordinal()].getBitSet());
		totalPieces.getBitSet().or(allEnemyPieces[Colour.BLACK.ordinal()].getBitSet());

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
		} else {
			// capture move
			if (!move.isEnpassant()) {
				allEnemyPieces[colourOrdinal].getBitSet().flip(moveFromBitIndex);
				allEnemyPieces[colourOrdinal].getBitSet().flip(moveToBitIndex);
				allEnemyPieces[oppositeColourOrdinal].getBitSet().flip(moveToBitIndex);
				totalPieces.getBitSet().flip(moveFromBitIndex);
			} else {
				// enpassant
				int enpassantSquareBitIndex = Square.findMoveFromEnpassantSquare(move.to()).bitIndex();
				allEnemyPieces[colourOrdinal].getBitSet().flip(moveFromBitIndex);
				allEnemyPieces[colourOrdinal].getBitSet().flip(moveToBitIndex);
				allEnemyPieces[oppositeColourOrdinal].getBitSet().flip(enpassantSquareBitIndex);
				totalPieces.getBitSet().flip(moveFromBitIndex);
				totalPieces.getBitSet().flip(moveToBitIndex);
				totalPieces.getBitSet().flip(enpassantSquareBitIndex);
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
		return findMoves(colour, this.inCheck);
	}

	/**
	 * Find all moves for the given colour from the current position, overriding the position's <code>inCheck</code> value.
	 *
	 * @param colour the required colour
	 * @param inCheck whether the king is in check in this position
	 * @return all moves for this colour.
	 */
	private List<Move> findMoves(Colour colour,
			boolean inCheck) {
		List<Move> moves = new ArrayList<>(100);
		for (PieceType type : PieceType.ALL_PIECE_TYPES) {
			Piece p = getPieces(colour)[type.ordinal()];
			if (GENERATE_ILLEGAL_MOVES) {
				moves.addAll(p.findPotentialMoves(this));
			} else {
				moves.addAll(p.findMoves(this, inCheck));
			}
		}
		if (GENERATE_ILLEGAL_MOVES) {
			// 'moves' must now be pruned to get rid of illegal moves,
			// i.e. those leaving my king in check
			checkMovesForLegality(moves, inCheck);
		}

		/*
		 * at this point have found all legal moves. Now need to establish which moves leave the opponent's king in check
		 */
		final Square opponentsKing = King.findOpponentsKing(getSideToMove(), this);
		final SquareCache<CheckStates> checkCache = new SquareCache<>();
		final SquareCache<Boolean> discoveredCheckCache = new SquareCache<>();
		final BitSet emptySquares = getTotalPieces().flip();
		for (Move move : moves) {
			Piece p = getPieces(colour)[move.getPiece().ordinal()];
			move.setCheck(p.isOpponentsKingInCheckAfterMove(this, move, opponentsKing, emptySquares, checkCache, discoveredCheckCache));
		}

		return moves;
	}

	/**
	 * Removes any moves that leave my king in check, i.e. which are illegal.
	 *
	 * @param moves the potential moves found so far
	 * @param inCheck whether the king is in check already
	 */
	private void checkMovesForLegality(List<Move> moves,
			boolean inCheck) {
		final SquareCache<CheckStates> checkCache = new SquareCache<>();
		final Square myKing = King.findKing(getSideToMove(), this);

		BitSet friendlyPieces = this.getAllPieces(sideToMove).getBitSet();
		Colour opponentsColour = Colour.oppositeColour(sideToMove);
		BitSet[] enemyPieces = Position.setupEnemyBitsets(this.getPieces(opponentsColour));

		// check if the piece moving away from 'fromSquare' has left my king in (discovered) check
		ListIterator<Move> iter = moves.listIterator();
		while (iter.hasNext()) {
			Move move = iter.next();

			// special for castling
			// castling -- can't castle out of check or over a square in check
			if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
				if (inCheck) {
					iter.remove();
				} else {
					if (move.isCastleKingsSide() && !King.isCastlingLegal(this, sideToMove, opponentsColour, CastlingRights.KINGS_SIDE)) {
						iter.remove();
					}
					if (move.isCastleQueensSide() && !King.isCastlingLegal(this, sideToMove, opponentsColour, CastlingRights.QUEENS_SIDE)) {
						iter.remove();
					}
				}
			} else {

				enemyPieces = Position.setupEnemyBitsets(this.getPieces(opponentsColour));
				if (KingCheck.isKingInCheckAfterMove(myKing, sideToMove, friendlyPieces, enemyPieces, move, inCheck)) {
					iter.remove();
				}

				// Square fromSquare = move.from();
				// Ray rayToKing = RayUtils.getRay(fromSquare, myKing);
			}
		}

	}

	// public List<Move> findMovesParallel(Colour colour) {
	// // set up tasks
	// List<Callable<List<Move>>> tasks = new ArrayList<>();
	// for (PieceType type : PieceType.getPieceTypes()) {
	// tasks.add(new Callable<List<Move>>() {
	//
	// @Override
	// public List<Move> call() throws Exception {
	// Piece p = getPieces(colour)[type.ordinal()];
	// return p.findMoves(Position.this, inCheck);
	// }
	//
	// });
	// }
	//
	// // and execute
	// List<Move> moves = new ArrayList<>(60);
	// try {
	// List<Future<List<Move>>> results = threadPool.invokeAll(tasks);
	//
	// for (Future<List<Move>> f : results) {
	// moves.addAll(f.get());
	// }
	// } catch (InterruptedException | ExecutionException e) {
	// throw new RuntimeException("got InterruptedException from future (findMovesParallel)", e);
	// }
	//
	// return moves;
	// }

	private void writeDebug(Writer debugWriter,
			String string) {
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
	public Position move(Move move,
			Writer debugWriter) {
		Position newPosn = new Position(this);
		newPosn.internalMove(move, debugWriter);
		if (Zobrist.CHECK_HASH_UPDATE_AFTER_MOVE) {
			long updatedHash = newPosn.zobristHash;
			Position posnAfterMove = Fen.decode(Fen.encode(newPosn)).getPosition();
			if (updatedHash != posnAfterMove.zobristHash) {
				throw new IllegalStateException("non-matching zobrist\nposn:\n" + this + "\nmove: " + move + "\nnewPosn:\n" + newPosn);
			}
		}
		return newPosn;
	}

	/**
	 * Performs the given move, updating internal data structures.
	 *
	 * @param move the move
	 * @param debugWriter if not null, debug info will be written here
	 */
	private void internalMove(Move move,
			Writer debugWriter) {
		if (move.getColour() != sideToMove) {
			throw new IllegalArgumentException("move is for '" + move.getColour() + "' but sideToMove=" + sideToMove);
		}

		// update hash before the castling rights / enpassant square are changed
		zobristHash = Zobrist.INSTANCE.update(zobristHash, move, castling, enpassantSquare);

		if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
			pieceMgr.getClonedPiece(sideToMove, move.getPiece()).move(move);
			pieceMgr.getClonedPiece(sideToMove, PieceType.ROOK).move(move.getRooksCastlingMove());
			// castling rights are reset later on
		} else {
			if (!move.isCapture() && getTotalPieces().getBitSet().get(move.to().bitIndex())) {
				throw new IllegalArgumentException("square " + move.to() + " is not empty. Move=" + move);
			}
			// update structures for the moving piece
			pieceMgr.getClonedPiece(sideToMove, move.getPiece()).move(move);
			// capture: remove the captured piece
			if (move.isCapture()) {
				if (move.isEnpassant()) {
					pieceMgr.getClonedPiece(Colour.oppositeColour(sideToMove), move.getCapturedPiece())
							.removePiece(Square.findMoveFromEnpassantSquare(move.to()));
				} else {
					pieceMgr.getClonedPiece(Colour.oppositeColour(sideToMove), move.getCapturedPiece()).removePiece(move.to());
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
			enpassantSquare = Square.findEnpassantSquareFromMove(move.to());
		} else {
			enpassantSquare = null;
		}
		sideToMove = Colour.oppositeColour(sideToMove);
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

	public void setPositionScore(PositionScore positionScore) {
		this.positionScore = positionScore;
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
	 * Calculates a static value for the current position. In order for NegaMax to work, it is important to return the score
	 * relative to the side being evaluated.
	 *
	 * @return a value in centipawns
	 */
	public int evaluate() {
		/*
		 * materialScore = kingWt * (wK-bK) + queenWt * (wQ-bQ) + rookWt * (wR-bR) + knightWt* (wN-bN) + bishopWt* (wB-bB) +
		 * pawnWt * (wP-bP) mobilityScore = mobilityWt * (wMobility-bMobility)
		 */
		int materialScore = 0;
		for (PieceType type : PieceType.ALL_PIECE_TYPES) {
			int pieceScore = 0;
			Piece piece = getPieces(Colour.WHITE)[type.ordinal()];
			if (piece != null) {
				pieceScore += piece.calculatePieceSquareValue();
			}
			piece = getPieces(Colour.BLACK)[type.ordinal()];
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
		Square prevEnpassantSquare = null;
		if (getSideToMove() != Colour.WHITE) {
			prevEnpassantSquare = getEnpassantSquare();
			enpassantSquare = null;
		}
		List<Move> moves = findMoves(Colour.WHITE, getSideToMove() == Colour.WHITE ? true : false);
		if (getSideToMove() != Colour.WHITE) {
			enpassantSquare = prevEnpassantSquare;
		}
		whiteMobility = moves.size();
		moves = new ArrayList<>(60);
		if (getSideToMove() != Colour.BLACK) {
			prevEnpassantSquare = getEnpassantSquare();
			enpassantSquare = null;
		}
		moves = findMoves(Colour.BLACK, getSideToMove() == Colour.BLACK ? true : false);
		if (getSideToMove() != Colour.BLACK) {
			enpassantSquare = prevEnpassantSquare;
		}
		blackMobility = moves.size();

		final int MOBILITY_WEIGHTING = 2;
		int mobilityScore = MOBILITY_WEIGHTING * (whiteMobility - blackMobility);
		return (mobilityScore + materialScore) * (getSideToMove() == Colour.WHITE ? 1 : -1);
	}

	/**
	 * If the king moved then remove all castling rights<br>
	 * and if a rook moved, remove the appropriate castling right
	 */
	private void updateCastlingRightsAfterMove(Move move,
			Writer debugWriter) {
		if (PieceType.KING == move.getPiece()) {
			move.setPreviousCastlingRights(castling[sideToMove.ordinal()]);
			castling[sideToMove.ordinal()].clear();
			// writeDebug(debugWriter, "move: " + move + ", sideToMove: " + sideToMove + ", castling=" + castling[sideToMove.ordinal()]);
		} else if (PieceType.ROOK == move.getPiece()) {
			// remove castling rights if rook has moved
			move.setPreviousCastlingRights(castling[sideToMove.ordinal()]);
			if (CastlingRights.kingsSideCastlingRightsGoneAfterMove(castling[sideToMove.ordinal()], sideToMove, move)) {
				castling[sideToMove.ordinal()].remove(CastlingRights.KINGS_SIDE);
			}
			if (CastlingRights.queensSideCastlingRightsGoneAfterMove(castling[sideToMove.ordinal()], sideToMove, move)) {
				castling[sideToMove.ordinal()].remove(CastlingRights.QUEENS_SIDE);
			}
			// writeDebug(debugWriter, "move: " + move + ", sideToMove: " + sideToMove + ", castling=" + castling[sideToMove.ordinal()]);
		}
		// update OPPONENT's castling rights if necessary
		if (move.isCapture()) {
			final Colour opponentsColour = Colour.oppositeColour(sideToMove);
			boolean processed = false;
			if (CastlingRights.opponentKingsSideCastlingRightsGoneAfterMove(castling[opponentsColour.ordinal()], sideToMove, move)) {
				move.setPreviousCastlingRightsOpponent(castling[opponentsColour.ordinal()]);
				castling[opponentsColour.ordinal()].remove(CastlingRights.KINGS_SIDE);
				processed = true;
				// writeDebug(debugWriter, "move: " + move + ", removed kings side castling for " + opponentsColour);
			}
			if (!processed) {
				if (CastlingRights.opponentQueensSideCastlingRightsGoneAfterMove(castling[opponentsColour.ordinal()], sideToMove, move)) {
					move.setPreviousCastlingRightsOpponent(castling[opponentsColour.ordinal()]);
					castling[opponentsColour.ordinal()].remove(CastlingRights.QUEENS_SIDE);
					// writeDebug(debugWriter, "move: " + move + ", removed queens side castling for " + opponentsColour);
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
		for (Colour colour : Colour.ALL_COLOURS) {
			for (PieceType pt : PieceType.ALL_PIECE_TYPES) {
				Piece p = getPieceManager().getPiece(colour, pt);
				Square[] locations = p.getLocations();
				for (Square locn : locations) {
					board[locn.rank()][locn.file()] = p.getFenSymbol();
				}
			}
		}

		StringBuilder sb = new StringBuilder(150);
		for (int rank = 7; rank >= 0; rank--) {
			for (int file = 0; file < 8; file++) {
				sb.append(board[rank][file]);
			}
			switch (rank) {
			case 7:
				sb.append("   " + sideToMove + " to move");
				break;
			case 6:
				sb.append("   castlingRights: " + castling[0] + ", " + castling[1]);
				break;
			case 5:
				sb.append("   enpassant square: " + enpassantSquare);
				break;
			case 4:
				sb.append("   hash (zobrist): " + hashCode());
				break;
			default:
				break;
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
	 * Updates the given bitset to represent the move. The from and to squares will be flipped. If castling then the rook's
	 * move is also taken into a/c.
	 *
	 * @param bitset the bitset to be updated.
	 * @param move the move. NB only non-capture moves are supported by this method!
	 */
	private void updateBitSet(BitSet bitset,
			Move move) {
		bitset.flip(move.from().bitIndex());
		bitset.flip(move.to().bitIndex());
		if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
			bitset.flip(move.getRooksCastlingMove().from().bitIndex());
			bitset.flip(move.getRooksCastlingMove().to().bitIndex());
		}
	}

	/**
	 * Access to the pieces of a given colour.
	 *
	 * @param colour the required colour
	 * @return the pieces of this colour
	 */
	public Piece[] getPieces(Colour colour) {
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
	 * Access to a BitBoard of all the pieces irrespective of colour. Logical NOT of this BitBoard gives a bitboard of all
	 * empty squares.
	 *
	 * @return a BitBoard containing all the pieces irrespective of colour.
	 */
	public BitBoard getTotalPieces() {
		return totalPieces;
	}

	public PieceManager getPieceManager() {
		return pieceMgr;
	}

	public PositionScore getPositionScore() {
		return positionScore;
	}

	public void debug() {
		for (Colour colour : Colour.ALL_COLOURS) {
			System.out.println(colour + " all pieces");
			System.out.println(allEnemyPieces[colour.ordinal()].display());
			System.out.println("---");
		}
		System.out.println("pieces");
		for (Colour colour : Colour.ALL_COLOURS) {
			for (PieceType p : PieceType.ALL_PIECE_TYPES) {
				System.out.println(p + ", " + colour);
				System.out.println(pieceMgr.getPiece(colour, p).getBitBoard().display());
				System.out.println("---");
			}
		}
		System.out.println("totalPieces");
		System.out.println(totalPieces.display());
		System.out.println("---");

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
	public boolean squareIsAttacked(Square targetSquare,
			Colour opponentsColour) {
		Piece[] opponentsPieces = getPieces(opponentsColour);
		// iterate over the pieces
		// TODO instead of treating queens separately, could 'merge' them with the rooks and the bishops
		BitSet emptySquares = getTotalPieces().flip();
		for (PieceType type : PieceType.ALL_PIECE_TYPES) {
			Piece piece = opponentsPieces[type.ordinal()];
			if (piece != null) {
				if (piece.attacksSquare(emptySquares, targetSquare)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks for a discovered check after the move <code>move</code>.
	 * <p>
	 * This will not be 100% correct for moves along the same ray to the opponent's king. But these moves are already check
	 * and not discovered check.
	 *
	 * @param posn the chessboard
	 * @param move the move
	 * @param colour which side is moving
	 * @param opponentsKing where the opponent's king is
	 * @return true if this move leads to a discovered check
	 */
	public static boolean checkForDiscoveredCheck(Position posn,
			Move move,
			Colour colour,
			Square opponentsKing) {
		final int moveFromIndex = move.from().bitIndex();

		// optimization (see RayUtils.discoveredCheck)
		if (null == RayUtils.getRay(opponentsKing, move.from())) {
			return false;
		}

		// set up the emptySquares and myPieces bitsets *after* this move
		BitSet emptySquares = posn.getTotalPieces().flip();
		BitSet myPieces = posn.getAllPieces(colour).cloneBitSet();

		emptySquares.set(moveFromIndex);
		myPieces.clear(moveFromIndex);

		// 1) do not need to set 'move.to()' -- if we're moving on the same ray, then it will be check already
		// 2) can't get a discovered check from castling

		return RayUtils.discoveredCheck(colour, posn, emptySquares, myPieces, opponentsKing, move.from());
	}

	/**
	 * Returns true if a piece on <code>startSquare</code> attacks <code>targetSquare</code>, i.e. the two squares are on
	 * the same ray and there are no intervening pieces.
	 * <p>
	 * It still depends on the piece type to determine whether there really is an attack.
	 *
	 * @param emptySquares bitset of empty Squares
	 * @param myPieces bitset of my pieces
	 * @param myColour my colour
	 * @param startSquare start square
	 * @param targetSquare target square
	 * @return true if a piece on <code>startSquare</code> attacks <code>targetSquare</code>
	 */
	// public static boolean checkIfPieceOnSquare1CouldAttackSquare2(BitSet emptySquares, BitSet
	// myPieces, Colour myColour,
	// Square startSquare, Square targetSquare) {
	// Ray ray = RayUtils.getRay(startSquare, targetSquare);
	// if (ray != null) {
	// RayInfo info = RayUtils.findFirstPieceOnRay(myColour, emptySquares, myPieces, ray,
	// startSquare.bitIndex());
	// int targetSquareIndex = targetSquare.bitIndex();
	// if (info.foundPiece() && info.getIndexOfPiece() == targetSquareIndex) {
	// return true;
	// }
	// for (int sq : info.getEmptySquares()) {
	// if (sq == targetSquareIndex) {
	// return true;
	// }
	// }
	// }
	// return false;
	// }

	/**
	 * Checks if my king is in check after my move, i.e. the piece that moved was actually pinned.
	 *
	 * @param posn the current posn
	 * @param move the move
	 * @param colour which side is moving
	 * @param myKing where my king is
	 * @return true if this move is illegal since the piece that moved was pinned
	 */
	// public static boolean checkForPinnedPiece(Position posn, Move move, Colour colour, Square myKing) {
	// // set up the bitsets *after* this move
	// BitSet emptySquares = posn.getTotalPieces().flip();
	// BitSet myPieces = posn.getAllPieces(colour).cloneBitSet();
	//
	// emptySquares.set(move.from().bitIndex());
	// emptySquares.clear(move.to().bitIndex());
	// if (move.isEnpassant()) {
	// emptySquares.set(Square.findMoveFromEnpassantSquare(move.to()).bitIndex());
	// }
	// myPieces.clear(move.from().bitIndex());
	// myPieces.set(move.to().bitIndex());
	//
	// return RayUtils.kingInCheck(colour, posn, emptySquares, myPieces, myKing, move.from());
	// }

	/**
	 * Returns the bitsets of the pieces parameter.
	 *
	 * @param pieces
	 * @return a bitset array
	 */
	public static BitSet[] setupEnemyBitsets(Piece[] pieces) {
		BitSet[] enemyPieces = new BitSet[PieceType.ALL_PIECE_TYPES.length];
		for (PieceType type : PieceType.ALL_PIECE_TYPES) {
			enemyPieces[type.ordinal()] = pieces[type.ordinal()].getBitBoard().getBitSet();
		}
		return enemyPieces;
	}

	/**
	 * Finds the piece at the given square. TODO optimize using Lookup?
	 *
	 * @param targetSquare square to use
	 * @param colour if not null, this piece's colour is expected.
	 * @return the piece at this location.
	 * @throws IllegalArgumentException if no piece [of the given colour] exists at the given square.
	 */
	public PieceType pieceAt(Square targetSquare,
			Colour expectedColour) {
		for (Colour colour : Colour.ALL_COLOURS) {
			if ((expectedColour != null) && (colour != expectedColour)) {
				continue;
			}
			for (PieceType type : PieceType.ALL_PIECE_TYPES) {
				Piece p = getPieces(colour)[type.ordinal()];
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

	@Override
	public int hashCode() {
		return (int) this.zobristHash;
	}

	@Override
	public boolean equals(Object other) {

		if (other instanceof Position) {
			Position otherPosn = (Position) other;
			if (this.zobristHash != otherPosn.zobristHash) {
				return false;
			}
			return this.getFen().equals(otherPosn.getFen());
		} else {
			return false;
		}
	}
}
