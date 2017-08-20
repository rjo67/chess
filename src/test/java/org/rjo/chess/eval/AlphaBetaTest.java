package org.rjo.chess.eval;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.rjo.chess.Fen;
import org.rjo.chess.Game;

public class AlphaBetaTest {

	@Test
	public void alphabetaWIP() {
		Game game = Fen.decode("8/5n2/8/8/pk6/4K3/P3NP2/8 w - - 0 15");
		SearchStrategy strat = new AlphaBeta(game.getZobristMap());
		MoveInfo m = strat.findMove(game.getPosition());
		assertEquals("Qg1xg7+", m.getMove().toString());
	}
}
