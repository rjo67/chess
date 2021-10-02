package org.rjo.newchess.move;

import java.util.ArrayList;
import java.util.List;

import org.rjo.newchess.board.Board;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

public class MoveGenerator {

   private final static int[][] unoccupiedSquaresKingssideCastling = new int[][]//
   { { Square.f1.index(), Square.g1.index() }, { Square.f8.index(), Square.g8.index() } };
   private final static int[][] unoccupiedSquaresQueenssideCastling = new int[][]//
   { { Square.b1.index(), Square.c1.index(), Square.d1.index() }, { Square.b8.index(), Square.c8.index(), Square.d8.index() } };

   public List<Move> findMoves(Position posn, Colour colour) {
      List<Move> moves = new ArrayList<>();
      for (int startSq = 0; startSq < 64; startSq++) {
         if (posn.colourOfPieceAt(startSq) == colour) {
            PieceType pt = posn.pieceAt(startSq);
            if (pt == PieceType.PAWN) {
               moves.addAll(generatePawnMoves(startSq, posn, colour));
            } else {
               for (int offset : pt.getMoveOffsets()) { // process each square along the ray
                  int nextSq = startSq;
                  while (true) {
                     nextSq = getMailboxSquare(nextSq, offset);
                     if (nextSq == -1) {
                        break; // outside board
                     }
                     Colour colourOfTargetSq = posn.colourOfPieceAt(nextSq);
                     if (colourOfTargetSq != Colour.UNOCCUPIED) {
                        // either capture, or we've hit one of our own pieces
                        if (colour.opposes(colourOfTargetSq)) {
                           moves.add(generateCapture(posn, startSq, nextSq));
                        }
                        break; // stop processing this ray in either case
                     } else {
                        moves.add(generateMove(posn, startSq, nextSq));
                     }
                     if (!pt.isSlidingPiece()) {
                        break; // next ray
                     }
                  }
               }
               if (pt == PieceType.KING) {
                  if (canCastleKingsside(posn, colour)) {
                     moves.add(Move.kingssideCastle(posn, startSq));
                  }
                  if (canCastleQueensside(posn, colour)) {
                     moves.add(Move.queenssideCastle(posn, startSq));
                  }
               }
            }
         }
      }
      return moves;
   }

   private boolean canCastleKingsside(Position posn, Colour colour) {
      final int rooksSquareIndex = colour == Colour.WHITE ? Square.h1.index() : Square.h8.index();

      if (!posn.canCastleKingsside(colour)) {
         return false;
      }
      if (posn.pieceAt(rooksSquareIndex) != PieceType.ROOK) {
         return false;
      }
      for (int sq : unoccupiedSquaresKingssideCastling[colour.ordinal() - 1]) {
         if (posn.colourOfPieceAt(sq) != Colour.UNOCCUPIED) {
            return false;
         }
      }
      return true;
   }

   private boolean canCastleQueensside(Position posn, Colour colour) {
      final int rooksSquareIndex = colour == Colour.WHITE ? Square.a1.index() : Square.a8.index();

      if (!posn.canCastleQueensside(colour)) {
         return false;
      }
      if (posn.pieceAt(rooksSquareIndex) != PieceType.ROOK) {
         return false;
      }
      for (int sq : unoccupiedSquaresQueenssideCastling[colour.ordinal() - 1]) {
         if (posn.colourOfPieceAt(sq) != Colour.UNOCCUPIED) {
            return false;
         }
      }
      return true;
   }

   // given a square e.g. 0, corresponding to a8, and an offset, e.g. +10, returns
   // the corresponding square in the mailbox data type:
   // : 0 in mailbox64 == 21
   // : 21 + offset +10 == 31
   // : 31 in mailbox == 8 -- corresponds to a7
   private int getMailboxSquare(int square, int offset) {
      return Board.mailbox(Board.mailbox64(square) + offset);
   }

