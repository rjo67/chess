package org.rjo.chess;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.rjo.chess.pieces.Piece;
import org.rjo.chess.pieces.PieceType;

/**
 * Encapsulates the board, the moves, castling rights, etc (clocks?).
 *
 * @author rich
 */
public class Game {

	private Position position;
	/** stores the moves (ply) */
	private Deque<Move> moves;

	/** half-moves. Not used as yet. */
	private int halfmoveClock;

	/**
	 * move number of the next move. Not calculated from size of 'moves' since
	 * we don't have to start at move 1
	 */
	private int moveNbr;
	/**
	 * if the king (of the sideToMove) is currently in check. Normally deduced
	 * from the last move but can be set delibarately for tests.
	 */
	private boolean inCheck;

	// thread pool for findMove()
	private ExecutorService threadPool = Executors.newFixedThreadPool(PieceType.getPieceTypes().length);

	/**
	 * Constructs a game with the default start position.
	 */
	public Game() {
		position = new Position();
		init(EnumSet.allOf(CastlingRights.class), EnumSet.allOf(CastlingRights.class));
	}

	public Position getPosition() {
		return position;
	}

	/**
	 * Inits a game with the given chessboard. Castling rights are set to
	 * 'empty'.
	 *
	 * @param chessboard
	 *            the chessboard
	 */
	public Game(Position chessboard) {
		this.position = chessboard;
		init(EnumSet.noneOf(CastlingRights.class), EnumSet.noneOf(CastlingRights.class));
	}

	public int getHalfmoveClock() {
		return halfmoveClock;
	}

	public void setHalfmoveClock(int halfmoveClock) {
		this.halfmoveClock = halfmoveClock;
	}

	@SuppressWarnings("unchecked")
	private void init(EnumSet<CastlingRights> whiteCastlingRights, EnumSet<CastlingRights> blackCastlingRights) {
		moves = new ArrayDeque<>();

		moveNbr = 1;
	}

	public int getMoveNumber() {
		return moveNbr;
	}

	/**
	 * Sets the move number.
	 */
	public void setMoveNumber(int moveNbr) {
		this.moveNbr = moveNbr;
	}

	/**
	 * Just for tests: indicate that in this position the king of the side to
	 * move is in check.
	 *
	 * @param inCheck
	 *            true when the king is in check.
	 */
	public void setInCheck(boolean inCheck) {
		this.inCheck = inCheck;
	}

	public boolean isInCheck() {
		return inCheck;
	}

	public Position getChessboard() {
		return position;
	}

	/**
	 * Find all moves for the given colour from the current position.
	 *
	 * @param colour
	 *            the required colour
	 * @return all moves for this colour.
	 */
	public List<Move> findMoves(Colour colour) {
		// return findMovesParallel(colour);
		List<Move> moves = new ArrayList<>(60);
		for (PieceType type : PieceType.getPieceTypes()) {
			Piece p = position.getPieces(colour).get(type);
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
					Piece p = position.getPieces(colour).get(type);
					return p.findMoves(Game.this, inCheck);
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
			// TODO Auto-generated catch block
			throw new RuntimeException("got InterruptedException from future (findMovesParallel)", e);
		} catch (ExecutionException e) {
			throw new RuntimeException("got ExecutionException from future (findMovesParallel)", e);
		}

		return moves;
	}

	// /**
	// * Reverses the given move. Version without debug info.
	// *
	// * @param move
	// * the move
	// */
	// public void unmove(Move move) {
	// unmove(move, null);
	// }

	// /**
	// * Reverses the given move.
	// *
	// * @param move
	// * the move
	// * @param debugWriter
	// * if not null, debug info will be written here
	// */
	// public void unmove(Move move, Writer debugWriter) {
	// if (move.getColour() == sideToMove) {
	// throw new IllegalArgumentException("unmove for '" + move.getColour() + "'
	// was unexpected");
	// }
	//
	// PieceType movingPiece = move.getPiece();
	//
	// if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
	// Move rooksMove = move.getRooksCastlingMove();
	// chessboard.getPieces(move.getColour()).get(movingPiece).unmove(move);
	// chessboard.getPieces(move.getColour()).get(PieceType.ROOK).unmove(rooksMove);
	// // castling rights are reset later on
	// } else {
	// if (!move.isCapture() &&
	// !chessboard.getEmptySquares().getBitSet().get(move.from().bitIndex())) {
	// throw new IllegalArgumentException("square " + move.from() + " is not
	// empty. Unmove=" + move);
	// }
	// // update structures for the moving piece
	// chessboard.getPieces(move.getColour()).get(movingPiece).unmove(move);
	// // capture: add the captured piece
	// if (move.isCapture()) {
	// if (move.isEnpassant()) {
	// chessboard.getPieces(Colour.oppositeColour(move.getColour())).get(move.getCapturedPiece())
	// .addPiece(Square.findMoveFromEnpassantSquare(move.to()));
	// } else {
	// chessboard.getPieces(Colour.oppositeColour(move.getColour())).get(move.getCapturedPiece())
	// .addPiece(move.to());
	// }
	// }
	// // promotion: remove the promoted piece
	// if (move.isPromotion()) {
	// chessboard.getPieces(move.getColour()).get(move.getPromotedPiece()).removePiece(move.to());
	// }
	// }
	//
	// chessboard.updateStructures(move);
	//
	// // reset castling rights if necessary
	// // if ((PieceType.KING == move.getPiece()) || (PieceType.ROOK ==
	// // move.getPiece())) {
	// if (move.previousCastlingRightsWasSet()) {
	// castling[move.getColour().ordinal()] = move.getPreviousCastlingRights();
	// // writeDebug(debugWriter, "unmove: " + move + ", sideToMove: " +
	// // move.getColour() + ", castling="
	// // + castling[move.getColour().ordinal()]);
	// }
	// if (move.previousCastlingRightsOpponentWasSet()) {
	// castling[Colour.oppositeColour(move.getColour()).ordinal()] =
	// move.getPreviousCastlingRightsOpponent();
	// // writeDebug(debugWriter, "unmove: " + move + ", sideToMove: " +
	// // Colour.oppositeColour(move.getColour())
	// // + ", opponents castling=" +
	// // castling[Colour.oppositeColour(move.getColour()).ordinal()]);
	// }
	// // undoing black's move means that black should now move
	// setSideToMove(move.getColour());
	//
	// // pollLast instead of removeLast to avoid exception
	// Move lastMove = this.moves.pollLast();
	// // check if the 'new' last move was a check
	// inCheck = (lastMove != null) ? lastMove.isCheck() : false;
	//
	// if (lastMove != null && lastMove.isEnpassant()) {
	// chessboard.setEnpassantSquare(lastMove.to());
	// } else {
	// chessboard.setEnpassantSquare(null);
	// }
	//
	// if (Colour.BLACK == sideToMove) {
	// moveNbr--;
	// }
	// }
}
