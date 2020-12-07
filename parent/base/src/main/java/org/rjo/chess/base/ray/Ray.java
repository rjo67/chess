package org.rjo.chess.base.ray;

import java.util.Iterator;
import java.util.stream.Stream;

import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitBoard;

/**
 * A Ray describes a horizontal, vertical, or diagonal line between two squares.
 *
 * @author rich
 */
public interface Ray {

    /**
     * Returns an iterator returning all squares from startSquare (exclusive) to the end of the ray.
     *
     * @param startSquare start square
     * @return An iterator returning all squares from startSquare (exclusive) to the end of the ray.
     */
    Iterator<Integer> squaresFrom(Square startSquare);

    /**
     * Returns an iterator returning all squares from startSquare (exclusive) to the end of the ray.
     *
     * @param startSquareIndex bit index of start square
     * @return An iterator returning all squares from startSquare (exclusive) to the end of the ray.
     */
    Iterator<Integer> squaresFrom(int startSquareIndex);

    /**
     * Returns a stream of the squares from startSquare (exclusive) to the end of the ray.
     *
     * @param startSquare start square
     * @return A stream returning all squares from startSquare (exclusive) to the end of the ray.
     */
    Stream<Integer> streamSquaresFrom(Square startSquare);

    /**
     * Returns a stream of the squares from startSquare (exclusive) to the end of the ray.
     *
     * @param startSquareIndex bit index of start square
     * @return A stream returning all squares from startSquare (exclusive) to the end of the ray.
     */
    Stream<Integer> streamSquaresFrom(int startSquareIndex);

    boolean isRelevantPieceForDiscoveredCheck(PieceType piece);

    /**
     * returns the type of the ray
     *
     * @return the type of the ray
     */
    RayType getRayType();

    /**
     * True if this ray is the 'opposite' of the given ray, e.g. NW and SE, or N and S.
     *
     * @param ray the ray to check.
     * @return True if this ray is the 'opposite' of the given ray
     */
    boolean oppositeOf(Ray ray);

    /**
     * Return the 'opposite' ray to 'this', e.g. this==NW, will return SE. For this ray oppositeOf is true.
     */
    Ray getOpposite();

    /**
     * returns the 'attack' bitboard for the given square.
     *
     * @param sqIndex the square index
     * @return the 'attack' bitboard for the given square
     */
    BitBoard getAttackBitBoard(int sqIndex);

}
