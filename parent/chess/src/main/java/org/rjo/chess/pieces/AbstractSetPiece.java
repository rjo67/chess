package org.rjo.chess.pieces;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.bits.BitSetUnifier;

/**
 * For pieces which aren't stored in a bitset.
 *
 * @author rich
 * @since 2017-08-18
 */
public abstract class AbstractSetPiece extends AbstractPiece {

	protected Set<Square> pieces;

	protected AbstractSetPiece(Colour colour, PieceType type) {
		super(colour, type);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		AbstractSetPiece newPiece = (AbstractSetPiece) super.clone();
		newPiece.pieces = new HashSet<>(this.pieces);
		return newPiece;
	}

	@Override
	public void initPosition(Square... squares) {
		pieces = new HashSet<>(3);
		if (squares != null) {
			pieces.addAll(Arrays.asList(squares));
		}
	}

	@Override
	public boolean pieceAt(Square targetSquare) {
		return pieces.contains(targetSquare);
	}

	@Override
	public Square[] getLocations() {
		return pieces.toArray(new Square[0]);
	}

	@Override
	public void addPiece(Square square) {
		pieces.add(square);
	}

	@Override
	public int numberOfPieces() {
		return pieces.size();
	}

	@Override
	public void removePiece(Square square) {
		boolean removed = pieces.remove(square);
		if (!removed) {
			throw new IllegalArgumentException("no " + this.getType() + " found on square " + square + ". Squares: " + pieces);
		}
	}

	@Override
	public void move(Move move) {
		boolean removed = pieces.remove(move.from());
		if (!removed) {
			throw new IllegalArgumentException("no " + this.getType() + " found on required square. Move: " + move + ", squares: " + pieces);
		}
		pieces.add(move.to());
	}

	protected static int pieceSquareValue(Set<Square> pieces,
			Colour colour,
			int pieceValue,
			int[] squareValue) {
		int value = 0;
		for (Square sq : pieces) {
			int sqValue;
			if (colour == Colour.WHITE) {
				sqValue = squareValue[sq.bitIndex()];
			} else {
				sqValue = squareValue[63 - sq.bitIndex()];
			}
			value += pieceValue + sqValue;
		}
		return value;
	}

	//
	// not nice to have these....
	//
	protected BitSetUnifier createBitSetOfPieces() {
		return getBitBoard().getBitSet();
	}

	@Override
	public BitBoard getBitBoard() {
		BitBoard bb = new BitBoard();
		for (Square sq : pieces) {
			bb.setBitsAt(sq);
		}
		return bb;
	}
}
