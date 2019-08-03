package org.rjo.chess.pieces;

import java.util.List;

import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.bits.BitSetUnifier;
import org.rjo.chess.pieces.PieceManager.Pieces;
import org.rjo.chess.position.Position;
import org.rjo.chess.position.PositionCheckState;
import org.rjo.chess.position.PositionInfo;

/**
 * A 'dummy' Pawn piece to transmit information. Should not be used to calculate moves etc. Instead use {@link Pawns}.
 *
 * @author rich
 * @since 2019-08-03
 */
public class Pawn extends AbstractPiece {

	public Pawn(Colour colour, Square location) {
		super(colour, PieceType.PAWN, location);
	}

	@Override
	public List<Move> findMoves(Position position,
			boolean kingInCheck,
			PositionInfo posnInfo) {
		throw new IllegalStateException("do not call");
	}

	@Override
	public boolean doesMoveLeaveOpponentInCheck(Move move,
			Pieces pieces,
			Square opponentsKing,
			BitBoard[] checkingBitboards) {
		throw new IllegalStateException("do not call");
	}

	@Override
	public Piece attacksSquare(BitSetUnifier emptySquares,
			Square targetSq,
			PositionCheckState checkCache) {
		throw new IllegalStateException("do not call");
	}

	@Override
	public int calculatePieceSquareValue() {
		throw new IllegalStateException("do not call");
	}

}
