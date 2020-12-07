package org.rjo.chess.pieces;

import java.util.List;

import org.rjo.chess.base.Move;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.position.Fen;
import org.rjo.chess.position.Game;

public class AbstractMoveTest {

	protected Game game;

	protected void setupGame(String fen) {
		game = Fen.decode(fen);
	}

	private List<Move> findAndFilterMoves(PieceType pieceType) {
		List<Move> moves = game.getPosition().findMoves(game.getPosition().getSideToMove());
		moves.removeIf(m -> m.getPiece() != pieceType);
		return moves;
	}

	protected List<Move> findKnightMoves() {
		return findAndFilterMoves(PieceType.KNIGHT);
	}

	protected List<Move> findBishopMoves() {
		return findAndFilterMoves(PieceType.BISHOP);
	}

	protected List<Move> findKingMoves() {
		return findAndFilterMoves(PieceType.KING);
	}

	protected List<Move> findQueenMoves() {
		return findAndFilterMoves(PieceType.QUEEN);
	}

	protected List<Move> findRookMoves() {
		return findAndFilterMoves(PieceType.ROOK);
	}

	protected List<Move> findPawnMoves() {
		return findAndFilterMoves(PieceType.PAWN);
	}
}