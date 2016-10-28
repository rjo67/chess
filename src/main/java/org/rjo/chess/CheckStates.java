package org.rjo.chess;

/**
 * Stores possible values for the 'check' Cache, used in Position::findMove.
 *
 * @author rich
 * @since 2016-10-28
 */
public enum CheckStates {

	/**
	 * no check from this square.
	 */
	NOT_CHECK,
	/**
	 * check from this square.
	 */
	CHECK,
	/**
	 * check from this square if the piece on it is captured.
	 */
	CHECK_IF_CAPTURE;
}
