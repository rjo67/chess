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

	static {
		setOpposites(NORTH, SOUTH);
		setOpposites(NORTHWEST, SOUTHEAST);
		setOpposites(NORTHEAST, SOUTHWEST);
		setOpposites(WEST, EAST);
	}

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
	private RayType opposite;

	RayType(int index, boolean diagonal, boolean bitIndicesIncrease, String abbrev) {
		this.index = index;
		this.diagonal = diagonal;
		this.bitIndicesIncrease = bitIndicesIncrease;
		this.abbreviation = abbrev;
	}

	public int getIndex() {
		return index;
	}

	/**
	 * @return true if the next index in a bitset along this ray would be greater than the current index.
	 */
	public boolean isBitIndicesIncrease() {
		return bitIndicesIncrease;
	}

	/**
	 * @return true if this ray is a diagonal ray.
	 */
	public boolean isDiagonal() {
		return diagonal;
	}

	/**
	 * @return the ray's abbreviation
	 */
	public String getAbbreviation() {
		return abbreviation;
	}

	/**
	 * @return the opposite raytype e.g. the opposite of NW is SE.
	 */
	public RayType getOpposite() {
		return opposite;
	}

	/**
	 * private helper to set up the opposing rays. Must be done in a static block since cannot reference the other ray
	 * before it is defined.
	 */
	private static void setOpposites(RayType ray1,
			RayType ray2) {
		ray1.opposite = ray2;
		ray2.opposite = ray1;
	}

}
