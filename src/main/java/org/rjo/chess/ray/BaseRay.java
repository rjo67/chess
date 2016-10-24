package org.rjo.chess.ray;

import java.util.Iterator;

import org.rjo.chess.Square;
import org.rjo.chess.pieces.PieceType;

public abstract class BaseRay implements Ray {

	private static final Ray[] rays = new Ray[RayType.values().length];

	static {
		rays[RayType.NORTH.getIndex()] = NorthRay.instance();
		rays[RayType.NORTHEAST.getIndex()] = NorthEastRay.instance();
		rays[RayType.EAST.getIndex()] = EastRay.instance();
		rays[RayType.SOUTHEAST.getIndex()] = SouthEastRay.instance();
		rays[RayType.SOUTH.getIndex()] = SouthRay.instance();
		rays[RayType.SOUTHWEST.getIndex()] = SouthWestRay.instance();
		rays[RayType.WEST.getIndex()] = WestRay.instance();
		rays[RayType.NORTHWEST.getIndex()] = NorthWestRay.instance();
	}

	/**
	 * Returns the appropriate Ray class for the given raytype.
	 * 
	 * @param type ray type
	 * @return the matching Ray class
	 */
	public static Ray getRay(RayType type) {
		return rays[type.getIndex()];
	}

	private RayType rayType;
	private boolean diagonal;
	protected PieceType[] piecesThatCanGiveCheckOnThisRay;

	protected BaseRay(RayType rayType, boolean diagonal, PieceType[] pieceTypes) {
		this.rayType = rayType;
		this.diagonal = diagonal;
		this.piecesThatCanGiveCheckOnThisRay = pieceTypes;
	}

	@Override
	public RayType getRayType() {
		return rayType;
	}

	@Override
	public boolean isDiagonal() {
		return diagonal;
	}

	@Override
	public Iterator<Integer> squaresFrom(Square startSquare) {
		return squaresFrom(startSquare.bitIndex());
	}

	@Override
	public boolean isRelevantPieceForDiscoveredCheck(PieceType piece) {
		for (PieceType pt : piecesThatCanGiveCheckOnThisRay) {
			if (pt == piece) {
				return true;
			}
		}
		return false;
	}

}
