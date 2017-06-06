package org.rjo.chess;

import java.util.BitSet;

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
		enemyPieces = Position.setupEnemyBitsets(chessboard.getPieces(opponentsColour));
		this.myKing = myKing;
	}

	public boolean isKingInCheck(
			Move move,
			boolean kingWasInCheck) {
		return KingCheck.isKingInCheckAfterMove(myKing, myColour, friendlyPieces, enemyPieces, move, kingWasInCheck);
	}
}
