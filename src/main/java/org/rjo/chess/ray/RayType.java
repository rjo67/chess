package org.rjo.chess.ray;

public enum RayType {
	NORTH(0, false, true, "N"), //
	NORTHEAST(1, true, true, "NE"), //
	EAST(2, false, true, "E"), //
	SOUTHEAST(3, true, false, "SE"), //
	SOUTH(4, false, false, "S"), //
	SOUTHWEST(5, true, false, "SW"), //
	WEST(6, false, false, "W"), //
	NORTHWEST(7, true, true, "NW");

	public static final RayType[] RAY_TYPES_DIAGONAL = new RayType[] { RayType.SOUTHEAST, RayType.SOUTHWEST, RayType.NORTHEAST,
			RayType.NORTHWEST };
	public static final RayType[] RAY_TYPES_VERTICAL = new RayType[] { RayType.NORTH, RayType.SOUTH };
	public static final RayType[] RAY_TYPES_HORIZONTAL = new RayType[] { RayType.WEST, RayType.EAST };

	private int index;
	private boolean diagonal;
	// if true, the next bit on the ray has a larger index than the previous i.e. use nextSetBit to find it (e.g. NW, N, NE, E)
	// if false, use prevSetBit.
	private boolean bitIndicesIncrease;
	private String abbreviation;

	RayType(int index, boolean diagonal, boolean bitIndicesIncrease, String abbrev) {
		this.index = index;
		this.diagonal = diagonal;
		this.bitIndicesIncrease = bitIndicesIncrease;
		this.abbreviation = abbrev;
	}

	public int getIndex() {
		return index;
	}

	public boolean isBitIndicesIncrease() {
		return bitIndicesIncrease;
	}

	public boolean isDiagonal() {
		return diagonal;
	}

	public String getAbbreviation() {
		return abbreviation;
	}
}
