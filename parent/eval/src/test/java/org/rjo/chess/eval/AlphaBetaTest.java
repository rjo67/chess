package org.rjo.chess.eval;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.rjo.chess.Game;
import org.rjo.chess.base.eval.MoveInfo;
import org.rjo.chess.position.Fen;

public class AlphaBetaTest {

	@Test
	public void alphabetaWIP() {
		Game game = Fen.decode("8/5n2/8/8/pk6/4K3/P3NP2/8 w - - 0 15");
		SearchStrategy strat = new AlphaBeta3(System.out);
		strat.incrementDepth(2);
		MoveInfo m = strat.findMove(game.getPosition());
		assertEquals("Ke3-d3", m.getMove().toString());
	}

	@Test
	public void badMove() {
		Game game = Fen.decode("rnbqk1nr/pppp1ppp/8/3P4/8/2N5/PP1QPPPP/R3KBNR b KQkq - 2 7");
		SearchStrategy strat = new AlphaBeta3(System.out);
		strat.incrementDepth(1);
		//		AlphaBeta3.ORDER_MOVES = false;
		MoveInfo m = strat.findMove(game.getPosition());
		assertEquals("Qd8-f6", m.getMove().toString());
	}

	// mate in 1 Qc1-h6  posns evaluated:25191
	// with move ordering: posns evaluated 8821
	// depth 5: posns evaluated 83751
	@Test
	public void mateInOne() {
		Game game = Fen.decode("4r1k1/3R2pp/2N3p1/2p5/6PK/r7/6P1/2q5 b - - 67 34");
		SearchStrategy strat = new AlphaBeta3(System.out);
		strat.incrementDepth(1);
		MoveInfo m = strat.findMove(game.getPosition());
		assertEquals("Qc1-h6+", m.getMove().toString());
	}

	//TODO
	/*
	 * 2017-12-26 16:41:48.963<--1:after move Rg3xg8+, fen:r1b2kR1/pppp1p2/2n1p2p/7q/2BPP3/2P2N2/P1PKQP1P/R7 b - - 27 14
	 * 2017-12-26 16:41:48.963<--1:set strategy depth to 4 2017-12-26 16:41:48.967<--1:Exception in thread "main"
	 * java.lang.IllegalStateException: cannot remove king!? 2017-12-26 16:41:48.968<--1: at
	 * org.rjo.chess.pieces.King.removePiece(King.java:226) 2017-12-26 16:41:48.969<--1: at
	 * org.rjo.chess.Position.internalMove(Position.java:516) 2017-12-26 16:41:48.969<--1: at
	 * org.rjo.chess.Position.move(Position.java:476) 2017-12-26 16:41:48.970<--1: at
	 * org.rjo.chess.eval.AlphaBeta2.alphaBetaMin(AlphaBeta2.java:103) 2017-12-26 16:41:48.970<--1: at
	 * org.rjo.chess.eval.AlphaBeta2.alphaBetaMax(AlphaBeta2.java:66) 2017-12-26 16:41:48.971<--1: at
	 * org.rjo.chess.eval.AlphaBeta2.alphaBetaMin(AlphaBeta2.java:105) 2017-12-26 16:41:48.971<--1: at
	 * org.rjo.chess.eval.AlphaBeta2.alphaBetaMax(AlphaBeta2.java:66) 2017-12-26 16:41:48.972<--1: at
	 * org.rjo.chess.eval.AlphaBeta2.findMove(AlphaBeta2.java:32) 2017-12-26 16:41:48.972<--1: at
	 * org.rjo.chess.UCI.processCommandGo(UCI.java:67) 2017-12-26 16:41:48.973<--1: at org.rjo.chess.UCI.run(UCI.java:41)
	 */

