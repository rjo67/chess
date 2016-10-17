package org.rjo.chess;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;

import org.rjo.chess.pieces.King;

/**
 * Starts threads for a UCI-conform interface and the engine.
 *
 * @author rich
 */
public class UCI {
	private Game game;
	private SearchStrategy strategy;
	private MoveInfo moveinfo;

	public static void main(String[] args) throws InterruptedException {
		UCI uci = new UCI();
		uci.run();
	}

	public void run() {
		boolean finished = false;
		strategy = new AlphaBeta(/* new PrintStream(new NullOutputStream()) */);
		try (Scanner sc = new Scanner(System.in)) {
			while (!finished) {
				String line = sc.nextLine();
				try (Scanner lineScanner = new Scanner(line)) {
					if (lineScanner.hasNext()) {
						switch (lineScanner.next()) {
						case "uci":
							processCommandUci();
							break;
						case "isready":
							processCommandIsReady();
							break;
						case "go":
							processCommandGo(lineScanner);
							break;
						case "stop":
							processCommandStop();
							break;
						case "position":
							processCommandPosition(lineScanner);
							break;
						case "quit":
							finished = true;
							break;

						}
					}

				}
			}
		}
	}

	private void processCommandIsReady() {
		System.out.println("readyok");
	}

	private void processCommandGo(Scanner lineScanner) {
		boolean infinite = lineScanner.hasNext() && "infinite".equals(lineScanner.next());
		moveinfo = strategy.findMove(game);
		if (!infinite) {
			System.out.println("bestmove " + moveinfo.getMove().toUCIString());
		}
	}

	private void processCommandStop() {
		if (moveinfo != null) {
			System.out.println("bestmove " + moveinfo.getMove().toUCIString());
		}
	}

	private void processCommandPosition(Scanner lineScanner) {
		// position [fen <fenstring> | startpos ] moves <move1> .... <movei>
		String subcmd = lineScanner.next();
		if (!"moves".equals(subcmd)) {
			String fen = null;
			if ("fen".equals(subcmd)) {
				fen = lineScanner.next();
			} else if ("startpos".equals(subcmd)) {
				fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w kqKQ - 0 1";
			}
			game = Fen.decode(fen);
			// move on to "moves"
			if (lineScanner.hasNext()) {
				subcmd = lineScanner.next();
			}
		}
		if ("moves".equals(subcmd)) {
			// process moves
			while (lineScanner.hasNext()) {
				String moveStr = lineScanner.next();
				boolean lastmove = !lineScanner.hasNext();
				Move m = Move.fromUCIString(moveStr, game);
				// only worry about check for the last move
				game.move(m);
				if (lastmove) {
					Square kingsSquare = King.findKing(game.getChessboard().getSideToMove(), game.getChessboard());
					boolean incheck = game.getChessboard().squareIsAttacked(kingsSquare,
							Colour.oppositeColour(game.getChessboard().getSideToMove()));
					m.setCheck(incheck);
					game.setInCheck(incheck);
					System.out.println("after move " + m + ", fen:" + Fen.encode(game));
				}
			}
		}
	}

	private void processCommandUci() {
		System.out.println("id name bulldog 1.0");
		System.out.println("id author rjo67");
		System.out.println("uciok");
	}

	class NullOutputStream extends OutputStream {
		@Override
		public void write(int arg0) throws IOException {
		}
	}

}
