package org.rjo.chess;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.apache.commons.lang3.tuple.Pair;
import org.rjo.chess.ray.RayType;
import org.rjo.chess.util.SquareCache;

/**
 * Stores information about which squares in a position lead to check on the opponent's king.
 *
 * @author rich
 * @since 2017-07-23
 */
public final class PositionCheckState {

	final static class CheckInfo {
		private static final CheckInfo DEFAULT = new CheckInfo(CheckStates.UNKNOWN, null);
		private CheckStates state;
		private RayType rayType;

		/**
		 * Constructor.
		 *
		 * @param state 'check state' of square
		 * @param ray ray to king (can be null).
		 */
		CheckInfo(CheckStates state, RayType rayType) {
			this.state = state;
			if ((rayType == null) && ((state == CheckStates.CHECK) || (state == CheckStates.CHECK_IF_CAPTURE))) {
				throw new IllegalArgumentException("ray cannot be null for state: " + state);
			}
			this.rayType = rayType;
		}

		public CheckStates getState() {
			return state;
		}

		public RayType getRayType() {
			return rayType;
		}

		@Override
		public String toString() {
			switch (state) {
			case CHECK:
				return rayType.getAbbreviation() + ((rayType.getAbbreviation().length() == 1) ? "_" : "") + "+";
			case CHECK_IF_CAPTURE:
				return rayType.getAbbreviation() + ((rayType.getAbbreviation().length() == 1) ? "_" : "") + "C";
			case NOT_CHECK:
				return "_-_";
			case UNKNOWN:
				return "-?-";
			default:
				return "!!!";
			}
		}
	}

	private SquareCache<CheckInfo> checkCache;

	/**
	 * default constructor.
	 */
	public PositionCheckState() {
		this.checkCache = new SquareCache<>(CheckInfo.DEFAULT);
	}

	/**
	 * copy constructor.
	 *
	 * @param copy the object to copy
	 */
	public PositionCheckState(PositionCheckState copy) {
		this.checkCache = new SquareCache<>(copy.checkCache);
	}

	/**
	 * Reset the cache to the default.
	 */
	public void reset() {
		checkCache.reset();
	}

	//
	// -------------------------------------------
	// methods to set the state of a square
	// -------------------------------------------
	//

	/**
	 * Sets the given square to NOT_CHECK.
	 *
	 * @param rayType the required raytype (can be null)
	 * @param square a square
	 */
	public void setNotCheck(RayType rayType,
			Square square) {
		setNotCheck(rayType, square.bitIndex());
	}

	/**
	 * Sets the given square to NOT_CHECK.
	 *
	 * @param rayType the required raytype (can be null)
	 * @param square a square
	 */
	public void setNotCheck(RayType rayType,
			Integer squareBitIndex) {
		store(squareBitIndex, CheckStates.NOT_CHECK, rayType);
	}

	/**
	 * Sets all the given squares to NOT_CHECK.
	 *
	 * @param rayType the required ray (can be null)
	 * @param squares list of one or more squares
	 */
	public void setNotCheck(RayType rayType,
			List<Integer> squares) {
		squares.stream().forEach(square -> this.store(square, CheckStates.NOT_CHECK, rayType));
	}

	/**
	 * Sets the given square to CHECK_IF_CAPTURE.
	 *
	 * @param rayType the required ray
	 * @param square a square
	 */
	public void setCheckIfCapture(RayType rayType,
			Square square) {
		this.store(square.bitIndex(), CheckStates.CHECK_IF_CAPTURE, rayType);
	}

	/**
	 * Sets the given square to CHECK.
	 *
	 * @param rayType the required ray
	 * @param square a square
	 */
	public void setCheck(RayType rayType,
			Square square) {
		store(square.bitIndex(), CheckStates.CHECK, rayType);
	}

	/**
	 * Sets all the given squares to CHECK.
	 *
	 * @param ray the required ray
	 * @param squares list of one or more squares
	 */
	public void setCheck(RayType rayType,
			List<Integer> squares) {
		squares.stream().forEach(square -> this.store(square, CheckStates.CHECK, rayType));
	}

