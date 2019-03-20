package org.rjo.chess;

/**
 * Collection of the various system flags.
 *
 * @author rich
 * @since 2017-08-13
 */
public class SystemFlags {

	private SystemFlags() {
	}

	/**
	 * if TRUE, re-calculate the zobrist hash in order to check that the update function works.
	 * <p>
	 * Is a good check for the Zobrist function, however it does slow things down dramatically.
	 */
	public static final boolean CHECK_HASH_UPDATE_AFTER_MOVE = Boolean.parseBoolean(System.getProperty("checkZobristHash", "false"));

	/**
	 * if TRUE, uses the Zobrist map (in order to discard already-seen positions during evaluation). Otherwise not.
	 */
	public static final boolean USE_ZOBRIST = Boolean.parseBoolean(System.getProperty("useZobrist", "true"));

	/**
	 * if TRUE, the pre-calculated data structures moveMap, vertMoveMap are used (in Rook.java). Otherwise the ray algorithm
	 * will be used.
	 */
	public static final boolean USE_MOVE_MAP = Boolean.parseBoolean(System.getProperty("useMoveMap", "false"));

	/** if TRUE, the 'checkState' will be examined after a move to make sure everything's correct */
	public static final boolean DEBUG_CHECK_STATE = false;

	/** whether to cache the check state (buggy) */
	public static final boolean USE_CHECK_STATE = false;

}
