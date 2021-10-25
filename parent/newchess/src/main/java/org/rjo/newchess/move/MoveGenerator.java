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
import org.rjo.newchess.game.Position.CheckInfo;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Piece;

/**
 * @author rich
 *
 */
/**
 * @author rich
 *
 */
public class MoveGenerator {

   private boolean verbose;

   // **** Note
   // the first dimension of all these arrays is indexed on colour (w/b)
   // ****

   // square where the king must be to be able to castle
   public final static int[] kingsCastlingSquareIndex = new int[] { Square.e1.index(), Square.e8.index() };
   // square where the king ends up after castling kings or queensside, indexed on colour
   public final static int[][] kingsSquareAfterCastling = new int[][] { { Square.g1.index(), Square.c1.index() }, { Square.g8.index(), Square.c8.index() } };
   // square where the rook ends up after castling kings or queensside, indexed on colour and side of board
   public final static int[][] rooksSquareAfterCastling = new int[][] { { Square.f1.index(), Square.d1.index() }, { Square.f8.index(), Square.d8.index() } };
   // stores the rook's squares for kingsside or queensside castling, indexed on colour and side of board
   public final static int[][] rooksCastlingSquareIndex = new int[][] { { Square.h1.index(), Square.a1.index() }, { Square.h8.index(), Square.a8.index() } };
   // squares which must be unoccupied in order to castle kingsside
   private final static int[][] unoccupiedSquaresKingssideCastling = new int[][]//
   { { Square.f1.index(), Square.g1.index() }, { Square.f8.index(), Square.g8.index() } };
   // if an enemy knight is on these squares, then cannot castle kingsside
   private final static int[][] knightSquaresKingssideCastling = new int[][]//
   { { Square.d2.index(), Square.e3.index(), Square.g3.index(), Square.h2.index(), Square.e2.index(), Square.f3.index(), Square.h3.index() },
         { Square.d7.index(), Square.e6.index(), Square.g6.index(), Square.h7.index(), Square.e7.index(), Square.f6.index(), Square.h6.index() } };
   // if an enemy pawn is on these squares, then cannot castle kingsside
   private final static int[][] pawnSquaresKingssideCastling = new int[][]//
   { { Square.e2.index(), Square.f2.index(), Square.g2.index(), Square.h2.index() },
         { Square.e7.index(), Square.f7.index(), Square.g7.index(), Square.h7.index() } };
   // squares which must be unoccupied in order to castle queensside
   private final static int[][] unoccupiedSquaresQueenssideCastling = new int[][]//
   { { Square.b1.index(), Square.c1.index(), Square.d1.index() }, { Square.b8.index(), Square.c8.index(), Square.d8.index() } };
   // if an enemy knight is on these squares, then cannot castle queensside (not including c2/c7, since a knight on that
   // square checks the king)
   private final static int[][] knightSquaresQueenssideCastling = new int[][]//
   { { Square.b3.index(), Square.c3.index(), Square.d3.index(), Square.e3.index(), Square.a2.index(), Square.b2.index(), Square.e2.index(), Square.f2.index() },
         { Square.b6.index(), Square.c6.index(), Square.d6.index(), Square.e6.index(), Square.a7.index(), Square.b7.index(), Square.e7.index(),
               Square.f7.index() } };
   // if an enemy pawn is on these squares, then cannot castle queensside
   private final static int[][] pawnSquaresQueenssideCastling = new int[][]//
   { { Square.b2.index(), Square.c2.index(), Square.d2.index(), Square.e2.index() },
         { Square.b7.index(), Square.c7.index(), Square.d7.index(), Square.e7.index() } };
   // key: the enpassant square; values: the squares where a pawn must be in order to take with e.p.
   private final static Map<Integer, Integer[]>[] enpassantSquares;
   public final static Set<Integer>[] knightMoves; // stores set of possible knight moves for each square
   public final static Set<Integer>[][] pawnCaptures; // stores set of possible pawn captures for each square (for w/b)
   static {
      enpassantSquares = new HashMap[2];
      enpassantSquares[Colour.WHITE.ordinal()] = new HashMap<>();
      enpassantSquares[Colour.WHITE.ordinal()].put(Square.a6.index(), new Integer[] { Square.b5.index() });
      enpassantSquares[Colour.WHITE.ordinal()].put(Square.b6.index(), new Integer[] { Square.a5.index(), Square.c5.index() });
      enpassantSquares[Colour.WHITE.ordinal()].put(Square.c6.index(), new Integer[] { Square.b5.index(), Square.d5.index() });
      enpassantSquares[Colour.WHITE.ordinal()].put(Square.d6.index(), new Integer[] { Square.c5.index(), Square.e5.index() });
      enpassantSquares[Colour.WHITE.ordinal()].put(Square.e6.index(), new Integer[] { Square.d5.index(), Square.f5.index() });
      enpassantSquares[Colour.WHITE.ordinal()].put(Square.f6.index(), new Integer[] { Square.e5.index(), Square.g5.index() });
      enpassantSquares[Colour.WHITE.ordinal()].put(Square.g6.index(), new Integer[] { Square.f5.index(), Square.h5.index() });
      enpassantSquares[Colour.WHITE.ordinal()].put(Square.h6.index(), new Integer[] { Square.g5.index() });
      enpassantSquares[Colour.BLACK.ordinal()] = new HashMap<>();
      enpassantSquares[Colour.BLACK.ordinal()].put(Square.a3.index(), new Integer[] { Square.b4.index() });
      enpassantSquares[Colour.BLACK.ordinal()].put(Square.b3.index(), new Integer[] { Square.a4.index(), Square.c4.index() });
      enpassantSquares[Colour.BLACK.ordinal()].put(Square.c3.index(), new Integer[] { Square.b4.index(), Square.d4.index() });
      enpassantSquares[Colour.BLACK.ordinal()].put(Square.d3.index(), new Integer[] { Square.c4.index(), Square.e4.index() });
      enpassantSquares[Colour.BLACK.ordinal()].put(Square.e3.index(), new Integer[] { Square.d4.index(), Square.f4.index() });
      enpassantSquares[Colour.BLACK.ordinal()].put(Square.f3.index(), new Integer[] { Square.e4.index(), Square.g4.index() });
      enpassantSquares[Colour.BLACK.ordinal()].put(Square.g3.index(), new Integer[] { Square.f4.index(), Square.h4.index() });
      enpassantSquares[Colour.BLACK.ordinal()].put(Square.h3.index(), new Integer[] { Square.g4.index() });

      knightMoves = new Set[64];
      for (int sq = 0; sq < 64; sq++) {
         knightMoves[sq] = new HashSet<>();
         for (int offset : Piece.KNIGHT.getMoveOffsets()) {
            int targetSq = getMailboxSquare(sq, offset);
            if (targetSq != -1) { knightMoves[sq].add(targetSq); }
         }
      }

      pawnCaptures = new Set[2][64];
      int[][] captureOffset = new int[][] { { -9, -11 }, { 9, 11 } };
      for (Colour col : new Colour[] { Colour.WHITE, Colour.BLACK }) {
         // process first and last rank as well, need this squares defined for kingIsInCheckAfterMove()
         for (int sq = 0; sq < 64; sq++) {
            pawnCaptures[col.ordinal()][sq] = new HashSet<>();
            for (int offset : captureOffset[col.ordinal()]) {
               int targetSq = getMailboxSquare(sq, offset);
               if (targetSq != -1) { pawnCaptures[col.ordinal()][sq].add(targetSq); }
            }
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
       * our king in check. The 'squaresProcessed' array is used to keep track of which squares have been processed during the
       * 'rays' loop.
       */
      boolean[] squaresProcessed = new boolean[64]; // false - not processed
      // stores moves in the ray directions. Moves are stored in the direction of the
      // ray, starting next to the kings square
      List<Move>[] movesWithStartSqOnRay = new List[Ray.values().length];
      List<Move> otherMoves = new ArrayList<>(); // stores moves from all other squares
      List<Move> kingMoves = new ArrayList<>(8); // stores king moves

      var kingInCheck = posn.isKingInCheck();
      var kingInDoubleCheck = kingInCheck && posn.getCheckSquares().size() == 2;

      int kingsSquare = posn.getKingsSquare(colour);
      if (kingInDoubleCheck) {
         processSquare(posn, kingsSquare, colour, otherMoves, kingMoves);
      } else {
         for (Ray ray : Ray.values()) {
            movesWithStartSqOnRay[ray.ordinal()] = new ArrayList<>();
            for (int raySq : Ray.raysList[kingsSquare][ray.ordinal()]) {
               processSquare(posn, raySq, colour, movesWithStartSqOnRay[ray.ordinal()], null);
               squaresProcessed[raySq] = true;
            }
         }
         // process all other squares
         for (int sq = 0; sq < 64; sq++) {
            if (!squaresProcessed[sq]) { processSquare(posn, sq, colour, otherMoves, kingMoves); }
         }

         // If we are already in check, then process all non-king moves to see if they block the check (or capture the checking
         // piece)
         if (kingInCheck) {
            for (Ray ray : Ray.values()) {
               Iterator<Move> moveIter = movesWithStartSqOnRay[ray.ordinal()].iterator();
               while (moveIter.hasNext()) {
                  Move move = moveIter.next();
                  if (!moveBlocksCheck(posn, move, kingsSquare, colour)) { moveIter.remove(); }
               }
            }
            Iterator<Move> moveIter = otherMoves.iterator();
            while (moveIter.hasNext()) {
               Move move = moveIter.next();
               if (!moveBlocksCheck(posn, move, kingsSquare, colour)) { moveIter.remove(); }
            }
         } else { // king not in check
            if (canCastleKingsside(posn, colour)) { kingMoves.add(Move.createKingssideCastlingMove(colour)); }
            if (canCastleQueensside(posn, colour)) { kingMoves.add(Move.createQueenssideCastlingMove(colour)); }
         }

      }

      // remove moves along rays to king if the piece is pinned
      removeMovesLeavingKingInCheckAlongRay(posn, posn.getKingsSquare(colour), colour, movesWithStartSqOnRay);

      // process king moves to make sure the king hasn't moved into check
      Iterator<Move> kingMoveIter = kingMoves.iterator();
      while (kingMoveIter.hasNext()) {
         Move kingsMove = kingMoveIter.next();
         if (kingIsInCheckAfterKingsMove(posn, kingsMove, colour)) { kingMoveIter.remove(); }
      }

      // collect all the moves
      List<Move> allMoves = new ArrayList<>(64);
      for (Ray ray : Ray.values()) {
         allMoves.addAll(movesWithStartSqOnRay[ray.ordinal()]);
      }
      allMoves.addAll(otherMoves);
      // could process king moves separately in the following code, but not really necessary,
      // since isKingInCheckAfterMove(...) copes with king moves as well
      allMoves.addAll(kingMoves);

      // now process checks against _opposing_ king
      RayCacheInfo[] targetSquaresWhichAttackKing = new RayCacheInfo[64]; // this stores the result of processed squares (for sliding pieces)
      int opponentsKingsSquare = posn.getKingsSquare(colour.opposite());
      for (Move m : allMoves) {
         List<CheckInfo> checkSquares = isKingInCheckAfterMove(posn, m, opponentsKingsSquare, colour.opposite());
         if (!checkSquares.isEmpty()) { m.setCheck(checkSquares); }
      }

      return allMoves;
   }

   /**
    * Only called when the king is already in check. The given move must therefore either capture the checking piece or
    * block the check.
    * 
    * @param  posn        position. **King must be in check**
    * @param  move
    * @param  kingsSquare
    * @param  colour
    * @return             true if the move sucessfully blocks the check (or captures the checking piece)
    */
   private boolean moveBlocksCheck(Position posn, Move move, int kingsSquare, Colour colour) {
      List<CheckInfo> checkSquares = posn.getCheckSquares();
      if (checkSquares.isEmpty()) { throw new IllegalStateException("no check squares specified, move " + move + ", posn: " + posn); }
      if (checkSquares.size() == 2) { return false; } // a double check cannot be blocked
      CheckInfo checkInfo = checkSquares.get(0);
      int checkSquare = checkInfo.square();
      // (1) does the move capture the piece?
      if (move.isCapture()) {
         // can the checking piece be captured?
         if (move.getTarget() == checkSquare) { return true; }
         // enpassant
         if (move.getMovingPiece() == Piece.PAWN && move.isEnpassant() && move.getSquareOfPawnCapturedEnpassant() == checkSquare) { return true; }
      }
      // (2) does the move block the check?
      if (checkInfo.piece() == Piece.PAWN || checkInfo.piece() == Piece.KNIGHT) { return false; } // cannot block these checks
      Ray moveRay = Ray.findRayBetween(kingsSquare, move.getTarget());
      if (moveRay == null) { return false; } // can't block a check if there's no ray between the moved piece and the king
      if (checkInfo.rayToKing() == null) { checkInfo.setRayToKing(Ray.findRayBetween(kingsSquare, checkSquare)); }
      if (moveRay != checkInfo.rayToKing()) { return false; } // not on same ray
      return moveRay.squareBetween(move.getTarget(), kingsSquare, checkSquare);
   }

   /**
    * Processes king's moves. Returns true if the king is (still) in check after moving.
    * 
    * @param  posn      current posn
    * @param  kingsMove the king's move
    * @param  colour    king's colour
    * @return           true if in check after move
    */
   /* package protected */ boolean kingIsInCheckAfterKingsMove(Position posn, Move kingsMove, Colour colour) {
      int captureSquare = kingsMove.isCapture() ? kingsMove.getTarget() : -1;
      Colour opponentsColour = colour.opposite();
      if (posn.isKingInCheck()) {
         // if already in check, cannot move along the same ray as the checker (unless it's a capture)
         List<CheckInfo> checkSquares = posn.getCheckSquares();
         for (CheckInfo checkInfo : checkSquares) {
            // ignore if a pawn (can move away from pawn on the same ray w/o any problem)
            if (checkInfo.piece() == Piece.PAWN) { continue; }
            Ray rayBeforeMove = Ray.findRayBetween(kingsMove.getOrigin(), checkInfo.square());
            // ignore if not on ray (==> knight check)
            if (rayBeforeMove != null) {
               Ray rayAfterMove = Ray.findRayBetween(kingsMove.getTarget(), checkInfo.square());
               if (rayBeforeMove == rayAfterMove && captureSquare != checkInfo.square()) { return true; }
               if (rayAfterMove != null && rayBeforeMove == rayAfterMove.getOpposite()) { return true; }
            }
         }
      }

      for (int sq : pawnCaptures[colour.ordinal()][kingsMove.getTarget()]) {
         if (sq != captureSquare && posn.pieceAt(sq) == Piece.PAWN && posn.colourOfPieceAt(sq) == opponentsColour) { return true; }
      }

      for (int sq : knightMoves[kingsMove.getTarget()]) {
         if (sq != captureSquare && posn.pieceAt(sq) == Piece.KNIGHT && posn.colourOfPieceAt(sq) == opponentsColour) { return true; }
      }

      // now need to process all rays from the new king's square.
      // *** Careful, the position still stores the _old_ king's position.

      for (Ray ray : Ray.values()) {
         Pair<Piece, Integer> enemyPieceInfo = posn.opponentsPieceOnRay(colour, kingsMove.getTarget(), ray);
         Piece enemyPiece = enemyPieceInfo.getLeft();
         int enemySquare = enemyPieceInfo.getRight();
         if (enemyPiece != null && enemySquare != captureSquare && enemyPiece.canSlideAlongRay(ray)) { return true; }
      }
      return false;
   }

   /**
    * Is the king (colour 'colour') at 'kingsSquare' in check after the move 'm'? If yes, the checking square(s) will be
    * returned.
    * 
    * @param  posn        current position
    * @param  m           the move
    * @param  kingsSquare opponent's king
    * @param  colour      colour of king
    * @return             an empty list if not in check, otherwise a list containing the squares which check the king
    */
   /* package protected */ List<CheckInfo> isKingInCheckAfterMove(Position posn, Move m, int kingsSquare, Colour colour) {
      List<CheckInfo> checkSquares = new ArrayList<>(2);
      // (1) does the moving piece check the king directly?
      if (moveAttacksSquare(posn, m, kingsSquare, null)) { checkSquares.add(new CheckInfo(m.getMovingPiece(), m.getTarget())); }
      // (2) is there a piece on the ray origin..kingssquare which _now_ attacks the king (i.e. discovered check)?
      Ray rayToSearch = Ray.findRayBetween(kingsSquare, m.getOrigin());
      if (rayToSearch != null) {
         if (interveningSquaresAreEmpty(posn, kingsSquare, m.getOrigin(), rayToSearch)) {
            // squares from origin to king are empty, so see if there's an opponent's piece on this ray
            Pair<Piece, Integer> enemyPieceInfo = posn.opponentsPieceOnRay(colour, m.getOrigin(), rayToSearch);
            Piece enemyPiece = enemyPieceInfo.getLeft();
            if (enemyPiece != null) {
               int enemySq = enemyPieceInfo.getRight();
               // capable of checking the king?
               if (enemyPiece.canSlideAlongRay(rayToSearch)) { checkSquares.add(new CheckInfo(enemyPiece, enemySq)); }
            }
         }
      }
      return checkSquares;
   }

   /**
    * Returns true if the king (colour 'colour') would be in check after the given 'move'.
    * 
    * @param  posn
    * @param  move        the move to inspect
    * @param  kingsSquare the current king's square
    * @param  colour      colour of moving king
    * @return             an empty list if king is not in check; otherwise, the squares with pieces which give check
    * 
    */
   // TODO for non-king moves, should use cache built up from previous call of removeMovesLeavingKingInCheck
   private List<CheckInfo> kingIsInCheckAfterMove(Position posn, Move move, int kingsSquare, Colour colour) {
      int newKingsSq;
      int captureSquare = -1;
      if (move.getMovingPiece() == Piece.KING) {
         newKingsSq = move.getTarget();
      } else {
         newKingsSq = kingsSquare;
      }
      if (move.isCapture()) { captureSquare = move.isEnpassant() ? move.getSquareOfPawnCapturedEnpassant() : move.getTarget(); }

      return posn.isKingInCheck(newKingsSq, colour, captureSquare);
   }

   /**
    * Returns true if the given move would attack the given square. Can be used e.g. to see if a move will check the
    * opponent's king.
    * 
    * NB if a king move, always returns false.
    * 
    * @param  posn                     current position
    * @param  move                     the move to test
    * @param  targetSq                 square which might be attacked by the move
    * @param  squaresWhichAttackTarget stores the result of processed squares (for sliding pieces). Can be null.
    * @return                          true if the given move attacks the given square
    */
   private boolean moveAttacksSquare(Position posn, Move move, int targetSq, RayCacheInfo[] squaresWhichAttackTarget) {
      Piece movingPiece = move.getMovingPiece();
      if (movingPiece == Piece.KING) {
         return false;
      } else if (movingPiece == Piece.PAWN) {
         if (move.isPromotion()) {
            return pieceAttacksSquare(posn, move.getPromotedPiece(), move.getTarget(), targetSq, squaresWhichAttackTarget);
         } else {
            return pawnCaptures[move.getColourOfMovingPiece().ordinal()][move.getTarget()].contains(targetSq);
         }
      }
      return pieceAttacksSquare(posn, move.getMovingPiece(), move.getTarget(), targetSq, squaresWhichAttackTarget);
   }

   /**
    * whether a (possibly hypothetical) piece 'piece' at 'origin' attacks 'target'. Should not be called for kings or
    * pawns.
    */
   private boolean pieceAttacksSquare(Position posn, Piece piece, int origin, int target, RayCacheInfo[] squaresWhichAttackTarget) {
      if (piece == Piece.KNIGHT) { return knightMoves[origin].contains(target); }

      // see if result has already been calculated
      RayCacheInfo cacheInfo = squaresWhichAttackTarget != null ? squaresWhichAttackTarget[origin] : null;
      if (cacheInfo != null) {
         if (!cacheInfo.clearPathToTarget) { return false; }
         return piece.canSlideAlongRay(cacheInfo.rayBetween);
      }

      Ray rayBetween = Ray.findRayBetween(origin, target);
      if (rayBetween == null || !piece.canSlideAlongRay(rayBetween)) { return false; }
      // now know that the piece at 'origin' could attack the targetSq, if there is a clear path
      boolean clearPathToTarget = interveningSquaresAreEmpty(posn, origin, target, rayBetween);
      if (clearPathToTarget) {
         if (squaresWhichAttackTarget != null) { squaresWhichAttackTarget[origin] = new RayCacheInfo(rayBetween); } // update 'cache'
      } else {
         if (squaresWhichAttackTarget != null) { squaresWhichAttackTarget[origin] = new RayCacheInfo(); } // also store a negative result
      }
      return clearPathToTarget;
   }

   /**
    * Finds all moves for a piece at 'startSq'. King moves are stored separately.
    * 
    * @param posn
    * @param startSq   square to process
    * @param colour
    * @param moves     non-king moves will be added to this list
    * @param kingMoves king moves will be added to this list
    */
   private void processSquare(Position posn, int startSq, Colour colour, List<Move> moves, List<Move> kingMoves) {
      if (posn.colourOfPieceAt(startSq) == colour) {
         Piece pt = posn.pieceAt(startSq);
         if (pt == Piece.PAWN) {
            moves.addAll(generatePawnMoves(posn, startSq, colour));
         } else if (pt == Piece.KNIGHT) {
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
                     if (pt == Piece.KING) {
                        kingMoves.add(move);
                     } else {
                        moves.add(move);
                     }
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
    * @return          a move object or null if the targetSq contains a piece of our colour or the king would move adjacent
    *                  to enemy king
    */
   private Move potentiallyGenerateMoveOrCapture(Position posn, int startSq, int targetSq, Colour colour) {
      Move move = null;
      // king would move adjacent to opponent's king?
      if (posn.pieceAt(startSq) == Piece.KING
            && Square.toSquare(targetSq).adjacentTo(Square.toSquare(posn.getKingsSquare(posn.getSideToMove().opposite())))) {
         return null;
      }
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
    * Is our king in check after the given move? I.e. is there an enemy piece capable of giving check on the appropriate
    * ray.
    * 
    * @param  posn
    * @param  m                      the move to check
    * @param  ray                    the ray to check
    * @param  kingsSquare
    * @param  colour
    * @param  pinnedPieces           stores the squares of pinned pieces; use new int[] { -1, -1, -1, -1, -1, -1, -1, -1 }
    *                                if not applicable.
    * @param  squaresWhichAttackKing a cache of previously processed squares
    * @return                        true if the given move leaves the king in check
    */
   private boolean moveLeavesOurKingInCheckAlongRay(Position posn, Move m, Ray ray, int kingsSquare, Colour colour, int[] pinnedPieces,
         RayCacheInfo[] squaresWhichAttackKing) {
      // return immediately if this move involves a pinned piece
      if (m.getOrigin() == pinnedPieces[ray.ordinal()]) {
         // but a move along the ray is still ok
         if (moveOnSameRayOrOpposite(m, ray)) {
            if (verbose) { System.out.println(String.format("move %s with pinned piece is ok because along same ray as pin", m)); }
            return false;
         } else {
            if (verbose) { System.out.println(String.format("move %s illegal b/c piece at %s is pinned", m, Square.toSquare(pinnedPieces[ray.ordinal()]))); }
            return true;
         }
      }
      // no further processing if this move is 'further' along a ray where a pinned piece has already been found
      // (nb this method is called with moves sorted with squares closest to king first)
      if (pinnedPieces[ray.ordinal()] != -1) {
         if (verbose) {
            System.out.println(String.format("skipping processing move %s b/c piece at %s is pinned", m, Square.toSquare(pinnedPieces[ray.ordinal()])));
         }
         return false;
      }
      if (verbose) {
         System.out.println(String.format("processing move %s: sq %s is on ray %s from King (%s)", m, Square.toSquare(m.getOrigin()), ray.getAbbreviation(),
               Square.toSquare(kingsSquare)));
      }
      // If there's a piece between the moving piece and the king, then the king cannot be
      // left in check, so don't process this move anymore
      // TODO could optimize here and store the result of this analysis
      if (!interveningSquaresAreEmpty(posn, kingsSquare, m.getOrigin(), ray)) { return false; }
      // .. otherwise see if there's an enemy piece on this ray
      Pair<Piece, Integer> enemyPieceInfo = posn.opponentsPieceOnRay(colour, m.getOrigin(), ray);
      Piece enemyPiece = enemyPieceInfo.getLeft();
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
    * Processes all moves <B>on the given ray</B> (must be sorted with origin squares closest to kingsSquare occurring
    * first).
    * 
    * @param posn
    * @param kingsSquare
    * @param colour
    * @param movesOnRay  the moves to inspect, stored for each ray as seen from the king's square, sorted with origin
    *                    squares closest to king first
    */
   private void removeMovesLeavingKingInCheckAlongRay(Position posn, int kingsSquare, Colour colour, List<Move>[] movesOnRay) {
      int[] pinnedPieces = new int[] { -1, -1, -1, -1, -1, -1, -1, -1 }; // stores squares of pinned pieces for each ray
      RayCacheInfo[] squaresWhichAttackKing = new RayCacheInfo[64]; // this stores the result of processed squares (for sliding pieces)
      for (Ray ray : Ray.values()) {
         Iterator<Move> moveIter = movesOnRay[ray.ordinal()].iterator();
         while (moveIter.hasNext()) {
            Move m = moveIter.next();
            if (moveLeavesOurKingInCheckAlongRay(posn, m, ray, kingsSquare, colour, pinnedPieces, squaresWhichAttackKing)) { moveIter.remove(); }
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

   // if the king is in check is not verified by this method
   private boolean canCastleKingsside(Position posn, Colour colour) {
      if (!posn.canCastleKingsside(colour)) { return false; }
      int colourOrd = colour.ordinal();
      if (posn.pieceAt(rooksCastlingSquareIndex[colourOrd][0]) != Piece.ROOK) { return false; }
      for (int sq : unoccupiedSquaresKingssideCastling[colourOrd]) {
         if (posn.colourOfPieceAt(sq) != Colour.UNOCCUPIED) { return false; }
      }

      // cannot castle over a square in check
      for (int sq : pawnSquaresKingssideCastling[colourOrd]) {
         if ((posn.pieceAt(sq) == Piece.PAWN) && (posn.colourOfPieceAt(sq) == colour.opposite())) { return false; }
      }
      for (int sq : knightSquaresKingssideCastling[colourOrd]) {
         if ((posn.pieceAt(sq) == Piece.KNIGHT) && (posn.colourOfPieceAt(sq) == colour.opposite())) { return false; }
      }
      for (int sq : unoccupiedSquaresKingssideCastling[colourOrd]) {
         Ray[] raysToCheck = colour == Colour.WHITE ? new Ray[] { Ray.NORTHWEST, Ray.NORTH, Ray.NORTHEAST }
               : new Ray[] { Ray.SOUTHWEST, Ray.SOUTH, Ray.SOUTHEAST };
         for (Ray ray : raysToCheck) {
            Pair<Piece, Integer> enemyPieceInfo = posn.opponentsPieceOnRay(colour, sq, ray);
            Piece enemyPiece = enemyPieceInfo.getLeft();
            // found piece capable of checking the king?
            if (enemyPiece != null && enemyPiece.canSlideAlongRay(ray)) { return false; }
         }
      }
      return true;
   }

   // if the king is in check is not verified by this method
   private boolean canCastleQueensside(Position posn, Colour colour) {
      if (!posn.canCastleQueensside(colour)) { return false; }
      int colourOrd = colour.ordinal();
      if (posn.pieceAt(rooksCastlingSquareIndex[colourOrd][1]) != Piece.ROOK) { return false; }
      for (int sq : unoccupiedSquaresQueenssideCastling[colourOrd]) {
         if (posn.colourOfPieceAt(sq) != Colour.UNOCCUPIED) { return false; }
      }
      // cannot castle over a square in check
      for (int sq : pawnSquaresQueenssideCastling[colourOrd]) {
         if ((posn.pieceAt(sq) == Piece.PAWN) && (posn.colourOfPieceAt(sq) == colour.opposite())) { return false; }
      }
      for (int sq : knightSquaresQueenssideCastling[colourOrd]) {
         if ((posn.pieceAt(sq) == Piece.KNIGHT) && (posn.colourOfPieceAt(sq) == colour.opposite())) { return false; }
      }
      for (int sq : unoccupiedSquaresQueenssideCastling[colourOrd]) {
         // we misuse this array for the 'square in check' calculation: the b1/b8 squares don't need to be inspected
         if (sq == Square.b1.index() || sq == Square.b8.index()) { continue; }
         Ray[] raysToCheck = colour == Colour.WHITE ? new Ray[] { Ray.NORTHWEST, Ray.NORTH, Ray.NORTHEAST }
               : new Ray[] { Ray.SOUTHWEST, Ray.SOUTH, Ray.SOUTHEAST };
         for (Ray ray : raysToCheck) {
            Pair<Piece, Integer> enemyPieceInfo = posn.opponentsPieceOnRay(colour, sq, ray);
            Piece enemyPiece = enemyPieceInfo.getLeft();
            // found piece capable of checking the king?
            if (enemyPiece != null && enemyPiece.canSlideAlongRay(ray)) { return false; }
         }
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

   private List<Move> generatePawnMoves(Position posn, int startSq, Colour colour) {
      List<Move> moves = new ArrayList<>();
      // following cases ('reverse' for black pawns):
      // - pawn on 2nd rank can move 1 or two squares forward
      // - capturing diagonally
      // - pawn on 7th rank can promote
      // - en passant

      int forwardOffset = colour == Colour.WHITE ? -10 : 10;
      // promotion
      if (onPawnPromotionRank(startSq, colour)) {
         int nextSq = getMailboxSquare(startSq, forwardOffset);
         moves.addAll(generatePossibleNormalPromotionMoves(posn, startSq, nextSq, colour, posn.colourOfPieceAt(nextSq)));
         for (int targetSq : pawnCaptures[colour.ordinal()][startSq]) {
            moves.addAll(generatePossibleCapturePromotionMoves(posn, startSq, targetSq, colour, posn.colourOfPieceAt(targetSq)));
         }
      } else {
         // 1 square forward, optionally followed by 2 squares forward
         int nextSq = getMailboxSquare(startSq, forwardOffset);
         // NB this square cannot be outside of the board, we've already checked for the 'promotion' rank
         if (posn.colourOfPieceAt(nextSq) == Colour.UNOCCUPIED) {
            moves.add(generateMove(posn, startSq, nextSq));
            if (onPawnStartRank(startSq, colour)) {
               nextSq = getMailboxSquare(nextSq, forwardOffset); // cannot be outside of the board
               if (posn.colourOfPieceAt(nextSq) == Colour.UNOCCUPIED) { moves.add(Move.createPawnTwoSquaresForwardMove(startSq, posn.raw(startSq), nextSq)); }
            }
         }
         // captures
         for (int targetSq : pawnCaptures[colour.ordinal()][startSq]) {
            addPawnCaptureMoveIfPossible(posn, startSq, targetSq, colour, moves);
         }
      }
      if (posn.getEnpassantSquare() != null) {
         int epSquare = posn.getEnpassantSquare().index();
         for (int sq : enpassantSquares[colour.ordinal()].get(epSquare)) {
            if (startSq == sq) { moves.add(Move.createEnpassantMove(startSq, posn.raw(startSq), epSquare, posn.raw(epSquare))); }
         }
      }
      return moves;

   }

   // caters for pawn advancing one square and promoting
   private List<Move> generatePossibleNormalPromotionMoves(Position posn, int origin, int target, Colour myColour, Colour colourOfTargetSq) {
      List<Move> moves = new ArrayList<>();
      if (colourOfTargetSq == Colour.UNOCCUPIED) {
         for (Piece pt : new Piece[] { Piece.ROOK, Piece.KNIGHT, Piece.BISHOP, Piece.QUEEN }) {
            moves.add(Move.createPromotionMove(origin, posn.raw(origin), target, pt));
         }
      }
      return moves;
   }

   // caters for pawn capturing a piece and promoting
   private List<Move> generatePossibleCapturePromotionMoves(Position posn, int origin, int target, Colour myColour, Colour colourOfTargetSq) {
      List<Move> moves = new ArrayList<>();
      if (myColour.opposes(colourOfTargetSq)) {
         for (Piece pt : new Piece[] { Piece.ROOK, Piece.KNIGHT, Piece.BISHOP, Piece.QUEEN }) {
            moves.add(Move.createPromotionCaptureMove(origin, posn.raw(origin), target, posn.raw(target), pt));
         }
      }
      return moves;
   }

   private void addPawnCaptureMoveIfPossible(Position posn, int origin, int target, Colour colour, List<Move> moves) {
      Colour colourOfTargetSq = posn.colourOfPieceAt(target);
      if (colour.opposes(colourOfTargetSq)) { moves.add(generateCapture(posn, origin, target)); }
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
      return Move.createMove(from, posn.raw(from), to);
   }

   private Move generateCapture(Position posn, int from, int to) {
      return Move.createCapture(from, posn.raw(from), to, posn.raw(to));
   }

   /**
    * Stores info about a square in relation to a particular target square (often, the opposing king's square).
    * 
    * @param rayBetween        stores the ray to the target square or null. Only set if 'clearPathToTarget'==TRUE
    * @param clearPathToTarget whether there's a clear path to the target
    */
   private static record RayCacheInfo(Ray rayBetween, boolean clearPathToTarget) {

      // constructor if there is a clearPathToTarget
      public RayCacheInfo(Ray rayBetween) {
         this(rayBetween, true);
      }

      // default constructor: no clearPathToTarget
      public RayCacheInfo() {
         this(null, false);
      }

   }
}