	/*
	 * self-mate
	 * {@formatter:off}
	2017-12-27 20:13:41.995-->1:position startpos moves d2d4 e7e6 d4d5 e6d5 d1d5 f8b4 c2c3 g8e7 d5e4 b4c5 g1f3 d7d5 e4c2 c8f5 c2a4 d8d7 a4d7 b8d7 c1f4 c5d6 f4d6 c7d6 f3d4 e8g8 e2e3 d7c5 b1d2 c5d3 f1d3 f5d3 e1c1 e7f5 d2f3 f5d4 d1d3 d4f3 g2f3 f7f6 d3d5 b7b6 d5d6 g8f7 h1d1 g7g5 c1c2 f7g6 e3e4 a7a5 e4e5 h7h5 d6b6 f8h8 b6f6 g6g7 d1d7 g7g8 f6f7
	2017-12-27 20:13:41.995-->1:go wtime 672409 btime 398139 winc 20000 binc 20000
	2017-12-27 20:13:41.996<--1:after move Rf6-f7, fen:r5kr/3R1R2/8/p3P1pp/8/2P2P2/PPK2P1P/8 b - - 57 29
	2017-12-27 20:13:41.996<--1:set strategy depth to 5
	2017-12-27 20:13:49.347<--1:20:13:49.347 [main] INFO  org.rjo.chess.eval.AlphaBeta3 - got result: 430:Optional[[Ra8-f8, Rf7-g7+]], posns evaluated: 37112
	2017-12-27 20:13:49.348<--1:bestmove a8f8
	2017-12-27 20:13:49.348*1*Found move:Ra8-f8
	 * {@formatter:on}
	 */

	@Test
	public void easierUCIString() {
		//		Game game = Fen.decode("6k1/6pp/6p1/8/3p2PK/2pr4/6P1/2q5 b - - 67 34");
		Game game = Fen.decode("r1bnk3/pp1p2pp/3P2p1/Q3P3/6q1/8/PP4PP/R4RK1 w - - 5 6");
		SearchStrategy strat = new AlphaBeta3(System.out);
		MoveInfo m = strat.findMove(game.getPosition());
		System.out.println(m);
	}

