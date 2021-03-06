package org.rjo.chess.pieces;

import java.util.HashSet;
import java.util.Set;

import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.bits.BitSetUnifier;

/**
 * Base class for all pieces which use a BitBoard to store the piece locations.
 *
 * @author rich
 * @since 2016-10-20
 */
public abstract class AbstractBitBoardPiece extends AbstractPiece {

    /**
     * stores position of the piece(s) of a particular kind (queen, pawns, ...)
     */
    protected BitBoard pieces;

    protected AbstractBitBoardPiece(Colour colour, PieceType type) {
        super(colour, type);
        // pieces is set in initPosition
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        AbstractBitBoardPiece piece = (AbstractBitBoardPiece) super.clone();
        piece.pieces = new BitBoard(pieces);
        return piece;
    }

    @Override
    public void move(Move move) {
        if (!pieces.get(move.from().bitIndex())) {
            throw new IllegalArgumentException("no " + getType() + " found on square " + move.from() + ". Move: " + move);
        }
        pieces.clear(move.from().bitIndex());
        if (!move.isPromotion()) {
            pieces.set(move.to().bitIndex());
        }
    }

    @Override
    public void removePiece(Square square) {
        if (!pieces.get(square.bitIndex())) {
            throw new IllegalArgumentException("no " + getType() + " found on square " + square);
        }
        pieces.clear(square.bitIndex());
    }

    @Override
    public void addPiece(Square square) {
        pieces.set(square.bitIndex());
    }

    @Override
    public BitBoard getBitBoard() {
        return pieces;
    }

    @Override
    public void initPosition(Square... requiredSquares) {
        pieces = new BitBoard();
        if (requiredSquares != null) {
            pieces.setBitsAt(requiredSquares);
        }
    }

    @Override
    public int numberOfPieces() {
        return pieces.cardinality();
    }

    @Override
    public Square[] getLocations() {
        Set<Square> set = new HashSet<>();
        for (int i = pieces.getBitSet().nextSetBit(0); i >= 0; i = pieces.getBitSet().nextSetBit(i + 1)) {
            set.add(Square.fromBitIndex(i));
        }
        return set.toArray(new Square[0]);
    }

    @Override
    public boolean pieceAt(Square targetSquare) {
        return pieces.get(targetSquare.bitIndex());
    }

    /**
     * Calculates the piece-square value in centipawns. For each piece, its piece_value is added to the square_value of the
     * square where it currently is.
     *
     * @return the piece-square value in centipawns (for all pieces of this type).
     */
    @Override
    public abstract int calculatePieceSquareValue();

    public static int pieceSquareValue(final BitBoard pieces,
                                       final Colour colour,
                                       final int pieceValue,
                                       final int[] squareValue) {
        return pieceSquareValue(pieces.getBitSet(), colour, pieceValue, squareValue);
    }

    public static int pieceSquareValue(final BitSetUnifier piecesBitSet,
                                       final Colour colour,
                                       final int pieceValue,
                                       final int[] squareValue) {
        int value = 0;
        for (int i = piecesBitSet.nextSetBit(0); i >= 0; i = piecesBitSet.nextSetBit(i + 1)) {
            int sqValue;
            if (colour == Colour.WHITE) {
                sqValue = squareValue[i];
            } else {
                sqValue = squareValue[63 - i];
            }
            value += pieceValue + sqValue;
        }
        return value;
    }

}
