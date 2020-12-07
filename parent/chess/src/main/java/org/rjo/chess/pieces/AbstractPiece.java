package org.rjo.chess.pieces;

import org.rjo.chess.base.Colour;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitSetUnifier;
import org.rjo.chess.position.PositionCheckState;

/**
 * Stores the type and colour of a piece.
 *
 * @author rich
 * @since 2016-10-20
 */
public abstract class AbstractPiece implements Piece {

	// type of this piece
	protected PieceType type;

	// stores the colour of the piece
	protected Colour colour;

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
