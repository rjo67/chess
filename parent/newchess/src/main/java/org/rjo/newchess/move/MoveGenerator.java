package org.rjo.newchess.move;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rjo.newchess.board.Board;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

public class MoveGenerator {

   private final static int[] kingsSquareIndex = new int[] { Square.e1.index(), Square.e8.index() };
   // stores the rook's squares for kingsside or queensside castling
   private final static int[][] rooksSquareIndex = new int[][] { { Square.h1.index(), Square.h8.index() }, { Square.a1.index(), Square.a8.index() } };

   private final static int[][] unoccupiedSquaresKingssideCastling = new int[][]//
   { { Square.f1.index(), Square.g1.index() }, { Square.f8.index(), Square.g8.index() } };
   private final static int[][] unoccupiedSquaresQueenssideCastling = new int[][]//
   { { Square.b1.index(), Square.c1.index(), Square.d1.index() }, { Square.b8.index(), Square.c8.index(), Square.d8.index() } };
   // key: the enpassant square; values: the squares where a pawn must be in order
   // to take with e.p.
   private final static Map<Integer, Integer[]>[] enpassantSquares;
   static {
      enpassantSquares = new HashMap[2];
      enpassantSquares[0] = new HashMap<>();
      enpassantSquares[0].put(Square.a6.index(), new Integer[] { Square.b5.index() });
      enpassantSquares[0].put(Square.b6.index(), new Integer[] { Square.a5.index(), Square.c5.index() });
      enpassantSquares[0].put(Square.c6.index(), new Integer[] { Square.b5.index(), Square.d5.index() });
      enpassantSquares[0].put(Square.d6.index(), new Integer[] { Square.c5.index(), Square.e5.index() });
      enpassantSquares[0].put(Square.e6.index(), new Integer[] { Square.d5.index(), Square.f5.index() });
      enpassantSquares[0].put(Square.f6.index(), new Integer[] { Square.e5.index(), Square.g5.index() });
      enpassantSquares[0].put(Square.g6.index(), new Integer[] { Square.f5.index(), Square.h5.index() });
      enpassantSquares[0].put(Square.h6.index(), new Integer[] { Square.g5.index() });
      // black
      enpassantSquares[1] = new HashMap<>();
      enpassantSquares[1].put(Square.a3.index(), new Integer[] { Square.b4.index() });
      enpassantSquares[1].put(Square.b3.index(), new Integer[] { Square.a4.index(), Square.c4.index() });
      enpassantSquares[1].put(Square.c3.index(), new Integer[] { Square.b4.index(), Square.d4.index() });
      enpassantSquares[1].put(Square.d3.index(), new Integer[] { Square.c4.index(), Square.e4.index() });
      enpassantSquares[1].put(Square.e3.index(), new Integer[] { Square.d4.index(), Square.f4.index() });
      enpassantSquares[1].put(Square.f3.index(), new Integer[] { Square.e4.index(), Square.g4.index() });
      enpassantSquares[1].put(Square.g3.index(), new Integer[] { Square.f4.index(), Square.h4.index() });
      enpassantSquares[1].put(Square.h3.index(), new Integer[] { Square.g4.index() });
   }

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
                  if (canCastleKingsside(posn, startSq, colour)) {
                     moves.add(Move.kingssideCastle(posn, startSq));
                  }
                  if (canCastleQueensside(posn, startSq, colour)) {
                     moves.add(Move.queenssideCastle(posn, startSq));
                  }
               }
            }
         }
      }
      return moves;
   }

   private boolean canCastleKingsside(Position posn, int startSq, Colour colour) {
      if (startSq != kingsSquareIndex[colour.ordinal()]) {
         return false;
      }
      if (!posn.canCastleKingsside(colour)) {
         return false;
      }
      if (posn.pieceAt(rooksSquareIndex[0][colour.ordinal()]) != PieceType.ROOK) {
         return false;
      }
      for (int sq : unoccupiedSquaresKingssideCastling[colour.ordinal()]) {
         if (posn.colourOfPieceAt(sq) != Colour.UNOCCUPIED) {
            return false;
         }
      }
      return true;
   }

   private boolean canCastleQueensside(Position posn, int startSq, Colour colour) {
      if (startSq != kingsSquareIndex[colour.ordinal()]) {
         return false;
      }
      if (!posn.canCastleQueensside(colour)) {
         return false;
      }
      if (posn.pieceAt(rooksSquareIndex[1][colour.ordinal()]) != PieceType.ROOK) {
         return false;
      }
      for (int sq : unoccupiedSquaresQueenssideCastling[colour.ordinal()]) {
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
      // following cases ('reverse' for black pawns):
      // - pawn on 2nd rank can move 1 or two squares forward
      // - capturing diagonally
      // - pawn on 7th rank can promote
      // - en passant

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
         // NB this square cannot be outside of the board, we've already checked for the
         // 'promotion' rank
         if (posn.colourOfPieceAt(nextSq) == Colour.UNOCCUPIED) {
            moves.add(generateMove(posn, startSq, nextSq));
            if (onPawnStartRank(startSq, colour)) {
               nextSq = getMailboxSquare(nextSq, forwardOffset); // cannot be outside of the board
               if (posn.colourOfPieceAt(nextSq) == Colour.UNOCCUPIED) {
                  moves.add(generateMove(posn, startSq, nextSq));
               }
            }
         }
         // captures
         for (int i = 0; i < 2; i++) {
            addCaptureMoveIfPossible(startSq, captureOffset[i], posn, colour, moves);
         }
      }
      if (posn.getEnpassantSquare() != null) {
         int epSquare = posn.getEnpassantSquare().index();
         for (int sq : enpassantSquares[colour.ordinal()].get(epSquare)) {
            if (startSq == sq) {
               moves.add(Move.enpassant(posn, startSq, epSquare));
            }
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
