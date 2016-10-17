package org.rjo.chess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.rjo.chess.pieces.PieceType;

public class PlayGame {

	private SearchStrategy strategy;

	public PlayGame(SearchStrategy strategy) {
		this.strategy = strategy;
	}

	public static void main(String[] args) throws IOException {
		PlayGame p = new PlayGame(new AlphaBeta());
		p.run();
	}

	private void run() throws IOException {
		System.out.println("Starting new game with strategy: " + strategy.toString());
		Game game = new Game();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
			boolean finished = false;
			while (!finished) {
				System.out.print(game.getMoveNumber() + ":  ");
				try {
					Move humanMove = getMove(game, in);
					checkValidity(humanMove, game);
					game.move(humanMove);
					MoveInfo mi = strategy.findMove(game);
					if (mi.getMove() == null) {
						finished = true;
						if (game.isInCheck()) {
							System.out.println("checkmate!");
						} else {
							System.out.println("stalemate!");
						}
					} else {
						System.out.println(game.getMoveNumber() + "... " + mi.getMove());
						game.move(mi.getMove());
					}
				} catch (IllegalArgumentException x) {
					System.out.println(x.getMessage());
				}
			}
		}
	}

	private void checkValidity(Move move, Game game) throws IllegalArgumentException {
		// piece at given square?
		if (move.getPiece() != game.getChessboard().pieceAt(move.from(), game.getChessboard().getSideToMove())) {
			throw new IllegalArgumentException("no " + move.getPiece() + " at " + move.from());
		}
		if (!move.isCapture()) {
			if (!game.getChessboard().getEmptySquares().getBitSet().get(move.to().bitIndex())) {
				throw new IllegalArgumentException("square " + move.to() + " not empty");
			}
		} else {
			// if capture: opponent's piece at given square?
			if (!game.getChessboard().getAllPieces(Colour.oppositeColour(game.getChessboard().getSideToMove()))
					.getBitSet().get(move.to().bitIndex())) {
				throw new IllegalArgumentException("no opponent's piece at " + move.to());
			}
		}
		// legal move for piece?
		// king in check after move?
	}

	private Move getMove(Game game, BufferedReader in) throws IllegalArgumentException, IOException {
		Move m;
		String moveStr = in.readLine();
		if ("O-O".equals(moveStr)) {
			m = Move.castleKingsSide(game.getChessboard().getSideToMove());
		} else if ("O-O-O".equals(moveStr)) {
			m = Move.castleQueensSide(game.getChessboard().getSideToMove());
		} else {
			PieceType pt = convertStringToPieceType(moveStr.charAt(0));
			int reqdStrLen = 6;
			int startOfFromSquare = 1;
			if (pt == PieceType.PAWN) {
				startOfFromSquare = 0;
				reqdStrLen = 5;
			} else {
				reqdStrLen = 6;
				startOfFromSquare = 1;
			}
			if (moveStr.length() < reqdStrLen) {
				if (pt == PieceType.PAWN) {
					throw new IllegalArgumentException("invalid input. Must be >=5 chars for a pawn move");
				} else {
					throw new IllegalArgumentException("invalid input. Must be >=6 chars");
				}
			}
			if (!((moveStr.charAt(startOfFromSquare + 2) == 'x') || (moveStr.charAt(startOfFromSquare + 2) == '-'))) {
				throw new IllegalArgumentException(
						"invalid input. Expected 'x' or '-' at position " + (startOfFromSquare + 3));
			}
			Square from = Square.fromString(moveStr.substring(startOfFromSquare, startOfFromSquare + 2));
			if (game.getChessboard().pieceAt(from, game.getChessboard().getSideToMove()) != pt) {
				throw new IllegalArgumentException("error: no " + pt + " at square " + from);
			}
			boolean capture = moveStr.charAt(startOfFromSquare + 2) == 'x';
			Square to = Square.fromString(moveStr.substring(startOfFromSquare + 3, startOfFromSquare + 5));
			if (capture) {
				PieceType capturedPiece = game.getChessboard().pieceAt(to,
						Colour.oppositeColour(game.getChessboard().getSideToMove()));
				m = new Move(pt, game.getChessboard().getSideToMove(), from, to, capturedPiece);
			} else {
				m = new Move(pt, game.getChessboard().getSideToMove(), from, to);
			}
			if ((pt == PieceType.PAWN) && (to.rank() == 7)) {
				System.out.println("promote to? ");
				String promote = in.readLine();
				if (promote.length() != 1) {
					throw new IllegalArgumentException("promote piece must be 1 char");
				}
				PieceType promotedPiece = convertStringToPieceType(promote.charAt(0));
				if ((promotedPiece == PieceType.PAWN) || (promotedPiece == PieceType.KING)) {
					throw new IllegalArgumentException("cannot promote to a pawn or a king");
				}
				m.setPromotionPiece(promotedPiece);
			}
		}
		if (moveStr.charAt(moveStr.length() - 1) == '+') {
			m.setCheck(true);
		}

		return m;
	}

	private PieceType convertStringToPieceType(char ch) {
		PieceType pt;
		switch (ch) {
		case 'R':
			pt = PieceType.ROOK;
			break;
		case 'N':
			pt = PieceType.KNIGHT;
			break;
		case 'B':
			pt = PieceType.BISHOP;
			break;
		case 'Q':
			pt = PieceType.QUEEN;
			break;
		case 'K':
			pt = PieceType.KING;
			break;
		default:
			pt = PieceType.PAWN;
			break;
		}
		return pt;
	}

}
