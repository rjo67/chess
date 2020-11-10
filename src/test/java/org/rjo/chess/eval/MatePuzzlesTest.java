package org.rjo.chess.eval;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.rjo.chess.Fen;
import org.rjo.chess.Game;

public class MatePuzzlesTest {

	@Test
	@Disabled // too slow
	public void mateInFour() {
		Game game = Fen.decode("4k2r/1R3R2/p3p1pp/4b3/1BnNr3/8/P1P5/5K2 w - - 1 0");
		SearchStrategy strat = new AlphaBeta3(System.out);
		strat.incrementDepth(3);
		System.out.println(strat.getCurrentDepth());
		MoveInfo mi = strat.findMove(game.getPosition());
		System.out.println(mi);
	}

	@Test
	@Disabled // too slow
	public void mateInThree() {
		Game game = Fen.decode("4r1k1/3n1ppp/4r3/3n3q/Q2P4/5P2/PP2BP1P/R1B1R1K1 b - - 0 1");
		SearchStrategy strat = new AlphaBeta3(System.out);
		strat.incrementDepth(3);
		System.out.println(strat.getCurrentDepth());
		MoveInfo mi = strat.findMove(game.getPosition());
		System.out.println(mi);
	}

	@Test
	public void mateInTwoA() {
		Game game = Fen.decode("4r1k1/pQ3pp1/7p/4q3/4r3/P7/1P2nPPP/2BR1R1K b - - 0 1");
		SearchStrategy strat = new AlphaBeta3(System.out);
		System.out.println(strat.getCurrentDepth());
		MoveInfo mi = strat.findMove(game.getPosition());
		System.out.println(mi);
	}

	@Test
	public void mateInTwoB() {
		Game game = Fen.decode("r4R2/1b2n1pp/p2Np1k1/1pn5/4pP1P/8/PPP1B1P1/2K4R w - - 1 0");
		SearchStrategy strat = new AlphaBeta3(System.out);
		System.out.println(strat.getCurrentDepth());
		MoveInfo mi = strat.findMove(game.getPosition());
		System.out.println(mi);
	}

}
