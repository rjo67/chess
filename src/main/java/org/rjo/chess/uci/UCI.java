package org.rjo.chess.uci;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;

import org.rjo.chess.Colour;
import org.rjo.chess.Fen;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.Square;
import org.rjo.chess.eval.AlphaBeta3;
import org.rjo.chess.eval.MoveInfo;
import org.rjo.chess.eval.SearchStrategy;

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

	/**
	 * interface for tests.
	 *
	 * @param cmd the required command
	 * @return the UCI object
	 */
	public static UCI testInterface(String cmd) {
		UCI uci = new UCI();

		try (Scanner lineScanner = new Scanner(cmd)) {
			uci.processLine(lineScanner);
		}
		return uci;
	}

	public void run() {
		boolean finished = false;
		try (Scanner sc = new Scanner(System.in)) {
			while (!finished) {
				String line = sc.nextLine();
				try (Scanner lineScanner = new Scanner(line)) {
					finished = processLine(lineScanner);
				}
			}
		}
	}

	/* for tests */
	public Game getGame() {
		return game;
	}

	/**
	 * process one uci command line.
	 *
	 * @param lineScanner
	 * @return true if processing is finished (e.g. "quit" received)
	 */
	boolean processLine(Scanner lineScanner) {
		boolean finished = false;
		if (lineScanner.hasNext()) {
			String nextCmd = lineScanner.next();
			switch (nextCmd) {
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
			default:
				System.out.println("unrecognised: " + nextCmd);
			}
		} else {
			finished = true;
		}
		return finished;
	}

	private void processCommandIsReady() {
		System.out.println("readyok");
	}

	private void processCommandGo(Scanner lineScanner) {
		boolean infinite = lineScanner.hasNext() && "infinite".equals(lineScanner.next());

		// UCI reporter thread
		UciReporter uciReporter = new UciReporter(strategy, System.out);
		new Thread(uciReporter).start();

		moveinfo = strategy.findMove(game.getPosition());
		uciReporter.setStop(true);
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
			} else {
				throw new RuntimeException("invalid value after 'position': expected fen or startpos, got '" + subcmd + "'");
			}
			game = Fen.decode(fen);
			// create strategy here in order to be able to autoincrement the depth
			strategy = new AlphaBeta3(System.out, game.getZobristMap());
			// move on to "moves"
			if (lineScanner.hasNext()) {
				subcmd = lineScanner.next();
			}
		}
		int nbrCapturedPieces = 0;
		if ("moves".equals(subcmd)) {
			// process moves
			while (lineScanner.hasNext()) {
				String moveStr = lineScanner.next();
				boolean lastmove = !lineScanner.hasNext();
				Move m = Move.fromUCIString(moveStr, game);
				// only worry about check for the last move
				game.makeMove(m);
				if (m.isCapture()) {
					nbrCapturedPieces++;
				}
				if (lastmove) {
					Square kingsSquare = game.getPosition().getKingPosition(game.getPosition().getSideToMove());
					boolean incheck = game.getPosition().squareIsAttacked(kingsSquare,
							Colour.oppositeColour(game.getPosition().getSideToMove()));
					m.setCheck(incheck);
					game.getPosition().setInCheck(incheck);
					System.out.println("after move " + m + ", fen:" + Fen.encode(game));
				}
			}
		}
		// pretty simplistic way to increase the search depth if fewer pieces on the board
		strategy.incrementDepth(nbrCapturedPieces / 12);
		System.out.println("set strategy depth to " + strategy.getCurrentDepth());
	}

	private void processCommandUci() {
		System.out.println("id name bulldog 1.0");
		System.out.println("id author rjo67");
		System.out.println("uciok");
	}

	static class NullOutputStream extends OutputStream {
		@Override
		public void write(@SuppressWarnings("unused") int arg0) throws IOException {
		}
	}

}
