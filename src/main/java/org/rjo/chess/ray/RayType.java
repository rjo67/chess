package org.rjo.chess.ray;

public enum RayType {
	NORTH(0), NORTHEAST(1), EAST(2), SOUTHEAST(3), SOUTH(4), SOUTHWEST(5), WEST(6), NORTHWEST(7);

	public static final RayType[] RAY_TYPES_DIAGONAL = new RayType[] { RayType.SOUTHEAST, RayType.SOUTHWEST,
			RayType.NORTHEAST, RayType.NORTHWEST };
	public static final RayType[] RAY_TYPES_VERTICAL = new RayType[] { RayType.NORTH, RayType.SOUTH };
	public static final RayType[] RAY_TYPES_HORIZONTAL = new RayType[] { RayType.WEST, RayType.EAST };

	private int index;

	RayType(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}
