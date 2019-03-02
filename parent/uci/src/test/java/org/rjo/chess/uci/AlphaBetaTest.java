package org.rjo.chess.uci;

import org.junit.Test;
import org.rjo.chess.Game;
import org.rjo.chess.base.eval.MoveInfo;
import org.rjo.chess.eval.AlphaBeta3;
import org.rjo.chess.eval.SearchStrategy;
import org.rjo.chess.position.Fen;

public class AlphaBetaTest {

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

	@Test
	public void fromUCIString() {
		UCI uci = UCI.testInterface(
				"position startpos moves d2d4 e7e6 e2e4 b8c6 g1f3 g8f6 b1c3 f8b4 f1d3 d7d5 e1g1 b4c3 b2c3 d5e4 d3e4 f6e4 f1e1 e4c3 d1d3 "
						+ "c3d5 c1a3 a7a6 c2c4 d5f6 d4d5 e6e5 d5c6 d8d3 c6b7 c8b7 f3e5 d3a3 e5g6");

		//		System.out.println(uci.getGame().getPosition().isInCheck());
		MoveInfo m = uci.findMove(new AlphaBeta3(System.out));
	}

	@Test
	public void selfMate() {
		UCI uci = UCI.testInterface(
				"position startpos moves d2d4 e7e6 d4d5 e6d5 d1d5 f8b4 c2c3 g8e7 d5e4 b4c5 g1f3 d7d5 e4c2 c8f5 "
						+ "c2a4 d8d7 a4d7 b8d7 c1f4 c5d6 f4d6 c7d6 f3d4 e8g8 e2e3 d7c5 b1d2 c5d3 f1d3 f5d3 e1c1 e7f5 "
						+ "d2f3 f5d4 d1d3 d4f3 g2f3 f7f6 d3d5 b7b6 d5d6 g8f7 h1d1 g7g5 c1c2 f7g6 e3e4 a7a5 "
						+ "e4e5 h7h5 d6b6 f8h8 b6f6 g6g7 d1d7 g7g8 f6f7");
		//2017-12-27 20:13:41.996<--1:after move Rf6-f7, fen:r5kr/3R1R2/8/p3P1pp/8/2P2P2/PPK2P1P/8 b - - 57 29
		SearchStrategy strat = new AlphaBeta3(System.out);
		strat.incrementDepth(1);
		MoveInfo m = strat.findMove(uci.getGame().getPosition());
	}

	/*
	 * bad queen move
	 * {@formatter:off}
	2017-12-30 15:41:10.464-->1:position startpos moves f2f4 g8f6 d2d4 e7e6 g1f3 f6e4 d1d3 d7d5 b1c3 b8c6 c3e4 d5e4 d3e4 f7f5 e4d3 f8b4 c1d2 d8e7 e1c1 e8g8 f3e5 b4d2 d1d2 c8d7 g2g3 f8f6 f1g2 d7e8 d3c4 c6e5 d4e5 f6f7 g2b7 a8d8 h1d1 d8d2 d1d2 g7g5 b7c8 f7f8 c8e6 g8h8 e6d5 e8h5 e5e6 g5f4 c4f4
	2017-12-30 15:41:10.464-->1:go wtime 651575 btime 720728 winc 20000 binc 20000
	2017-12-30 15:41:10.465<--1:after move Qc4xf4, fen:5r1k/p1p1q2p/4P3/3B1p1b/5Q2/6P1/PPPRP2P/2K5 b - - 47 24
	2017-12-30 15:41:10.465<--1:set strategy depth to 5
	2017-12-30 15:45:42.250<--1:15:45:42.250 [main] INFO  org.rjo.chess.eval.AlphaBeta3 - got result: 329:Optional[[Qe7-g5, Bd5-a8, c7-c6, Qf4-f1, Qg5-g8]], posns evaluated: 1889669
	2017-12-30 15:45:42.250<--1:bestmove e7g5
	2017-12-30 15:45:42.250*1*Found move:Qe7-g5
	 * {@formatter:on}
	 */
	@Test
	public void badQueenMove() {
		UCI uci = UCI.testInterface(
				"position startpos moves f2f4 g8f6 d2d4 e7e6 g1f3 f6e4 d1d3 d7d5 b1c3 b8c6 c3e4 d5e4 d3e4 "
						+ "f7f5 e4d3 f8b4 c1d2 d8e7 e1c1 e8g8 f3e5 b4d2 d1d2 c8d7 g2g3 f8f6 f1g2 d7e8 d3c4 c6e5 d4e5 f6f7 "
						+ "g2b7 a8d8 h1d1 d8d2 d1d2 g7g5 b7c8 f7f8 c8e6 g8h8 e6d5 e8h5 e5e6 g5f4 c4f4");
		//after move Qc4xf4, fen:5r1k/p1p1q2p/4P3/3B1p1b/5Q2/6P1/PPPRP2P/2K5 b - - 47 24
		SearchStrategy strat = new AlphaBeta3(System.out);
		System.out.println(strat.getCurrentDepth());
		MoveInfo mi = strat.findMove(uci.getGame().getPosition());
		System.out.println(mi);

		Game game = Fen.decode("5r1k/p1p1q2p/4P3/3B1p1b/5Q2/6P1/PPPRP2P/2K5 w - - 47 24");
		System.out.println(strat.getCurrentDepth());
		mi = strat.findMove(game.getPosition());
		System.out.println(mi);
	}

