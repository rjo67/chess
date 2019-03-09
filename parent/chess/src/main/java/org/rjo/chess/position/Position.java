package org.rjo.chess.position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rjo.chess.SystemFlags;
import org.rjo.chess.base.CastlingRightsSummary;
import org.rjo.chess.base.CastlingRightsSummary.CastlingRights;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.Move.CheckInformation;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.SquareCache;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.bits.BitSetUnifier;
import org.rjo.chess.base.ray.Ray;
import org.rjo.chess.base.ray.RayInfo;
import org.rjo.chess.base.ray.RayType;
import org.rjo.chess.base.ray.RayUtils;
import org.rjo.chess.pieces.Bishop;
import org.rjo.chess.pieces.King;
import org.rjo.chess.pieces.Knight;
import org.rjo.chess.pieces.Pawn;
import org.rjo.chess.pieces.Piece;
import org.rjo.chess.pieces.PieceManager;
import org.rjo.chess.pieces.Queen;
import org.rjo.chess.pieces.Rook;
import org.rjo.chess.pieces.SlidingPiece;
import org.rjo.chess.position.PositionCheckState.CheckInfo;
import org.rjo.chess.position.check.CheckRestriction;
import org.rjo.chess.position.check.CheckStates;
import org.rjo.chess.position.check.KingCheck;

