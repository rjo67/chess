package org.rjo.chess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.rjo.chess.pieces.PieceType;

public class PlayGame {

   public static void main(String[] args) throws IOException {
      PlayGame p = new PlayGame();
      p.run();
   }

   private void run() throws IOException {
      Game game = new Game();
      Random rand = new Random();
      try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
         boolean finished = false;
         while (!finished) {
            System.out.print(game.getMoveNumber() + ":  ");
            try {
               Move humanMove = getMove(game, in);
               game.move(humanMove); // TODO check for a legal move
               List<Move> computerMoves = game.findMoves(game.getSideToMove());
               if (computerMoves.size() == 0) {
                  finished = true;
                  if (humanMove.isCheck()) {
                     System.out.println("checkmate!");
                  } else {
                     System.out.println("stalemate!");
                  }
               } else {
                  List<Move> computerCaptures = new ArrayList<>();
                  List<Move> computerChecks = new ArrayList<>();
                  for (Move move : computerMoves) {
                     if (move.isCapture()) {
                        computerCaptures.add(move);
                     }
                     if (move.isCheck()) {
                        computerChecks.add(move);
                     }
                  }
                  System.out.println("[" + computerMoves.size() + " (" + computerCaptures.size() + "x, "
                        + computerChecks.size() + "+)]");
                  Move computerMove;
                  int PREFER_CHECKS = 70; // prefer checks x% of the time
                  int PREFER_CAPTURES = 60; // prefer captures x% of the time
                  if ((computerChecks.size() != 0) && ((rand.nextInt(100) + 1) <= PREFER_CHECKS)) {
                     computerMove = computerChecks.get(rand.nextInt(computerChecks.size()));
                  } else if ((computerCaptures.size() != 0) && ((rand.nextInt(100) + 1) <= PREFER_CAPTURES)) {
                     computerMove = computerCaptures.get(rand.nextInt(computerCaptures.size()));
                  } else {
                     computerMove = computerMoves.get(rand.nextInt(computerMoves.size()));
                  }
                  System.out.println(game.getMoveNumber() + "... " + computerMove);
                  game.move(computerMove);
               }
            } catch (IllegalArgumentException x) {
               System.out.println(x.getMessage());
            }
         }
      }
   }

   private Move getMove(Game game, BufferedReader in) throws IllegalArgumentException, IOException {
      String moveStr = in.readLine();
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
         throw new IllegalArgumentException("invalid input. Expected 'x' or '-' at position " + (startOfFromSquare + 3));
      }
      Square from = Square.fromString(moveStr.substring(startOfFromSquare, startOfFromSquare + 2));
      if (game.getChessboard().pieceAt(from, game.getSideToMove()) != pt) {
         throw new IllegalArgumentException("error: no " + pt + " at square " + from);
      }
      boolean capture = moveStr.charAt(startOfFromSquare + 2) == 'x';
      Square to = Square.fromString(moveStr.substring(startOfFromSquare + 3, startOfFromSquare + 5));
      Move m;
      if (capture) {
         PieceType capturedPiece = game.getChessboard().pieceAt(to, Colour.oppositeColour(game.getSideToMove()));
         m = new Move(pt, game.getSideToMove(), from, to, capturedPiece);
      } else {
         m = new Move(pt, game.getSideToMove(), from, to);
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