	/**
	 * Resets the state of the square (if it was check/checkifcapture, goes to NOT_CHECK).
	 * <p>
	 * <B>If state was unknown before, it is not changed.</B>
	 *
	 * @param squareIndex bitindex of square
	 * @param rayType ray type
	 */
	public void setToNotCheck(Integer squareBitIndex,
			RayType rayType) {
		if (isCheckStatusKnownForSquare(squareBitIndex, rayType)) {
			setNotCheck(rayType, squareBitIndex);
		}
	}

	/**
	 * Sets the state of the square to UNKNOWN.
	 *
	 * @param squareIndex bitindex of square
	 * @param rayType ray type
	 */
	public void setToUnknownState(Integer squareBitIndex,
			RayType rayType) {
		store(squareBitIndex, CheckStates.UNKNOWN, rayType);
	}

	//
	// -------------------------------------------
	// methods to query the state of a square
	// -------------------------------------------
	//
	public CheckStates getCheckState(int squareBitIndex,
			RayType rayType) {
		return lookup(squareBitIndex, rayType).getState();
	}

	/**
	 * Returns true if we know the status of <code>square</code>.
	 *
	 * @param square the square to check
	 * @param rayType the info stored must match with this ray
	 * @return true if we've processed this square/ray
	 */
	public boolean isCheckStatusKnownForSquare(Square square,
			RayType rayType) {
		return isCheckStatusKnownForSquare(square.bitIndex(), rayType);
	}

	/**
	 * Returns true if we know the status of the square defined by <code>bitIndex</code>.
	 *
	 * @param bitIndex the bitIndex of the square to check
	 * @param rayType the info stored must match with this ray
	 * @return true if we've processed this square/ray
	 */
	public boolean isCheckStatusKnownForSquare(int bitIndex,
			RayType rayType) {
		CheckInfo info = lookup(bitIndex, rayType);
		return info.getState() != CheckStates.UNKNOWN;
	}

	/**
	 * Returns true if the status of <code>square</code> is check or checkifcapture.
	 *
	 * @param square the square to check
	 * @param rayType the raytype
	 * @return true if this square has a check or checkifcapture status
	 */
	public boolean squareHasCheckStatus(Square square,
			RayType rayType) {
		return (lookup(square.bitIndex(), rayType).getState() == CheckStates.CHECK)
				|| (lookup(square.bitIndex(), rayType).getState() == CheckStates.CHECK_IF_CAPTURE);
	}

	/**
	 * Returns true if the status of <code>square</code> is checkifcapture.
	 *
	 * @param square the square to check
	 * @param rayType the raytype
	 * @return true if this square has a checkifcapture status
	 */
	public boolean squareHasCheckIfCaptureStatus(Square square,
			RayType rayType) {
		return lookup(square.bitIndex(), rayType).getState() == CheckStates.CHECK_IF_CAPTURE;
	}

	public Stream<Pair<Integer, CheckInfo>> stream() {
		Builder<Pair<Integer, CheckInfo>> sb = Stream.builder();
		for (int i = 0; i < 64; i++) {
			sb.accept(Pair.of(i, lookup(i, null)));
		}

		return sb.build();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(150);

		for (int rank = 7; rank >= 0; rank--) {
			for (int file = 0; file < 8; file++) {
				sb.append(checkCache.lookup(rank * 8 + file)).append(" ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	//
	// ----------------------------------------------------------------------
	//

	/**
	 * Returns the checkinfo for the given bitindex, if the ray matches. Otherwise CheckInfo.DEFAULT.
	 *
	 * @param squareBitIndex the bit index
	 * @param rayType the required ray type
	 * @return the checkinfo if the ray matches or is null, otherwise CheckInfo.DEFAULT
	 */
	private CheckInfo lookup(int squareBitIndex,
			RayType rayType) {
		CheckInfo info = checkCache.lookup(squareBitIndex);
		if ((rayType == null) || (info.rayType == rayType)) {
			return info;
		} else {
			return CheckInfo.DEFAULT;
		}
	}

	private void store(int squareBitIndex,
			CheckStates state,
			RayType rayType) {
		checkCache.store(squareBitIndex, new CheckInfo(state, rayType));
	}

}
