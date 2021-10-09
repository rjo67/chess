package org.rjo.newchess.move;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.rjo.newchess.board.Board;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.board.Ray;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

/**
 * @author rich
 *
 */
public class MoveGenerator {

   private boolean verbose;

   private final static int[] kingsCastlingSquareIndex = new int[] { Square.e1.index(), Square.e8.index() };
   // stores the rook's squares for kingsside or queensside castling
   private final static int[][] rooksCastlingSquareIndex = new int[][] { { Square.h1.index(), Square.h8.index() }, { Square.a1.index(), Square.a8.index() } };

   private final static int[][] unoccupiedSquaresKingssideCastling = new int[][]//
   { { Square.f1.index(), Square.g1.index() }, { Square.f8.index(), Square.g8.index() } };
   private final static int[][] unoccupiedSquaresQueenssideCastling = new int[][]//
   { { Square.b1.index(), Square.c1.index(), Square.d1.index() }, { Square.b8.index(), Square.c8.index(), Square.d8.index() } };
   // key: the enpassant square; values: the squares where a pawn must be in order to take with e.p.
   private final static Map<Integer, Integer[]>[] enpassantSquares;
   private final static Set<Integer>[] knightMoves; // stores set of possible knight moves for each square
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

      // knight moves
      knightMoves = new Set[64];
      for (int sq = 0; sq < 64; sq++) {
         knightMoves[sq] = new HashSet<>();
         for (int offset : PieceType.KNIGHT.getMoveOffsets()) {
            int targetSq = getMailboxSquare(sq, offset);
            if (targetSq != -1) { knightMoves[sq].add(targetSq); }
         }
      }
   }

   public MoveGenerator() {
      this(false);
   }

   public MoveGenerator(boolean verbose) {
      this.verbose = verbose;
   }

   public List<Move> findMoves(Position posn, Colour colour) {
      /*
       * Instead of looking at all the squares from 0..63, starts at the kingsSquare and proceeds in ray order first. Then all
       * other squares are processed. This is done to reduce / simplify the amount of work needed later to see if a move left
       * the king in check. 'squaresProcessed' is used to keep track of which squares have been processed during the 'rays'
       * loop.
       */
      boolean[] squaresProcessed = new boolean[64]; // false - not processed
      // stores moves in the ray directions. Moves are stored in the direction of the
      // ray, starting next to the kings square
      List<Move>[] movesWithStartSqOnRay = new List[Ray.values().length];
      List<Move> otherMoves = new ArrayList<>(); // stores moves from all other squares

      int kingsSquare = posn.getKingsSquare(colour);
      for (Ray ray : Ray.values()) {
         movesWithStartSqOnRay[ray.ordinal()] = new ArrayList<>();
         for (int raySq : Ray.raysList[kingsSquare][ray.ordinal()]) {
            processSquare(posn, raySq, colour, movesWithStartSqOnRay[ray.ordinal()]);
            squaresProcessed[raySq] = true;
         }
      }
      // process all other squares
      for (int sq = 0; sq < 64; sq++) {
         if (!squaresProcessed[sq]) { processSquare(posn, sq, colour, otherMoves); }
      }

      // only moves in 'movesWithStartSqOnRay' are relevant here,
      // 'otherMoves' cannot leave king in check by definition
      removeMovesLeavingKingInCheck(posn, posn.getKingsSquare(colour), colour, movesWithStartSqOnRay);

      // collect all the moves
      List<Move> allMoves = new ArrayList<>(64);
      for (Ray ray : Ray.values()) {
         allMoves.addAll(movesWithStartSqOnRay[ray.ordinal()]);
      }
      allMoves.addAll(otherMoves);
      int opponentsKingsSquare = posn.getKingsSquare(colour.opposite());
      for (Move m : allMoves) {
         if (moveAttacksSquare(posn, m, opponentsKingsSquare)) { m.setCheck(); }
      }
      return allMoves;
   }

   /**
    * Returns true if the given move would attack the given square. Can be used e.g. to see if a move will check the
    * opponent's king.
    * 
    * @param  posn
    * @param  move
    * @param  targetSq
    * @return          true if the given move attacks the given square
    */
   private boolean moveAttacksSquare(Position posn, Move move, int targetSq) {
      PieceType movingPiece = move.getMovingPiece();
      if (movingPiece == PieceType.KING) {
         return false;
      } else if (movingPiece == PieceType.PAWN) {
         if (move.isPromotion()) {
            return false; // TODO
         } else {
            return false; // TODO
         }
      } else if (movingPiece == PieceType.KNIGHT) {
         return false;// TODO special case
      }
      // case BISHOP, ROOK, QUEEN:
      Ray rayBetween = Ray.findRayBetween(move.getTarget(), targetSq);
      if (rayBetween == null || !move.getMovingPiece().canSlideAlongRay(rayBetween)) { return false; }
      // the piece is moving to a square where it could attack the targetSq.
      // Finally need to check if there is a clear path to the targetSq.
      return interveningSquaresAreEmpty(posn, move.getTarget(), targetSq, rayBetween);
   }

   private void processSquare(Position posn, int startSq, Colour colour, List<Move> moves) {
      if (posn.colourOfPieceAt(startSq) == colour) {
         PieceType pt = posn.pieceAt(startSq);
         if (pt == PieceType.PAWN) {
            moves.addAll(generatePawnMoves(startSq, posn, colour));
         } else if (pt == PieceType.KNIGHT) {
            moves.addAll(generateKnightMoves(posn, startSq, colour));
         } else {
            for (int offset : pt.getMoveOffsets()) { // process each square along the ray
               int nextSq = startSq;
               while (true) {
                  nextSq = getMailboxSquare(nextSq, offset);
                  if (nextSq == -1) {
                     break; // outside board
                  }
                  Move move = potentiallyGenerateMoveOrCapture(posn, startSq, nextSq, colour);
                  // stop processing ray if null move (a friendly piece is occupying this ray) or a capture
                  if (move != null) {
                     moves.add(move);
                     if (move.isCapture()) { break; }
                  } else {
                     // null move ==> a friendly piece is occupying this ray
                     break;
                  }
                  if (!pt.isSlidingPiece()) {
                     break; // next ray
                  }
               }
            }
            if (pt == PieceType.KING) {
               if (canCastleKingsside(posn, startSq, colour)) { moves.add(Move.kingssideCastle(posn, startSq)); }
               if (canCastleQueensside(posn, startSq, colour)) { moves.add(Move.queenssideCastle(posn, startSq)); }
            }
         }
      }
   }

   /**
    * Potentially generates a move object from originSq to targetSq.
    * 
    * @param  posn
    * @param  startSq
    * @param  targetSq targetSq. If empty, the object generated will be a plain move. If contains an enemy piece, the move
    *                  will be a capture. If contains a friendly piece, no move object will be generated.
    * @param  colour
    * @return          a move object or null if the targetSq contains a piece of our colour
    */
   private Move potentiallyGenerateMoveOrCapture(Position posn, int startSq, int targetSq, Colour colour) {
      Move move = null;
      final Colour colourOfTargetSq = posn.colourOfPieceAt(targetSq);
      if (colourOfTargetSq != Colour.UNOCCUPIED) {
         // either capture, or we've hit one of our own pieces
         if (colour.opposes(colourOfTargetSq)) { move = generateCapture(posn, startSq, targetSq); }
      } else {
         move = generateMove(posn, startSq, targetSq);
      }
      return move;
   }

   private List<Move> generateKnightMoves(Position posn, int startSq, Colour colour) {
      List<Move> moves = new ArrayList<>();
      for (int targetSq : knightMoves[startSq]) {
         Move move = potentiallyGenerateMoveOrCapture(posn, startSq, targetSq, colour);
         if (move != null) { moves.add(move); }
      }
      return moves;
   }

   /**
    * Is the king in check after the given move? I.e. is there an enemy piece capable of giving check on the appropriate
    * ray.
    * 
    * @param  posn
    * @param  m
    * @param  ray
    * @param  kingsSquare
    * @param  colour
    * @param  pinnedPieces stores the squares of pinned pieces; use new int[] { -1, -1, -1, -1, -1, -1, -1, -1 } if not
    *                      applicable.
    * @return              true if the given move leaves the king in check
    */
   private boolean moveLeavesKingInCheck(Position posn, Move m, Ray ray, int kingsSquare, Colour colour, int[] pinnedPieces) {
      // return immediately if this move involves the pinned piece
      if (m.getOrigin() == pinnedPieces[ray.ordinal()]) {
         // but a move along the ray is still ok
         if (moveOnSameRayOrOpposite(m, ray)) {
            if (verbose) { System.out.println(String.format("move %s with pinned piece is ok because along same ray as pin", m)); }
            return false;
         } else {
            if (verbose) {
               System.out.println(String.format("removed move %s because piece at %s is pinned", m, Square.toSquare(pinnedPieces[ray.ordinal()])));
            }
            return true;
         }
      }
      // no further processing if this move is 'further' along a ray where a pinned
      // piece has already been found (nb the movesOnRay list is sorted with squares
      // closest to king first)
      if (pinnedPieces[ray.ordinal()] != -1) {
         if (verbose) {
            System.out.println(String.format("skipped processing move %s because piece at %s is pinned", m, Square.toSquare(pinnedPieces[ray.ordinal()])));
         }
         return false;
      }
      if (verbose) {
         System.out.println(String.format("processing move %s: sq %s is on ray %s from King (%s)", m, Square.toSquare(m.getOrigin()), ray.getAbbreviation(),
               Square.toSquare(kingsSquare)));
      }
      // If there's a piece between this piece and the king, then the king cannot be
      // left in check, so don't process this move anymore
      // TODO could optimize here and store the result of this analysis
      if (!interveningSquaresAreEmpty(posn, kingsSquare, m.getOrigin(), ray)) { return false; }
      // .. otherwise see if there's an enemy piece on this ray
      Pair<PieceType, Integer> enemyPieceInfo = opponentsPieceOnRay(posn, colour, m.getOrigin(), ray);
      PieceType enemyPiece = enemyPieceInfo.getLeft();
      if (enemyPiece != null) {
         int enemySq = enemyPieceInfo.getRight();
         if (verbose) {
            System.out.println(String.format(".. found enemy piece %s at %s on same ray whilst processing move %s", enemyPiece, Square.toSquare(enemySq), m));
         }
         // ... which is capable of checking the king
         if (enemyPiece.canSlideAlongRay(ray)) {
            pinnedPieces[ray.ordinal()] = m.getOrigin();
            if (!moveOnSameRayOrOpposite(m, ray)) {
               if (verbose) {
                  System.out.println(String.format("piece at %s is pinned by %s at %s", Square.toSquare(m.getOrigin()), enemyPiece, Square.toSquare(enemySq)));
               }
               return true;
            } else {
               if (verbose) { System.out.println(String.format("move %s with pinned piece is ok because along same ray as pin", m)); }
               return false;
            }
         }
      }
      return false;
   }

   /**
    * Are all squares between origin and target empty?
    * 
    * @param  origin start square (e.g. kings square)
    * @param  target target square (e.g. move's origin square)
    * @param  ray    the required ray to check
    * @return        true if all squares between origin and target along the given ray are empty
    */
   private boolean interveningSquaresAreEmpty(Position posn, int origin, int target, Ray ray) {
      for (int interveningSq : Ray.raysList[origin][ray.ordinal()]) {
         if (interveningSq == target) { return true; }
         if (!posn.isEmpty(interveningSq)) { return false; }
      }
      // if get here, than have either hit the target square, or the ray is finished
      // (which would mean the origin and target are not on the same ray)
      return true;
   }

   /**
    * Returns the square and type of an enemy piece on the given ray, starting from (but not including) startSq. If an
    * intervening piece of my colour is found first, returns (null, -1).
    * 
    * @param  posn
    * @param  myColour my colour
    * @param  startSq  where to start
    * @param  ray      direction
    * @return          the piece-type and square of the enemy piece, if found. If no piece or one of my pieces was found,
    *                  returns (null, -1)
    */
   private Pair<PieceType, Integer> opponentsPieceOnRay(Position posn, Colour myColour, int startSq, Ray ray) {
      int enemySq = -1;
      PieceType enemyPiece = null;
      for (int potentialEnemySq : Ray.raysList[startSq][ray.ordinal()]) {
         Colour colourOfSq = posn.colourOfPieceAt(potentialEnemySq);
         if (colourOfSq == Colour.UNOCCUPIED) { continue; }
         if (myColour.opposes(colourOfSq)) {
            enemyPiece = posn.pieceAt(potentialEnemySq);
            enemySq = potentialEnemySq;
         }
         break; // can stop in any case, having found a piece
      }
      return Pair.of(enemyPiece, enemySq);
   }

   /**
    * Processes all moves on the given ray (must be sorted with origin squares closest to kingsSquare occurring first).
    * 
    * @param posn
    * @param kingsSquare
    * @param colour
    * @param movesOnRay  the moves to inspect, stored for each ray as seen from the king's square, sorted with origin
    *                    squares closest to king first
    */
   private void removeMovesLeavingKingInCheck(Position posn, int kingsSquare, Colour colour, List<Move>[] movesOnRay) {
      int[] pinnedPieces = new int[] { -1, -1, -1, -1, -1, -1, -1, -1 }; // stores squares of pinned pieces for each ray
      for (Ray ray : Ray.values()) {
         Iterator<Move> moveIter = movesOnRay[ray.ordinal()].iterator();
         while (moveIter.hasNext()) {
            Move m = moveIter.next();
            if (moveLeavesKingInCheck(posn, m, ray, kingsSquare, colour, pinnedPieces)) { moveIter.remove(); }
         }
      }
   }

   /**
    * @param  m   move
    * @param  ray ray
    * @return     true if the given move is along the given ray (or its opposite) (i.e. the piece is not pinned)
    */
   private boolean moveOnSameRayOrOpposite(Move m, Ray ray) {
      return ray.onSameRay(m.getOrigin(), m.getTarget()) || ray.getOpposite().onSameRay(m.getOrigin(), m.getTarget());
   }

   private boolean canCastleKingsside(Position posn, int startSq, Colour colour) {
      // TODO cannot castle over a square in check...

      if (startSq != kingsCastlingSquareIndex[colour.ordinal()]) { return false; }
      if (!posn.canCastleKingsside(colour)) { return false; }
      if (posn.pieceAt(rooksCastlingSquareIndex[0][colour.ordinal()]) != PieceType.ROOK) { return false; }
      for (int sq : unoccupiedSquaresKingssideCastling[colour.ordinal()]) {
         if (posn.colourOfPieceAt(sq) != Colour.UNOCCUPIED) { return false; }
      }
      return true;
   }

   private boolean canCastleQueensside(Position posn, int startSq, Colour colour) {
      // TODO cannot castle over a square in check...

      if (startSq != kingsCastlingSquareIndex[colour.ordinal()]) { return false; }
      if (!posn.canCastleQueensside(colour)) { return false; }
      if (posn.pieceAt(rooksCastlingSquareIndex[1][colour.ordinal()]) != PieceType.ROOK) { return false; }
      for (int sq : unoccupiedSquaresQueenssideCastling[colour.ordinal()]) {
         if (posn.colourOfPieceAt(sq) != Colour.UNOCCUPIED) { return false; }
      }
      return true;
   }

   // given a square e.g. 0, corresponding to a8, and an offset, e.g. +10, returns
   // the corresponding square in the mailbox data type:
   // : 0 in mailbox64 == 21
   // : 21 + offset +10 == 31
   // : 31 in mailbox == 8 -- corresponds to a7
   private static int getMailboxSquare(int square, int offset) {
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
               if (posn.colourOfPieceAt(nextSq) == Colour.UNOCCUPIED) { moves.add(generateMove(posn, startSq, nextSq)); }
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
            if (startSq == sq) { moves.add(Move.enpassant(posn, startSq, epSquare)); }
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
         if (colour.opposes(colourOfTargetSq)) { moves.add(generateCapture(posn, from, to)); }
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
