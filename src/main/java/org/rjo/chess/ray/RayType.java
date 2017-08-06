package org.rjo.chess.ray;

public enum RayType {
	NORTH(0, "N "), NORTHEAST(1, "NW"), EAST(2, "E "), SOUTHEAST(3, "SE"), SOUTH(4, "S "), SOUTHWEST(5, "SW"), WEST(6, "W "), NORTHWEST(7,
			"NW");

	public static final RayType[] RAY_TYPES_DIAGONAL = new RayType[] { RayType.SOUTHEAST, RayType.SOUTHWEST, RayType.NORTHEAST,
			RayType.NORTHWEST };
	public static final RayType[] RAY_TYPES_VERTICAL = new RayType[] { RayType.NORTH, RayType.SOUTH };
	public static final RayType[] RAY_TYPES_HORIZONTAL = new RayType[] { RayType.WEST, RayType.EAST };

	private int index;
	private String abbreviation;

	RayType(int index, String abbrev) {
		this.index = index;
		this.abbreviation = abbrev;
	}

	public int getIndex() {
		return index;
	}

	public String getAbbreviation() {
		return abbreviation;
	}
}
