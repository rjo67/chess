package org.rjo.chess.ray;

public enum RayType {
	NORTH(0, false, "N"), NORTHEAST(1, true, "NE"), EAST(2, false, "E"), SOUTHEAST(3, true, "SE"), SOUTH(4, false, "S"), SOUTHWEST(5, true,
			"SW"), WEST(6, false, "W"), NORTHWEST(7, true, "NW");

	public static final RayType[] RAY_TYPES_DIAGONAL = new RayType[] { RayType.SOUTHEAST, RayType.SOUTHWEST, RayType.NORTHEAST,
			RayType.NORTHWEST };
	public static final RayType[] RAY_TYPES_VERTICAL = new RayType[] { RayType.NORTH, RayType.SOUTH };
	public static final RayType[] RAY_TYPES_HORIZONTAL = new RayType[] { RayType.WEST, RayType.EAST };

	private int index;
	private boolean diagonal;
	private String abbreviation;

	RayType(int index, boolean diagonal, String abbrev) {
		this.index = index;
		this.diagonal = diagonal;
		this.abbreviation = abbrev;
	}

	public int getIndex() {
		return index;
	}

	public boolean isDiagonal() {
		return diagonal;
	}

	public String getAbbreviation() {
		return abbreviation;
	}
}
