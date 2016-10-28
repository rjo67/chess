package org.rjo.chess;

import java.util.BitSet;

import org.rjo.chess.pieces.Piece;
import org.rjo.chess.pieces.PieceType;

/**
 * Attempts to optimize the test whether a king is in check, by storing data structures which don't change.
 *
 * @author rich
 */
public class KingChecker {
	private BitSet friendlyPieces;
	private BitSet[] enemyPieces;
	private Square myKing;
	private Colour myColour;

	public KingChecker(Position chessboard, Colour opponentsColour, Square myKing) {
		this.myColour = Colour.oppositeColour(opponentsColour);
		friendlyPieces = chessboard.getAllPieces(myColour).getBitSet();
		enemyPieces = setupEnemyBitsets(chessboard.getPieces(opponentsColour));
		this.myKing = myKing;
	}

	// TODO check this seems to be duplicated in Position
	private static BitSet[] setupEnemyBitsets(Piece[] pieces) {
		BitSet[] enemyPieces = new BitSet[PieceType.ALL_PIECE_TYPES.length];
		for (PieceType type : PieceType.ALL_PIECE_TYPES) {
			enemyPieces[type.ordinal()] = pieces[type.ordinal()].getBitBoard().getBitSet();
		}
		return enemyPieces;
	}

	public boolean isKingInCheck(Move move, boolean kingWasInCheck) {
		return KingCheck.isKingInCheckAfterMove(myKing, myColour, friendlyPieces, enemyPieces, move, kingWasInCheck);
	}
}
