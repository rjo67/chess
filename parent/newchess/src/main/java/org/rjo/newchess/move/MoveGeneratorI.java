package org.rjo.newchess.move;

import java.util.List;

import org.rjo.newchess.game.Position;
import org.rjo.newchess.piece.Colour;

/**
 * Simple interface for MoveGenerator, to enable other implementations to be plugged in.
 * 
 * @author rich
 */
public interface MoveGeneratorI {
	List<Move> findMoves(Position posn, Colour colour);

}
