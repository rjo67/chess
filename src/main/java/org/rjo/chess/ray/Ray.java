package org.rjo.chess.ray;

import java.util.Iterator;

import org.rjo.chess.Square;
import org.rjo.chess.pieces.PieceType;

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
	public Iterator<Integer> squaresFrom(Square startSquare);

	/**
	 * Returns an iterator returning all squares from startSquare (exclusive) to the end of the ray.
	 *
	 * @param startSquareIndex bit index of start square
	 * @return An iterator returning all squares from startSquare (exclusive) to the end of the ray.
	 */
	public Iterator<Integer> squaresFrom(int startSquareIndex);

	public boolean isRelevantPieceForDiscoveredCheck(PieceType piece);

	/**
	 * returns the type of the ray
	 *
	 * @return the type of the ray
	 */
	public RayType getRayType();

	/**
	 * True if this ray is a diagonal ray.
	 *
	 * @return True if this ray is a diagonal ray.
	 */
	public boolean isDiagonal();

	/**
	 * True if this ray is the 'opposite' of the given ray, e.g. NW and SE, or N and S.
	 * 
	 * @param ray the ray to check.
	 * @return True if this ray is the 'opposite' of the given ray
	 */
	public boolean oppositeOf(Ray ray);
}
