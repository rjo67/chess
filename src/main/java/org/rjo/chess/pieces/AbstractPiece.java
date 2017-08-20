package org.rjo.chess.pieces;

import java.util.List;

import org.rjo.chess.Colour;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.Position;
import org.rjo.chess.PositionCheckState;
import org.rjo.chess.Square;
import org.rjo.chess.util.BitSetUnifier;

/**
 * Stores the type and colour of a piece.
 *
 * @author rich
 * @since 2016-10-20
 */
/**
 * @author rich
 * @since 2016-10-21
 */
public abstract class AbstractPiece implements Piece {

	// type of this piece
	private PieceType type;

	// stores the colour of the piece
	private Colour colour;

	protected AbstractPiece(Colour colour, PieceType type) {
		this.colour = colour;
		this.type = type;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		AbstractPiece newPiece = (AbstractPiece) super.clone();
		newPiece.type = this.type;
		newPiece.colour = this.colour;
		return newPiece;
	}

	/**
	 * @return the symbol for this piece.
	 */
	@Override
	public String getSymbol() {
		return type.getSymbol();
	}

	/**
	 * Finds all possible moves for this piece type in the given position. Delegates to {@link #findMoves(Game, boolean)}
	 * with 2nd parameter FALSE.
	 *
	 * @param position current game state.
	 * @return a list of all possible moves.
	 */
	@Override
	public final List<Move> findMoves(Position position) {
		return findMoves(position, false);
	}

	@Override
	public Colour getColour() {
		return colour;
	}

	/**
	 * Returns the FEN symbol for this piece. Delegates to {@link PieceType#getFenSymbol(Colour)}.
	 *
	 * @return the FEN symbol for this piece.
	 */
	@Override
	public String getFenSymbol() {
		return type.getFenSymbol(colour);
	}

	@Override
	public String toString() {
		return colour.toString() + "-" + type.toString() + "@" + Integer.toHexString(System.identityHashCode(this));
	}

	@Override
	public PieceType getType() {
		return type;
	}

	@Override
	public final boolean attacksSquare(BitSetUnifier emptySquares,
			Square targetSq) {
		return attacksSquare(emptySquares, targetSq, new PositionCheckState());
	}

}