	/*
	 * {@formatter:off}
2017-12-27 19:49:24.409-->1:position startpos moves d2d4 e7e6 e2e4 b8c6 g1f3 g8f6 b1c3 f8b4 f1d3 d7d5 e1g1 b4c3 b2c3 d5e4 d3e4 f6e4 f1e1 e4c3 d1d3 c3d5 c1a3 a7a6 c2c4 d5f6 d4d5 e6e5 d5c6 d8d3 c6b7 c8b7 f3e5 d3a3 e5g6
2017-12-27 19:49:24.409-->1:go wtime 524295 btime 314331 winc 20000 binc 20000
2017-12-27 19:49:24.410<--1:after move Ne5-g6+, fen:r3k2r/1bp2ppp/p4nN1/8/2P5/q7/P4PPP/R3R1K1 b kq - 33 17
2017-12-27 19:49:24.411<--1:set strategy depth to 5
2017-12-27 19:49:24.433<--1:Exception in thread "main" java.lang.IllegalStateException: cannot remove king!?
2017-12-27 19:49:24.434<--1:	at org.rjo.chess.pieces.King.removePiece(King.java:226)
2017-12-27 19:49:24.434<--1:	at org.rjo.chess.Position.internalMove(Position.java:516)
2017-12-27 19:49:24.435<--1:	at org.rjo.chess.Position.move(Position.java:476)
	 * {@formatter:on}
	 */
	/*
	 * {@formatter:off}
2017-12-27 10:57:44.286-->1:position startpos moves d2d4 e7e6 c2c4 b8c6 d4d5 e6d5 g1f3 f8b4 b1c3 d5c4 e2e4 d8e7 f1c4 e7e4 c4e2 g8f6 e1g1 b4c3 b2c3 e4d5 c3c4 d5d1 f1d1 e8g8 c1f4 d7d6 c4c5 d6c5 f4c7 c8g4 h2h3 f8e8 g1f1 a8c8 c7g3 g4h5 a1c1 f6e4 e2c4 e4g3 f2g3 c6a5 g3g4 a5c4 c1c4 h5g6 d1d7 b7b5 d7a7 b5c4 f1f2 c4c3 h3h4 c3c2 h4h5 c2c1q h5g6 f7g6 f2g3 e8e2 a7d7 e2a2 f3e5 c8e8 e5c6 a2a3 g3h4
2017-12-27 10:57:44.286-->1:go wtime 657717 btime 70652 winc 20000 binc 20000
2017-12-27 10:57:44.287<--1:after move Kg3-h4, fen:4r1k1/3R2pp/2N3p1/2p5/6PK/r7/6P1/2p5 b - - 67 34
2017-12-27 10:57:44.288<--1:set strategy depth to 5
2017-12-27 10:57:58.670<--1:10:57:58.670 [main] INFO  org.rjo.chess.eval.AlphaBeta2 - got result: 768:Optional[[g6-g5+, Kh4xg5, Ra3-a6, Kg5-h4, Ra6xc6]], posns evaluated: 141859
2017-12-27 10:57:58.670<--1:bestmove g6g5
2017-12-27 10:57:58.671*1*Found move:g6-g5
2017-12-27 10:59:40.646*1*Start calc, move no: 69
2017-12-27 10:59:40.646-->1:position startpos moves d2d4 e7e6 c2c4 b8c6 d4d5 e6d5 g1f3 f8b4 b1c3 d5c4 e2e4 d8e7 f1c4 e7e4 c4e2 g8f6 e1g1 b4c3 b2c3 e4d5 c3c4 d5d1 f1d1 e8g8 c1f4 d7d6 c4c5 d6c5 f4c7 c8g4 h2h3 f8e8 g1f1 a8c8 c7g3 g4h5 a1c1 f6e4 e2c4 e4g3 f2g3 c6a5 g3g4 a5c4 c1c4 h5g6 d1d7 b7b5 d7a7 b5c4 f1f2 c4c3 h3h4 c3c2 h4h5 c2c1q h5g6 f7g6 f2g3 e8e2 a7d7 e2a2 f3e5 c8e8 e5c6 a2a3 g3h4 g6g5 h4h5
2017-12-27 10:59:40.648-->1:go wtime 576279 btime 76282 winc 20000 binc 20000
2017-12-27 10:59:40.649<--1:after move Kh4-h5, fen:4r1k1/3R2pp/2N5/2p3pK/6P1/r7/6P1/2p5 b - - 69 35
2017-12-27 10:59:40.649<--1:set strategy depth to 5
2017-12-27 11:00:01.294<--1:11:00:01.294 [main] INFO  org.rjo.chess.eval.AlphaBeta2 - got result: 752:Optional[[Kg8-h8, Nc6-d8, g7-g6+, Kh5xg5, Re8xd8]], posns evaluated: 201499
2017-12-27 11:00:01.295<--1:bestmove g8h8
2017-12-27 11:00:01.296*1*Found move:Kg8-h8
2017-12-27 11:00:10.261*1*Start calc, move no: 71
2017-12-27 11:00:10.261-->1:position startpos moves d2d4 e7e6 c2c4 b8c6 d4d5 e6d5 g1f3 f8b4 b1c3 d5c4 e2e4 d8e7 f1c4 e7e4 c4e2 g8f6 e1g1 b4c3 b2c3 e4d5 c3c4 d5d1 f1d1 e8g8 c1f4 d7d6 c4c5 d6c5 f4c7 c8g4 h2h3 f8e8 g1f1 a8c8 c7g3 g4h5 a1c1 f6e4 e2c4 e4g3 f2g3 c6a5 g3g4 a5c4 c1c4 h5g6 d1d7 b7b5 d7a7 b5c4 f1f2 c4c3 h3h4 c3c2 h4h5 c2c1q h5g6 f7g6 f2g3 e8e2 a7d7 e2a2 f3e5 c8e8 e5c6 a2a3 g3h4 g6g5 h4h5 g8h8 c6e7
2017-12-27 11:00:10.262-->1:go wtime 587623 btime 75646 winc 20000 binc 20000
2017-12-27 11:00:10.263<--1:after move Nc6-e7, fen:4r2k/3RN1pp/8/2p3pK/6P1/r7/6P1/2p5 b - - 71 36
2017-12-27 11:00:10.263<--1:set strategy depth to 5
2017-12-27 11:00:31.972<--1:11:00:31.972 [main] INFO  org.rjo.chess.eval.AlphaBeta2 - got result: 764:Optional[[Ra3-a2, Kh5xg5, h7-h6+, Kg5-h4, Re8xe7]], posns evaluated: 220671
2017-12-27 11:00:31.973<--1:bestmove a3a2
2017-12-27 11:00:31.973*1*Found move:Ra3-a2
2017-12-27 11:00:40.142*1*Start calc, move no: 73
2017-12-27 11:00:40.143-->1:position startpos moves d2d4 e7e6 c2c4 b8c6 d4d5 e6d5 g1f3 f8b4 b1c3 d5c4 e2e4 d8e7 f1c4 e7e4 c4e2 g8f6 e1g1 b4c3 b2c3 e4d5 c3c4 d5d1 f1d1 e8g8 c1f4 d7d6 c4c5 d6c5 f4c7 c8g4 h2h3 f8e8 g1f1 a8c8 c7g3 g4h5 a1c1 f6e4 e2c4 e4g3 f2g3 c6a5 g3g4 a5c4 c1c4 h5g6 d1d7 b7b5 d7a7 b5c4 f1f2 c4c3 h3h4 c3c2 h4h5 c2c1q h5g6 f7g6 f2g3 e8e2 a7d7 e2a2 f3e5 c8e8 e5c6 a2a3 g3h4 g6g5 h4h5 g8h8 c6e7 a3a2 e7g6
2017-12-27 11:00:40.143-->1:go wtime 599763 btime 73948 winc 20000 binc 20000
2017-12-27 11:00:40.144<--1:after move Ne7-g6+, fen:4r2k/3R2pp/6N1/2p3pK/6P1/8/r5P1/2p5 b - - 73 37
2017-12-27 11:00:40.144<--1:set strategy depth to 5
2017-12-27 11:00:40.176<--1:Exception in thread "main" java.lang.IllegalStateException: cannot remove king!?
2017-12-27 11:00:40.177<--1:	at org.rjo.chess.pieces.King.removePiece(King.java:226)
2017-12-27 11:00:40.178<--1:	at org.rjo.chess.Position.internalMove(Position.java:516)
2017-12-27 11:00:40.178<--1:	at org.rjo.chess.Position.move(Position.java:476)
2017-12-27 11:00:40.179<--1:	at org.rjo.chess.eval.AlphaBeta2.alphaBetaMin(AlphaBeta2.java:103)
2017-12-27 11:00:40.179<--1:	at org.rjo.chess.eval.AlphaBeta2.alphaBetaMax(AlphaBeta2.java:66)
2017-12-27 11:00:40.179<--1:	at org.rjo.chess.eval.AlphaBeta2.alphaBetaMin(AlphaBeta2.java:105)
2017-12-27 11:00:40.180<--1:	at org.rjo.chess.eval.AlphaBeta2.alphaBetaMax(AlphaBeta2.java:66)
2017-12-27 11:00:40.180<--1:	at org.rjo.chess.eval.AlphaBeta2.findMove(AlphaBeta2.java:32)
2017-12-27 11:00:40.181<--1:	at org.rjo.chess.UCI.processCommandGo(UCI.java:67)
2017-12-27 11:00:40.181<--1:	at org.rjo.chess.UCI.run(UCI.java:41)
2017-12-27 11:00:40.182<--1:	at org.rjo.chess.UCI.main(UCI.java:23)
2017-12-27 11:00:47.092-->1:stop
	 * {@formatter:on}
    */

	@Test
	public void fromFenString() {
		Game game = Fen.decode("r2r2k1/pR3p1p/2n5/3N2p1/2P1p1b1/P1B1P3/2K3PP/5R2 b - - 1 22");
		SearchStrategy strat = new AlphaBeta3(System.out);
		MoveInfo m = strat.findMove(game.getPosition());
		System.out.println(m);
	}
}
