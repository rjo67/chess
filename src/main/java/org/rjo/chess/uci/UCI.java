package org.rjo.chess.uci;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.rjo.chess.Colour;
import org.rjo.chess.Fen;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.Position;
import org.rjo.chess.Square;
import org.rjo.chess.eval.AlphaBeta3;
import org.rjo.chess.eval.MoveInfo;
import org.rjo.chess.eval.SearchStrategy;
import org.rjo.chess.pieces.King;
import org.rjo.chess.pieces.Piece;
import org.rjo.chess.pieces.PieceType;

/**
 * Starts threads for a UCI-conform interface and the engine.
 *
 * @author rich
 */
public class UCI {
	private Game game;

	private MoveInfo moveinfo;

	// set after "processCommandPosition" to store the last move from the uci string
	private Move lastMove;

	public static void main(String[] args) {
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

		SearchStrategy strategy = new AlphaBeta3(System.out, game.getZobristMap());

		// UCI reporter thread
		UciReporter uciReporter = new UciReporter(strategy, System.out);
		new Thread(uciReporter).start();

		moveinfo = _findMove(strategy);

		uciReporter.setStop(true);
		if (!infinite) {
			System.out.println("bestmove " + moveinfo.getMove().toUCIString());
		}
	}

	/** for tests */
	public MoveInfo findMove(SearchStrategy strategy) {
		return _findMove(strategy);
	}

	private MoveInfo _findMove(SearchStrategy strategy) {

		Map<PieceType, Integer>[] pieceCounts = analysePosition(game.getPosition());

		int nbrPieces = 0;
		for (Colour colour : Colour.ALL_COLOURS) {
			nbrPieces += pieceCounts[colour.ordinal()].values().stream().mapToInt(Integer::intValue).sum();
		}
		// simplistic way to increase the search depth if fewer pieces on the board
		strategy.incrementDepth((32 - nbrPieces) / 12);
		System.out.println("set strategy depth to " + strategy.getCurrentDepth());

        King.IN_ENDGAME = inEndgame(pieceCounts);

		// can clear the zobrist map if the last move was a pawn move
		if (lastMove.getPiece() == PieceType.PAWN) {
			game.getZobristMap().clear();
		}

		return strategy.findMove(game.getPosition());
	}

	private Map<PieceType, Integer>[] analysePosition(Position position) {
		@SuppressWarnings("unchecked")
		Map<PieceType, Integer>[] pieceCounts = new HashMap[Colour.ALL_COLOURS.length];
		for (Colour colour : Colour.ALL_COLOURS) {
			final Piece[] pieces = position.getPieces(colour);
			Map<PieceType, Integer> counts = new HashMap<>();
			counts.put(PieceType.KING, 1); // always one king ;-)
			for (PieceType pieceType : PieceType.ALL_PIECE_TYPES_EXCEPT_KING) {
				counts.put(pieceType, pieces[pieceType.ordinal()].numberOfPieces());
			}
			pieceCounts[colour.ordinal()] = counts;
		}
		return pieceCounts;
	}

	/**
	 * Are we in an endgame?
	 *
	 * @param pieceCounts counts of pieces still present
	 * @return true if considerered to be in an endgame
	 */
	private boolean inEndgame(Map<PieceType, Integer>[] pieceCounts) {
		// in endgame if each side has <= 13 material points
		boolean inEndgame = true;
		for (Colour colour : Colour.ALL_COLOURS) {
			int points = (9 * pieceCounts[colour.ordinal()].get(PieceType.QUEEN)) +
					(5 * pieceCounts[colour.ordinal()].get(PieceType.ROOK)) +
					(3 * pieceCounts[colour.ordinal()].get(PieceType.BISHOP)) +
					(3 * pieceCounts[colour.ordinal()].get(PieceType.KNIGHT)) +
					pieceCounts[colour.ordinal()].get(PieceType.PAWN);
			inEndgame = inEndgame && (points <= 13);
		}
		return inEndgame;
	}

	private void processCommandStop() {
		if (moveinfo != null) {
			System.out.println("bestmove " + moveinfo.getMove().toUCIString());
		}
	}

	private void processCommandPosition(Scanner lineScanner) {
		// position [fen <fenstring> | startpos ] moves <move1> .... <movei>
		this.lastMove = null;
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
				game.makeMove(m);
				if (lastmove) {
					this.lastMove = m;
					Square kingsSquare = game.getPosition().getKingPosition(game.getPosition().getSideToMove());
					boolean incheck = game.getPosition().squareIsAttacked(kingsSquare,
							Colour.oppositeColour(game.getPosition().getSideToMove()));
					m.setCheck(incheck);
					game.getPosition().setInCheck(incheck);
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

	static class NullOutputStream extends OutputStream {
		@Override
		public void write(@SuppressWarnings("unused") int arg0) {
		}
	}

}