   private List<Move> generatePawnMoves(int startSq, Position posn, Colour colour) {
      List<Move> moves = new ArrayList<>();
      // 3 cases ('reverse' for black pawns):
      // - pawn on 2nd rank can move 1 or two squares forward
      // - capturing diagonally
      // - pawn on 7th rank can promote

      int forwardOffset;
      int[] captureOffset;
      switch (colour) {
      case WHITE:
         forwardOffset = -10;
         captureOffset = new int[] { -9, -11 };
         break;
      case BLACK:
         forwardOffset = 10;
         captureOffset = new int[] { 9, 11 };
         break;
      default:
         throw new IllegalArgumentException(
               String.format("cannot generate pawn moves for an unoccupied square: %d, colour=%s, posn=%s", startSq, colour.toString(), posn));
      }
      // promotion
      if (onPawnPromotionRank(startSq, colour)) {
         int nextSq = getMailboxSquare(startSq, forwardOffset);
         moves.addAll(generatePossibleNormalPromotionMoves(posn, startSq, nextSq, colour, posn.colourOfPieceAt(nextSq)));
         for (int i = 0; i < 2; i++) {
            nextSq = getMailboxSquare(startSq, captureOffset[i]);
            moves.addAll(generatePossibleCapturePromotionMoves(posn, startSq, nextSq, colour, posn.colourOfPieceAt(nextSq)));
         }
      } else {
         // 1 square forward, optionally followed by 2 squares forward
         int nextSq = getMailboxSquare(startSq, forwardOffset);
         // NB this 'if' is not really required, we've already checked for the
         // 'promotion' rank
         if (nextSq != -1) { // not outside board
            Colour colourOfTargetSq = posn.colourOfPieceAt(nextSq);
            if (colourOfTargetSq == Colour.UNOCCUPIED) {
               moves.add(generateMove(posn, startSq, nextSq));
               if (onPawnStartRank(startSq, colour)) {
                  nextSq = getMailboxSquare(nextSq, forwardOffset); // cannot be outside of the board
                  if (colourOfTargetSq == Colour.UNOCCUPIED) {
                     moves.add(generateMove(posn, startSq, nextSq));
                  }
               }
            }
         }
         // captures
         for (int i = 0; i < 2; i++) {
            addCaptureMoveIfPossible(startSq, captureOffset[i], posn, colour, moves);
         }
      }
      return moves;

   }

   // caters for pawn advancing one square and promoting
   private List<Move> generatePossibleNormalPromotionMoves(Position posn, int from, int to, Colour myColour, Colour colourOfTargetSq) {
      List<Move> moves = new ArrayList<>();
      if (colourOfTargetSq == Colour.UNOCCUPIED) {
         for (PieceType pt : new PieceType[] { PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN }) {
            moves.add(new Move(from, posn.raw(from), to, -1, pt));
         }
      }
      return moves;
   }

   // caters for pawn capturing a piece and promoting
   private List<Move> generatePossibleCapturePromotionMoves(Position posn, int from, int to, Colour myColour, Colour colourOfTargetSq) {
      List<Move> moves = new ArrayList<>();
      if (myColour.opposes(colourOfTargetSq)) {
         for (PieceType pt : new PieceType[] { PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN }) {
            moves.add(new Move(from, posn.raw(from), to, posn.raw(to), pt));
         }
      }
      return moves;
   }

   private void addCaptureMoveIfPossible(int from, int offset, Position posn, Colour colour, List<Move> moves) {
      int to = getMailboxSquare(from, offset);
      if (to != -1) { // not outside board
         Colour colourOfTargetSq = posn.colourOfPieceAt(to);
         if (colour.opposes(colourOfTargetSq)) {
            moves.add(generateCapture(posn, from, to));
         }
      }
   }

   private boolean onPawnStartRank(int startSq, Colour colour) {
      if (colour == Colour.WHITE) {
         return startSq >= 48 && startSq <= 55;
      } else {
         return startSq >= 8 && startSq <= 15;
      }
   }

   private boolean onPawnPromotionRank(int startSq, Colour colour) {
      if (colour == Colour.WHITE) {
         return startSq >= 8 && startSq <= 15;
      } else {
         return startSq >= 48 && startSq <= 55;
      }
   }

   private Move generateMove(Position posn, int from, int to) {
      return new Move(from, posn.raw(from), to);
   }

   private Move generateCapture(Position posn, int from, int to) {
      return new Move(from, posn.raw(from), to, posn.raw(to));
   }

}