	/*
	 * bad knight move
	 * {@formatter:off}
2017-12-30 16:38:38.890-->1:position startpos moves d2d4 e7e6 c2c4 b8c6 g1f3 f8b4 b1c3 g8f6 c1g5 e8g8 e2e4 d7d6 e4e5 d6e5 d4e5 b4c3 b2c3 d8d1 a1d1 f6e4 f1d3 e4c3 d1d2
2017-12-30 16:38:38.890-->1:go wtime 479186 btime 495679 winc 20000 binc 20000
2017-12-30 16:38:38.891<--1:after move Rd1-d2, fen:r1b2rk1/ppp2ppp/2n1p3/4P1B1/2P5/2nB1N2/P2R1PPP/4K2R b K - 23 12
2017-12-30 16:38:38.891<--1:set strategy depth to 4
2017-12-30 16:38:39.035<--1:info pv c3a2 d2a2 h7h6 a2a7 score cp 275
2017-12-30 16:38:39.175<--1:info pv c6e5 f3e5 h7h6 e5f7 score cp 231
 	 * {@formatter:on}
	 */

	/*
	 * no mate in one, finds mate in 3
	 * {@formatter:off}
2017-12-30 16:47:13.236-->1:position startpos moves d2d4 e7e6 c2c4 b8c6 g1f3 f8b4 b1c3 g8f6 c1g5 e8g8 e2e4 d7d6 e4e5 d6e5 d4e5 b4c3 b2c3 d8d1 a1d1 f6e4 f1d3 e4c3 d1d2 c6b4 g5e7 b4c6 e7f8 g8f8 d3h7 b7b6 e1g1 c8b7 f1c1 c3a4 f3d4 a4c5 d4c6 b7c6 f2f3 g7g6 g1f2 f8g7 h7g6 f7g6 c1d1 a8h8 h2h3 h8h5 d2e2 h5h4 d1c1 c5d3 f2e3 d3c1 e2c2 h4h5 f3f4 c6g2 c2g2 h5h3 e3d4 c1d3 f4f5 c7c5 d4e4 e6f5 e4d5 d3f4 d5d6 f4g2 e5e6 h3e3 d6d7 g2f4 e6e7 g7f7 a2a4 e3e7 d7d6 e7e4 d6c6 e4c4 c6b5 c4b4 b5c6 b4a4 c6b5 a4a3 b5c4 g6g5 c4b5 g5g4 b5c6 g4g3 c6b5 g3g2 b5c6 g2g1q c6b5 g1h1 b5c4 h1d5 c4b5
2017-12-30 16:47:13.236-->1:go wtime 992110 btime 1086412 winc 20000 binc 20000
2017-12-30 16:47:13.238<--1:after move Kc4-b5, fen:8/p4k2/1p6/1Kpq1p2/5n2/r7/8/8 b - - 103 52
2017-12-30 16:47:13.446<--1:info pv c5c4 b5b4 d5c5 score cp -2306
2017-12-30 16:47:14.034<--1:info pv a3a5 score cp -2312
2017-12-30 16:47:17.083<--1:info pv d5d6 b5c4 d6d3 score cp -2320
2017-12-30 16:47:17.177<--1:info pv d5d7 b5c4 d7a4 score cp -2336
2017-12-30 16:47:18.242<--1:info pv d5f3 b5c4 f3b3 score cp -2368
2017-12-30 16:47:18.470<--1:info pv d5g2 b5c4 g2e2 score cp -2384
2017-12-30 16:47:18.701<--1:info pv d5h1 b5c4 h1f1 score cp -2392
2017-12-30 16:47:20.766<--1:info pv d5b7 b5c4 b7a6 score cp -2400
2017-12-30 16:47:21.318<--1:16:47:21.318 [main] INFO  org.rjo.chess.eval.AlphaBeta3 - got result: -2400:Optional[[Qd5-b7, Kb5-c4, Qb7-a6+]], posns evaluated: 77133
2017-12-30 16:47:21.318<--1:bestmove d5b7
2017-12-30 16:47:21.318*1*Found move:Qd5-b7
 	 * {@formatter:on}
	 */
	@Test
	// this now works after fixing the value of a mate in AlphaBeta3
	public void anotherMateInOne() {
		UCI uci = UCI.testInterface(
				"position startpos moves d2d4 e7e6 c2c4 b8c6 g1f3 f8b4 b1c3 g8f6 c1g5 e8g8 e2e4 "
						+ "d7d6 e4e5 d6e5 d4e5 b4c3 b2c3 d8d1 a1d1 f6e4 f1d3 e4c3 d1d2 c6b4 g5e7 b4c6 "
						+ "e7f8 g8f8 d3h7 b7b6 e1g1 c8b7 f1c1 c3a4 f3d4 a4c5 d4c6 b7c6 f2f3 g7g6 g1f2 "
						+ "f8g7 h7g6 f7g6 c1d1 a8h8 h2h3 h8h5 d2e2 h5h4 d1c1 c5d3 f2e3 d3c1 e2c2 h4h5 "
						+ "f3f4 c6g2 c2g2 h5h3 e3d4 c1d3 f4f5 c7c5 d4e4 e6f5 e4d5 d3f4 d5d6 f4g2 e5e6 "
						+ "h3e3 d6d7 g2f4 e6e7 g7f7 a2a4 e3e7 d7d6 e7e4 d6c6 e4c4 c6b5 c4b4 b5c6 b4a4 c6b5 a4a3 b5c4 "
						+ "g6g5 c4b5 g5g4 b5c6 g4g3 c6b5 g3g2 b5c6 g2g1q c6b5 g1h1 b5c4 h1d5 c4b5");
		//after move Kc4-b5, fen:8/p4k2/1p6/1Kpq1p2/5n2/r7/8/8 b - - 103 52
		MoveInfo mi;
		SearchStrategy strat = new AlphaBeta3(System.out);

		//		System.out.println(strat.getCurrentDepth());
		//		mi = strat.findMove(uci.getGame().getPosition());
		//		System.out.println(mi);

		// simplified -- finds a different mate in 3
		Game game = Fen.decode("8/p4k2/1p6/1Kpq13/8/r7/8/8 b - - 103 52");
		System.out.println(strat.getCurrentDepth());
		mi = strat.findMove(game.getPosition());
		System.out.println(mi);
	}

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