/**
 * An immutable object which stores the board position after a particular move.<br>
 * <i>(Previously: Chessboard.java.)</i>
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
	 * Controls access to the pieces in the game.
	 */
	private PieceManager pieceMgr;

	/**
	 * bitboard of all pieces for a particular colour. The dimension indicates the colour {white, black}.
	 */
	private BitBoard[] allEnemyPieces;

	/**
	 * bitboard of all pieces on the board (irrespective of colour). Logical NOT of this BitBoard gives a bitboard of all
	 * empty squares (see {@link #emptySquares}.
	 */
	private BitBoard totalPieces;

	/**
	 * BitSet of all empty squares. Logical NOT of this BitBoard gives {@link #totalPieces}. Only created 'on demand', see
	 * {@link #getEmptySquares()}.
	 */
	private BitSetUnifier emptySquares;

	/** Indicates an enpassant square; can be null. */
	private Square enpassantSquare;

	/** which sides can still castle. <B>must be cloned on write</B> */
	private CastlingRightsSummary[] castling;

	/** which side is to move */
	private Colour sideToMove;

	/**
	 * check information of the previous move.
	 */
	private CheckInformation checkInformation;

	/** zobrist value of this position */
	private long zobristHash;

	/** stores the fen of this position */
	private String fen;

	/** the evaluated score for this position */
	private PositionScore positionScore;

	/**
	 * Which squares lead to check on the opponent's king. One for White's POV, one for Black's.
	 * <ul>
	 * >
	 * <li>checkState[0] stores white's POV, i.e. which squares lead to a check on the black king.</li>
	 * <li>checkState[1] stores black's POV, i.e. which squares lead to a check on the white king.</li>
	 * <p>
	 * Incrementally updated after each move (in the new position).
	 * <p>
	 * See {@link SystemFlags#USE_CHECK_STATE}.
	 */
	private PositionCheckState[] checkState;

	/** squares where the kings are, stored here as optimization */
	private Square[] kingPosition = new Square[Colour.ALL_COLOURS.length];

	public static Position startPosition() {
		return new Position(Colour.WHITE);
	}

	/**
	 * Constructs a new position with default starting positions and default castling rights.
	 */
	public Position(Colour sideToMove) {
		// default piece positions
		this(new HashSet<>(Arrays.asList(new Pawn(Colour.WHITE, true), new Rook(Colour.WHITE, true), new Knight(Colour.WHITE, true),
				new Bishop(Colour.WHITE, true), new Queen(Colour.WHITE, true), new King(Colour.WHITE, true))),
				new HashSet<>(Arrays.asList(new Pawn(Colour.BLACK, true), new Rook(Colour.BLACK, true), new Knight(Colour.BLACK, true),
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
	public Position(Set<Piece> whitePieces, Set<Piece> blackPieces, Colour sideToMove, EnumSet<CastlingRights> whiteCastlingRights,
			EnumSet<CastlingRights> blackCastlingRights, Square enpassantSquare) {
		initBoard(whitePieces, blackPieces);
		castling = new CastlingRightsSummary[2];
		castling[Colour.WHITE.ordinal()] = new CastlingRightsSummary(whiteCastlingRights);
		castling[Colour.BLACK.ordinal()] = new CastlingRightsSummary(blackCastlingRights);
		this.sideToMove = sideToMove;
		this.enpassantSquare = enpassantSquare;
		this.checkInformation = CheckInformation.NOT_CHECK;

		NBR_INSTANCES_CREATED++;

		this.zobristHash = Zobrist.INSTANCE.hash(this);
		this.fen = Fen.encode(this);
		this.checkState = new PositionCheckState[Colour.values().length];
		if (SystemFlags.USE_CHECK_STATE) {
			this.checkState[Colour.WHITE.ordinal()] = new PositionCheckState();
			this.checkState[Colour.BLACK.ordinal()] = new PositionCheckState();
		} else {
			//			this.checkState[Colour.WHITE.ordinal()] = PositionCheckState.NOOP_STATE;
			//			this.checkState[Colour.BLACK.ordinal()] = PositionCheckState.NOOP_STATE;
		}
		this.kingPosition[Colour.WHITE.ordinal()] = pieceMgr.getPiece(Colour.WHITE, PieceType.KING).getLocations()[0];
		this.kingPosition[Colour.BLACK.ordinal()] = pieceMgr.getPiece(Colour.BLACK, PieceType.KING).getLocations()[0];
	}

	/**
	 * copy constructor
	 */
	public Position(final Position otherPosn) {
		pieceMgr = new PieceManager(otherPosn.pieceMgr);

		// need to clone here, since these structures are changed incrementally in updateStructures()

		totalPieces = new BitBoard(otherPosn.totalPieces);
		emptySquares = null;

		allEnemyPieces = new BitBoard[2];
		castling = new CastlingRightsSummary[2];
		this.checkState = new PositionCheckState[2];
		for (int i = 0; i < 2; i++) {
			allEnemyPieces[i] = otherPosn.allEnemyPieces[i]; // cloned on move (updateStructures)
			// castling rights are cloned on write
			castling[i] = otherPosn.castling[i];
			if (SystemFlags.USE_CHECK_STATE) {
				checkState[i] = new PositionCheckState(otherPosn.checkState[i]);
			} else {
				//				checkState[i] = new PositionCheckState.NoOpPositionCheckState();
			}
			kingPosition[i] = otherPosn.kingPosition[i];
		}
		enpassantSquare = otherPosn.enpassantSquare;
		sideToMove = otherPosn.sideToMove;

		NBR_INSTANCES_CREATED++;
		this.zobristHash = otherPosn.zobristHash;
		// fen is not set here, since will be making a move straight away and should create it then
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
		return castling[colour.ordinal()].canCastle(rights);
	}

	/**
	 * return the castling rights of the current posn. this is for the zobrist calculation.
	 *
	 * @return castling rights
	 */
	public CastlingRightsSummary[] getCastlingRights() {
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

	public long getZobristHash() {
		return zobristHash;
	}

	/**
	 * Returns the position of the king with the requested colour.
	 *
	 * @param colour the required colour
	 * @return the king's position.
	 */
	public Square getKingPosition(Colour colour) {
		return kingPosition[colour.ordinal()];
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
		emptySquares = null;

		enpassantSquare = null;
	}

	/**
	 * update the internal structures (after a move).
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

		allEnemyPieces[colourOrdinal] = new BitBoard(allEnemyPieces[colourOrdinal]); // clone
		allEnemyPieces[oppositeColourOrdinal] = new BitBoard(allEnemyPieces[oppositeColourOrdinal]); // clone

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
		emptySquares = null; // will be recreated on-demand
	}

	/**
	 * Find all moves for the given colour from the current position.
	 *
	 * @param colour the required colour
	 * @return all moves for this colour.
	 */
	public List<Move> findMoves(Colour colour) {
		return findMoves(colour, this.checkInformation);
	}

	/**
	 * Find all moves for the given colour from the current position, overriding the position's <code>inCheck</code> value.
	 *
	 * @param colour the required colour
	 * @param checkInformation stores whether the king is in check in this position
	 * @return all moves for this colour.
	 */
	private List<Move> findMoves(Colour colour,
			CheckInformation checkInformation) {

		List<Move> moves = new ArrayList<>(100);

		if (SystemFlags.INSPECT_CHECKS_FIRST) {

			var boardInfo = KingCheck.isKingInCheck(getKingPosition(colour),
					colour, this.getAllPieces(colour).getBitSet(),
					setupBitsets(this.getPieces(colour)),
					setupBitsets(this.getPieces(Colour.oppositeColour(colour))),
					null, true);

			// double check -- king must move
			// single check -- set up check restriction
			// otherwise process as normal, but with info about pinned pieces
			if (boardInfo.isDoubleCheck()) {
				moves.addAll(getPieces(colour)[PieceType.KING.ordinal()].findMoves(this, boardInfo));
			} else if (boardInfo.isKingInCheck()) {
				for (PieceType type : PieceType.ALL_PIECE_TYPES) {
					Piece p = getPieces(colour)[type.ordinal()];
					moves.addAll(p.findMoves(this, boardInfo));
				}
			} else {
				for (PieceType type : PieceType.ALL_PIECE_TYPES) {
					Piece p = getPieces(colour)[type.ordinal()];
					moves.addAll(p.findMoves(this, checkInformation, CheckRestriction.NO_RESTRICTION));
				}
			}
		} else {
			// set up 'squareRestrictions' if in check
			CheckRestriction checkRestriction;
			if (checkInformation.isCheck()) {
				BitBoard squareRestriction = BitBoard.allSet();
				// if it is check, we restrict the possible move.to() squares
				if (checkInformation.getCheckingSquare() != null) {
					Square kingsPosn = getKingPosition(colour);
					Ray ray = RayUtils.getRay(checkInformation.getCheckingSquare(), kingsPosn);
					// TODO could be null if discovered check (since not dealing with this properly as yet)
					if (ray != null) {
						// set up bitset from checksquare to opponent's king
						squareRestriction = new BitBoard();
						squareRestriction.set(checkInformation.getCheckingSquare());
						Iterator<Integer> iter = ray.squaresFrom(checkInformation.getCheckingSquare());
						while (iter.hasNext()) {
							int sq = iter.next();
							if (sq == kingsPosn.bitIndex()) {
								break;
							}
							squareRestriction.set(sq);
						}
					}
				}
				checkRestriction = new CheckRestriction(squareRestriction);
			} else {
				checkRestriction = CheckRestriction.NO_RESTRICTION;
			}

			for (PieceType type : PieceType.ALL_PIECE_TYPES) {
				Piece p = getPieces(colour)[type.ordinal()];
				if (SystemFlags.GENERATE_ILLEGAL_MOVES) {
					moves.addAll(p.findPotentialMoves(this, checkRestriction));
				} else {
					moves.addAll(p.findMoves(this, checkInformation, checkRestriction));
				}
			}
			if (SystemFlags.GENERATE_ILLEGAL_MOVES) {
				// 'moves' must now be pruned to get rid of illegal moves,
				// i.e. those leaving my king in check
				checkMovesForLegality(moves, checkInformation.isCheck());
			}
		}

		/*
		 * at this point have found all legal moves. Now need to establish which moves leave the opponent's king in check.
		 */
		final Square opponentsKing = getKingPosition(Colour.oppositeColour(sideToMove));
		final SquareCache<Boolean> discoveredCheckCache = new SquareCache<>((Boolean) null);//TODO this cache should not be using null
		final BitSetUnifier emptySquares = getEmptySquares();
		for (Move move : moves) {
			Piece p = getPieces(colour)[move.getPiece().ordinal()];
			PositionCheckState pcs;
			if (SystemFlags.USE_CHECK_STATE) {
				pcs = checkState[colour.ordinal()];
			} else {
				pcs = PositionCheckState.NOOP_STATE;
			}
			CheckInformation checkInfo = p.isOpponentsKingInCheckAfterMove(this, move, opponentsKing, emptySquares,
					pcs, discoveredCheckCache);
			move.setCheck(checkInfo);
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
		final Square myKing = getKingPosition(sideToMove);

		BitSetUnifier friendlyPieces = this.getAllPieces(sideToMove).getBitSet();
		Colour opponentsColour = Colour.oppositeColour(sideToMove);

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
				BitSetUnifier[] enemyPieces = Position.setupBitsets(this.getPieces(opponentsColour));
				if (KingCheck.isKingInCheckAfterMove(myKing, sideToMove, friendlyPieces, enemyPieces, move, inCheck)) {
					iter.remove();
				}

				// Square fromSquare = move.from();
				// Ray rayToKing = RayUtils.getRay(fromSquare, myKing);
			}
		}

	}

	private void logDebug(String string) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(string);
		}
	}

	/**
	 * returns the new position after the given move.
	 *
	 * @param move the move
	 * @return a new Position object with the position after the given move
	 */
	public Position move(Move move) {
		Position newPosn = new Position(this);
		newPosn.internalMove(move);
		if (SystemFlags.CHECK_HASH_UPDATE_AFTER_MOVE) {
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
	 */
	private void internalMove(Move move) {
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
			if (!move.isCapture() && getTotalPieces().get(move.to().bitIndex())) {
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
		// update the locally held position of the king
		if (move.getPiece() == PieceType.KING) {
			kingPosition[sideToMove.ordinal()] = move.to();
		}
		if (SystemFlags.USE_CHECK_STATE) {
			updateCheckStateAfterMove(move, getKingPosition(sideToMove), getKingPosition(Colour.oppositeColour(sideToMove)));
		}

		updateCastlingRightsAfterMove(move);
		if (move.isPawnMoveTwoSquaresForward()) {
			enpassantSquare = Square.findEnpassantSquareFromMove(move.to());
		} else {
			enpassantSquare = null;
		}
		if (move.getPiece() == PieceType.KING) {
			kingPosition[sideToMove.ordinal()] = move.to();
		}
		checkInformation = move.getCheckInformation();
		sideToMove = Colour.oppositeColour(sideToMove);
	}

	/**
	 * Just for Fen.decode(): indicate that in this position the king of the side to move is in check.
	 *
	 * @param inCheck true when the king is in check.
	 */
	public void setInCheck(boolean inCheck) {
		this.checkInformation = inCheck ? CheckInformation.CHECK : CheckInformation.NOT_CHECK;
	}

	public boolean isInCheck() {
		return checkInformation.isCheck();
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
	 * Calculates a static value for the current position.
	 * <p>
	 * <B>algorithm has been changed, this does not currently apply:</B>In order for NegaMax to work, it is important to
	 * return the score relative to the side being evaluated.
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
		List<Move> moves = findMoves(Colour.WHITE, getSideToMove() == Colour.WHITE ? CheckInformation.CHECK : CheckInformation.NOT_CHECK);
		if (getSideToMove() != Colour.WHITE) {
			enpassantSquare = prevEnpassantSquare;
		}
		whiteMobility = moves.size();

		if (getSideToMove() != Colour.BLACK) {
			prevEnpassantSquare = getEnpassantSquare();
			enpassantSquare = null;
		}
		moves = findMoves(Colour.BLACK, getSideToMove() == Colour.BLACK ? CheckInformation.CHECK : CheckInformation.NOT_CHECK);
		if (getSideToMove() != Colour.BLACK) {
			enpassantSquare = prevEnpassantSquare;
		}
		blackMobility = moves.size();

		final int MOBILITY_WEIGHTING = 2;
		int mobilityScore = MOBILITY_WEIGHTING * (whiteMobility - blackMobility);
		//return (mobilityScore + materialScore) * (getSideToMove() == Colour.WHITE ? 1 : -1);
		return (mobilityScore + materialScore);
	}

	/**
	 * If the king moved then remove all castling rights<br>
	 * and if a rook moved, remove the appropriate castling right.
	 * <p>
	 * The data structure must be cloned before being changed!
	 */
	private void updateCastlingRightsAfterMove(Move move) {
		int mySide = sideToMove.ordinal();
		if (castling[mySide].cannotCastle()) {
			// no-op, couldn't castle before
		} else {
			CastlingRightsSummary newRights = null;
			if (PieceType.KING == move.getPiece()) {
				newRights = CastlingRightsSummary.NO_RIGHTS;
			} else if (PieceType.ROOK == move.getPiece()) {
				// remove castling rights if rook has moved
				if (CastlingRightsSummary.kingsSideCastlingRightsGoneAfterMove(castling[mySide], sideToMove, move)) {
					newRights = new CastlingRightsSummary(castling[mySide]);
					newRights.removeKingsSideCastlingRight();
				}
				if (CastlingRightsSummary.queensSideCastlingRightsGoneAfterMove(castling[mySide], sideToMove, move)) {
					newRights = new CastlingRightsSummary(castling[mySide]);
					newRights.removeQueensSideCastlingRight();
				}
			}
			if (newRights != null) {
				move.setPreviousCastlingRights(castling[mySide]);
				castling[mySide] = newRights;
				// logDebug("move: " + move + ", sideToMove: " + sideToMove + ", castling=" + castling[mySide]);
			}
		}
		// update OPPONENT's castling rights if necessary
		final int opponentsSide = Colour.oppositeColour(sideToMove).ordinal();
		if (move.isCapture() && castling[opponentsSide].canCastle()) {
			CastlingRightsSummary newRights = null;
			if (CastlingRightsSummary.opponentKingsSideCastlingRightsGoneAfterMove(castling[opponentsSide], sideToMove, move)) {
				newRights = new CastlingRightsSummary(castling[opponentsSide]);
				newRights.removeKingsSideCastlingRight();
			} else if (CastlingRightsSummary.opponentQueensSideCastlingRightsGoneAfterMove(castling[opponentsSide], sideToMove, move)) {
				newRights = new CastlingRightsSummary(castling[opponentsSide]);
				newRights.removeQueensSideCastlingRight();
			}
			if (newRights != null) {
				move.setPreviousCastlingRights(castling[opponentsSide]);
				castling[opponentsSide] = newRights;
				// logDebug("move: " + move + ", sideToMove: " + sideToMove + ", opponent's castling=" + castling[opponentsSide]);
			}
		}
	}

	/** update the check state after <code>move</code> */
	private void updateCheckStateAfterMove(Move move,
			Square myKing,
			Square opponentsKing) {

		/*
		 * When black moves, we need to update the checkState of WHITE (since this tracks the squares which check the black
		 * king). Ditto when white moves, need to update BLACK's checkState.
		 */
		PositionCheckState state = checkState[Colour.oppositeColour(move.getColour()).ordinal()];
		Square relevantSquare;
		Ray rayToKing;

		// king move - reset state
		if (move.getPiece() == PieceType.KING) {
			state.reset();
		} else {

			//
			// process ray between my king and move.to()
			//
			relevantSquare = move.to();
			rayToKing = RayUtils.getRay(relevantSquare, myKing);
			if (rayToKing != null) {
				RayType rayType = rayToKing.getRayType();
				// update move.to() if was CHECK, to CHECK_CAPTURE
				if (state.squareHasCheckStatus(relevantSquare, rayType)) {
					state.setCheckIfCapture(rayType, relevantSquare);
				}
				// squares further away from king set to NO_CHECK
				rayToKing.getOpposite().streamSquaresFrom(relevantSquare).forEach(sq -> state.setToNotCheck(sq, rayType));
			}
			//
			// process ray between my king and move.from()
			//
			relevantSquare = move.from();
			rayToKing = RayUtils.getRay(relevantSquare, myKing);
			if (rayToKing != null) {
				RayType rayType = rayToKing.getRayType();
				// update move.from() if was CHECK_CAPTURE, to CHECK (the pice has now moved away)
				if (state.squareHasCheckIfCaptureStatus(relevantSquare, rayType)) {
					state.setCheck(rayType, relevantSquare);
				}
				// squares further away from king set to UNKNOWN
				rayToKing.getOpposite().streamSquaresFrom(relevantSquare).forEach(sq -> state.setToUnknownState(sq, rayType));
			}
		}
		//
		// ALSO need to update our own state. The square we've moved to should set other squares on the same ray
		// (but further from the opponent's king) to NOT_CHECK.
		PositionCheckState stateOfMovingSide = checkState[move.getColour().ordinal()];
		relevantSquare = move.from();
		rayToKing = RayUtils.getRay(relevantSquare, opponentsKing);
		if (rayToKing != null) {
			RayType rayType = rayToKing.getRayType();
			// squares further away from king set to UNKNOWN
			rayToKing.getOpposite().streamSquaresFrom(relevantSquare).forEach(sq -> stateOfMovingSide.setToUnknownState(sq, rayType));
		}
		relevantSquare = move.to();
		rayToKing = RayUtils.getRay(relevantSquare, opponentsKing);
		if (rayToKing != null) {
			RayType rayType = rayToKing.getRayType();
			// squares further away from king set to NO_CHECK
			rayToKing.getOpposite().streamSquaresFrom(relevantSquare).forEach(sq -> stateOfMovingSide.setToNotCheck(sq, rayType));
		}

		if (SystemFlags.DEBUG_CHECK_STATE) {
			examineCheckState(move);
		}
	}

	/**
	 * this is a debug method to make sure that all squares marked as 'check' in checkState do really check the opponent's
	 * king.
	 *
	 * @param move the current move
	 */
	private void examineCheckState(Move move) {
		Colour sideToMove = move.getColour();
		// if white is moving, want to check the black's checkState against the position of the white king
		Colour opponentsColour = Colour.oppositeColour(sideToMove);
		Square opponentsKingsSquare = getKingPosition(opponentsColour);
		PositionCheckState state = checkState[sideToMove.ordinal()];
		BitSetUnifier emptySquares = getEmptySquares();
		state.stream()
				.filter(x -> x.getRight().getState() != CheckStates.UNKNOWN)
				.forEach(x -> {
					Square startSquare = Square.fromBitIndex(x.getLeft());
					CheckInfo info = x.getRight();
					boolean isCheck;
					// special case: if ray is null, then state can only be NOT_CHECK. Need to check rook/bishop/queen
					// TODO: queen checks diagonal and rank/file, which leads to problems...
					if (info.getRayType() == null) {
						if (info.getState() != CheckStates.NOT_CHECK) {
							throw new IllegalStateException("null ray but checkstate != NOT_CHECK");
						}
						isCheck = Bishop.attacksSquare(emptySquares, startSquare, opponentsKingsSquare, new PositionCheckState(), false, false);
						if (!isCheck) {
							isCheck = Rook.attacksSquare(emptySquares, startSquare, opponentsKingsSquare, new PositionCheckState(), false, false);
						}
						if (!isCheck) {
							isCheck = Queen.attacksSquare(emptySquares, startSquare, opponentsKingsSquare, new PositionCheckState(), false, false);
						}

						isCheck = false; //TODO    workaround

					} else {
						if (info.getRayType().isDiagonal()) {
							// bishop/queen
							isCheck = SlidingPiece.attacksSquareDiagonally(emptySquares, startSquare, opponentsKingsSquare,
									new PositionCheckState(), false, false);
						} else {
							// rook/queen
							isCheck = SlidingPiece.attacksSquareRankOrFile(emptySquares, startSquare, opponentsKingsSquare,
									new PositionCheckState(), false, false);
						}
					}
					switch (info.getState()) {
					case NOT_CHECK:
						if (isCheck) {
							throw new IllegalStateException("move: " + move + ", square " + startSquare + " has wrong state: " + info + ", king: "
									+ opponentsKingsSquare + "\n" + state.toString() + "posn:\n" + this);
						}
						break;
					case CHECK:
					case CHECK_IF_CAPTURE:
						if (!isCheck) {
							throw new IllegalStateException("move: " + move + ", square " + startSquare + " has wrong state: " + info + ", king: "
									+ opponentsKingsSquare + "\n" + state.toString() + "posn:\n" + this);
						}
						break;
					case UNKNOWN:
						throw new IllegalStateException("cannot happen");
					}

				});

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
				sb.append("   ").append(sideToMove).append(" to move");
				break;
			case 6:
				sb.append("   castlingRights: ").append(castling[0]).append(", ").append(castling[1]);
				break;
			case 5:
				sb.append("   enpassant square: ").append(enpassantSquare);
				break;
			case 4:
				sb.append("   hash (zobrist): ").append(hashCode());
				break;
			default:
				break;
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Updates the given bitset to represent the move. The from and to squares will be flipped. If castling then the rook's
	 * move is also taken into a/c.
	 *
	 * @param bitset the bitset to be updated.
	 * @param move the move. NB only non-capture moves are supported by this method!
	 */
	private void updateBitSet(BitSetUnifier bitset,
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

	/**
	 * returns the bitset of all empty squares (logical NOT of {@link #getTotalPieces()}). The bitset will be created on
	 * first usage.
	 *
	 * @return the bitset of all empty squares.
	 */
	public BitSetUnifier getEmptySquares() {
		if (emptySquares == null) {
			emptySquares = getTotalPieces().flip();
		}
		return emptySquares;
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
	 * Test-access to PositionCheckState.
	 *
	 * @return the current check state
	 */
	PositionCheckState[] getCheckState() {
		return checkState;
	}

	/**
	 * Returns true if the given square is attacked by any opponent's pieces.
	 *
	 * @param targetSquare the square to consider
	 * @param opponentsColour the colour of the opponent
	 * @return true if this square is attacked by the opponent
	 */
	public boolean squareIsAttacked(Square targetSquare,
			Colour opponentsColour) {
		Piece[] opponentsPieces = getPieces(opponentsColour);
		// iterate over the pieces
		// TODO instead of treating queens separately, could 'merge' them with the rooks and the bishops
		BitSetUnifier emptySquares = getEmptySquares();
		for (PieceType type : PieceType.ALL_PIECE_TYPES) {
			Piece piece = opponentsPieces[type.ordinal()];
			if (piece != null && piece.attacksSquare(emptySquares, targetSquare)) {
				return true;
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
		BitSetUnifier emptySquares = (BitSetUnifier) posn.getEmptySquares().clone();// need a clone
		BitSetUnifier myPieces = posn.getAllPieces(colour).cloneBitSet();

		emptySquares.set(moveFromIndex);
		myPieces.clear(moveFromIndex);

		// 1) do not need to set 'move.to()' -- if we're moving on the same ray, then it will be check already
		// 2) can't get a discovered check from castling

		return discoveredCheck(colour, posn, emptySquares, myPieces, opponentsKing, move.from());
	}

	/**
	 * Finds a discovered check on the opponent's king after a move from square <code>moveFromSquare</code>.
	 *
	 * @param myColour my colour
	 * @param cb the chessboard -- required for pieceAt()
	 * @param emptySquares the empty squares
	 * @param myPieces BitSet of my pieces
	 * @param opponentsKingsSquare where the opponent's king is
	 * @param moveFromSquare the square where the piece moved from
	 * @return true if the move from square <code>moveFromSquare</code> leads to a discovered check on the king.
	 */
	public static boolean discoveredCheck(Colour myColour,
			Position cb,
			BitSetUnifier emptySquares,
			BitSetUnifier myPieces,
			Square opponentsKingsSquare,
			Square moveFromSquare) {
		// if moveFromSquare is on a ray to kingsSquare,
		// then inspect this ray for a checking bishop/queen/rook
		Ray ray = RayUtils.getRay(opponentsKingsSquare, moveFromSquare);
		if (ray != null) {
			// TODO optimization: only interested in my pieces here
			RayInfo info = RayUtils.findFirstPieceOnRay(myColour, emptySquares, myPieces, ray, opponentsKingsSquare.bitIndex());
			if (info.foundPiece() && (info.getColour() == myColour)) {
				PieceType firstPieceFound = cb.pieceAt(Square.fromBitIndex(info.getIndexOfPiece()), myColour);
				return ray.isRelevantPieceForDiscoveredCheck(firstPieceFound);
			}
		}
		return false;
	}

	/**
	 * Finds a discovered check on the opponent's king after a move from square <code>moveFromSquare</code>.
	 *
	 * @param kingsColour my colour
	 * @param cb the chessboard -- required for pieceAt()
	 * @param emptySquares the empty squares
	 * @param kingsColourPieces BitSet of the pieces of the king's colour
	 * @param kingsSquare where the opponent's king is
	 * @param moveFromSquare the square where the piece moved from
	 * @return true if the move from square <code>moveFromSquare</code> leads to a discovered check on the king.
	 */
	public static boolean kingInCheck(Colour kingsColour,
			Position cb,
			BitSetUnifier emptySquares,
			BitSetUnifier kingsColourPieces,
			Square kingsSquare,
			Square moveFromSquare) {
		// if moveFromSquare is on a ray to kingsSquare,
		// then inspect this ray for a checking bishop/queen/rook
		Ray ray = RayUtils.getRay(kingsSquare, moveFromSquare);
		if (ray != null) {
			RayInfo info = RayUtils.findFirstPieceOnRay(kingsColour, emptySquares, kingsColourPieces, ray, kingsSquare.bitIndex());
			if (info.foundPiece() && (info.getColour() != kingsColour)) {
				PieceType firstPieceFound = cb.pieceAt(Square.fromBitIndex(info.getIndexOfPiece()), Colour.oppositeColour(kingsColour));
				return ray.isRelevantPieceForDiscoveredCheck(firstPieceFound);
			}
		}
		return false;
	}

	/**
	 * Returns the bitsets of the pieces parameter.
	 *
	 * @param pieces
	 * @return a bitset array
	 */
	public static BitSetUnifier[] setupBitsets(Piece[] pieces) {
		BitSetUnifier[] enemyPieces = new BitSetUnifier[PieceType.ALL_PIECE_TYPES.length];
		for (PieceType type : PieceType.ALL_PIECE_TYPES) {
			enemyPieces[type.ordinal()] = pieces[type.ordinal()].getBitBoard().getBitSet();
		}
		return enemyPieces;
	}

	/**
	 * Finds the piece at the given square. TODO optimize using Lookup?
	 *
	 * @param targetSquare square to use
	 * @param expectedColour if not null, this piece's colour is expected.
	 * @return the piece at this location.
	 * @throws IllegalArgumentException if no piece [of the given colour] exists at the given square.
	 */
	public PieceType pieceAt(Square targetSquare,
			Colour expectedColour) {
		for (Colour colour : Colour.ALL_COLOURS) {
			if (expectedColour != null && colour != expectedColour) {
				continue;
			}
			for (PieceType type : PieceType.ALL_PIECE_TYPES) {
				Piece p = getPieces(colour)[type.ordinal()];
				// null == piece-type no longer on board
				if (p != null && p.pieceAt(targetSquare)) {
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
