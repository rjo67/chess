package org.rjo.newchess.move;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;

import org.rjo.newchess.board.Board;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.board.Ray;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.game.Position.PieceSquareInfo;
import org.rjo.newchess.move.MoveGenerator.RayCacheInfo.RayCacheState;
import org.rjo.newchess.move.MoveGenerator.SquareInfo.State;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Piece;
import org.rjo.newchess.piece.Pieces;

/**
 * @author rich
 *
 */
public class MoveGenerator implements MoveGeneratorI {

   private boolean verbose;

   // **** Note
   // the first dimension of all these arrays is indexed on colour (w/b)
   // ****

   // square where the king must be to be able to castle
   public final static int[] kingsCastlingSquareIndex = new int[] { Square.e1.index(), Square.e8.index() };
   // square where the king ends up after castling kings or queensside, indexed on
   // colour
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
   // if an enemy knight is on these squares, then cannot castle queensside (not
   // including c2/c7, since a knight on that square checks the king)
   private final static int[][] knightSquaresQueenssideCastling = new int[][]//
   { { Square.b3.index(), Square.c3.index(), Square.d3.index(), Square.e3.index(), Square.a2.index(), Square.b2.index(), Square.e2.index(), Square.f2.index() },
         { Square.b6.index(), Square.c6.index(), Square.d6.index(), Square.e6.index(), Square.a7.index(), Square.b7.index(), Square.e7.index(),
               Square.f7.index() } };
   // if an enemy pawn is on these squares, then cannot castle queensside
   private final static int[][] pawnSquaresQueenssideCastling = new int[][]//
   { { Square.b2.index(), Square.c2.index(), Square.d2.index(), Square.e2.index() },
         { Square.b7.index(), Square.c7.index(), Square.d7.index(), Square.e7.index() } };
   // Stores (for both colours) the squares (dim1) where a pawn must be in order to
   // take a pawn on dim0 with e.p.
   private final static int[][] enpassantSquares = new int[64][];
   public final static Set<Integer>[] knightMoves; // stores set of possible knight moves for each square
   public final static int[][][] pawnCaptures = new int[2][64][]; // dim0: w/b; dim1: squares; dim2: possible pawn
   // captures (max 2)
   static {
      enpassantSquares[Square.a6.index()] = new int[] { Square.b5.index() };
      enpassantSquares[Square.b6.index()] = new int[] { Square.a5.index(), Square.c5.index() };
      enpassantSquares[Square.c6.index()] = new int[] { Square.b5.index(), Square.d5.index() };
      enpassantSquares[Square.d6.index()] = new int[] { Square.c5.index(), Square.e5.index() };
      enpassantSquares[Square.e6.index()] = new int[] { Square.d5.index(), Square.f5.index() };
      enpassantSquares[Square.f6.index()] = new int[] { Square.e5.index(), Square.g5.index() };
      enpassantSquares[Square.g6.index()] = new int[] { Square.f5.index(), Square.h5.index() };
      enpassantSquares[Square.h6.index()] = new int[] { Square.g5.index() };

      enpassantSquares[Square.a3.index()] = new int[] { Square.b4.index() };
      enpassantSquares[Square.b3.index()] = new int[] { Square.a4.index(), Square.c4.index() };
      enpassantSquares[Square.c3.index()] = new int[] { Square.b4.index(), Square.d4.index() };
      enpassantSquares[Square.d3.index()] = new int[] { Square.c4.index(), Square.e4.index() };
      enpassantSquares[Square.e3.index()] = new int[] { Square.d4.index(), Square.f4.index() };
      enpassantSquares[Square.f3.index()] = new int[] { Square.e4.index(), Square.g4.index() };
      enpassantSquares[Square.g3.index()] = new int[] { Square.f4.index(), Square.h4.index() };
      enpassantSquares[Square.h3.index()] = new int[] { Square.g4.index() };

      knightMoves = new Set[64];
      for (int sq = 0; sq < 64; sq++) {
         knightMoves[sq] = new HashSet<>();
         for (int offset : Piece.KNIGHT.getMoveOffsets()) {
            int targetSq = Board.getMailboxSquare(sq, offset);
            if (targetSq != -1) { knightMoves[sq].add(targetSq); }
         }
      }

      int[][] captureOffset = new int[][] { { -9, -11 }, { 9, 11 } };
      for (Colour col : new Colour[] { Colour.WHITE, Colour.BLACK }) {
         // process first and last rank as well, need these squares defined for
         // kingIsInCheckAfterMove()
         for (int sq = 0; sq < 64; sq++) {
            var tmpPawnCaptures = new ArrayList<Integer>();
            for (int offset : captureOffset[col.ordinal()]) {
               int targetSq = Board.getMailboxSquare(sq, offset);
               if (targetSq != -1) { tmpPawnCaptures.add(targetSq); }
            }
            pawnCaptures[col.ordinal()][sq] = new int[tmpPawnCaptures.size()];
            var slot = 0;
            for (int i : tmpPawnCaptures) {
               pawnCaptures[col.ordinal()][sq][slot] = i;
               slot++;
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

   @Override
   public List<Move> findMoves(Position posn, Colour colour) {
      /*
       * Instead of looking at all the squares from 0..63, starts at the kingsSquare and proceeds in ray order first. Then all other squares are
       * processed. This is done to reduce / simplify the amount of work needed later to see if a move left our king in check. The
       * 'squaresProcessed' array is used to keep track of which squares have been processed during the 'rays' loop.
       */
      boolean[] squaresProcessed = new boolean[64]; // false - not processed
      // stores moves in the ray directions. Moves are stored in the direction of the
      // ray, starting next to the kings square
      @SuppressWarnings("unchecked")
      List<Move>[] movesWithStartSqOnRay = new List[Ray.values().length];
      List<Move> otherMoves = new ArrayList<>(30); // stores moves from all other squares
      List<Move> kingMoves = new LinkedList<>(); // stores king moves

      var kingInCheck = posn.isKingInCheck();
      var kingInDoubleCheck = kingInCheck && posn.getCheckSquares().size() == 2;

      int kingsSquare = posn.getKingsSquare(colour);
      // does not include checking to see if the king is still in check after this move (happens later)
      processKingsSquare(posn, kingsSquare, colour, kingMoves);
      squaresProcessed[kingsSquare] = true;

      if (!kingInDoubleCheck) {
         // if our king is in check, this cache stores info about squares which block the
         // check
         int[] blocksCheck = new int[64]; // 0: not processed, 1: square blocks check; 2: not on ray to king or does
         // not block

         PieceSquareInfo checkInfo = null;
         if (kingInCheck) {
            checkInfo = posn.getCheckSquares().get(0);
            Ray ray = Ray.findRayBetween(kingsSquare, checkInfo.square());
            if (ray != null) {
               checkInfo.setRayToKing(ray);
               /*
                * Because moves are generated along the ray, starting next to the king, we can keep track of the first piece that we find. Any further
                * pieces along this ray cannot be pinned.
                */
               prefillCache(blocksCheck, checkInfo, kingsSquare); // squares between kingsSquare and checking piece
               // can be marked as "1"
            }
         }

         // generate all moves: only moves which get us out of check (if necessary) will
         // be recorded

         for (Ray ray : Ray.values()) {
            movesWithStartSqOnRay[ray.ordinal()] = new LinkedList<>();
            for (int raySq : Ray.raysList[kingsSquare][ray.ordinal()]) {
               processSquare(posn, raySq, colour, movesWithStartSqOnRay[ray.ordinal()], checkInfo, kingsSquare, blocksCheck);
               squaresProcessed[raySq] = true;
            }
         }
         // remove moves along rays to king if the piece is pinned
         removeMovesLeavingKingInCheckAlongRay(posn, posn.getKingsSquare(colour), colour, movesWithStartSqOnRay);

         // process all other squares
         for (int sq = 0; sq < 64; sq++) {
            if (!squaresProcessed[sq]) { processSquare(posn, sq, colour, otherMoves, checkInfo, kingsSquare, blocksCheck); }
         }

         if (!kingInCheck) {
            if (canCastleKingsside(posn, colour)) { kingMoves.add(Move.createKingssideCastlingMove(colour)); }
            if (canCastleQueensside(posn, colour)) { kingMoves.add(Move.createQueenssideCastlingMove(colour)); }
         }
      }

      // process king moves to make sure the king hasn't moved into check
      Iterator<Move> kingMoveIter = kingMoves.iterator();
      while (kingMoveIter.hasNext()) {
         Move kingsMove = kingMoveIter.next();
         if (kingIsInCheckAfterKingsMove(posn, kingsMove, colour)) { kingMoveIter.remove(); }
      }

      // *** have now found valid moves
      // now process to see if our moves are checking the enemy king
      // ***

      // collect all the moves
      List<Move> allMoves = new ArrayList<>(64);
      if (!kingInDoubleCheck) {
         for (Ray ray : Ray.values()) {
            allMoves.addAll(movesWithStartSqOnRay[ray.ordinal()]);
         }
         allMoves.addAll(otherMoves);
      }
      // could process king moves separately in the following code, but not really
      // necessary,
      // since isKingInCheckAfterMove(...) copes with king moves as well
      allMoves.addAll(kingMoves);

      // ***
      // now process checks against _opposing_ king
      // ***
      RayCacheInfo[] squaresAttackingOpponentsKing = new RayCacheInfo[64]; // this stores the result of processed
      // squares (for sliding pieces)
      int opponentsKingsSquare = posn.getKingsSquare(colour.opposite());
      for (Move m : allMoves) {
         List<PieceSquareInfo> checkSquares = isOpponentsKingInCheckAfterMove(posn, m, opponentsKingsSquare, colour.opposite(), squaresAttackingOpponentsKing);
         if (!checkSquares.isEmpty()) { m.setCheck(checkSquares); }
      }

      return allMoves;
   }

   private void prefillCache(int[] blocksCheck, PieceSquareInfo checkInfo, int kingsSquare) {
      for (int sq : Ray.raysList[kingsSquare][checkInfo.rayToKing().ordinal()]) {
         if (sq == checkInfo.square()) { break; }
         blocksCheck[sq] = 1;
      }
   }

   /**
    * If the king is already in check, this enum details the possibilities after a certain move.
    */
   enum BlockedCheckPossibility {
      NOT_BLOCKED, // move has not blocked the check
      CHECKING_PIECE_CAPTURED, // move has captured the checking piece
      RAY_BLOCKED; // move has blocked the ray along which the check was being made
   }

   /**
    * Only called when the king is already in check. The given move must therefore either capture the checking piece or block the check.
    * 
    * Do not call for king moves.
    * 
    * @param posn        position. **King must be in check**
    * @param move
    * @param kingsSquare
    * @param blocksCheck a cache of already processed squares, 1 if a sq blocks the check, 0 if not yet calculated
    * @return true if the move successfully blocks the check (or captures the checking piece)
    */
   private BlockedCheckPossibility moveBlocksCheck(Position posn, Move move, int kingsSquare, int[] blocksCheck) {
      List<PieceSquareInfo> checkSquares = posn.getCheckSquares();
      if (checkSquares.isEmpty()) { throw new IllegalStateException("no check squares specified, move " + move + ", posn: " + posn); }
      if (checkSquares.size() == 2) { return BlockedCheckPossibility.NOT_BLOCKED; } // a double check cannot be blocked
      PieceSquareInfo checkInfo = checkSquares.get(0);
      // (1) does the move capture the piece?
      if (move.moveCapturesPiece(checkInfo.square())) { return BlockedCheckPossibility.CHECKING_PIECE_CAPTURED; }
      // (2) does the move block the check?
      return moveToSquareBlocksCheck(checkInfo, move.getTarget(), kingsSquare, blocksCheck) ? BlockedCheckPossibility.RAY_BLOCKED
            : BlockedCheckPossibility.NOT_BLOCKED;
   }

   /**
    * Would a move to 'targetSquareOfMove' block the given check, i.e. is it on the same ray and between the checker and the king?
    * 
    * @param checkInfo          info about the checking piece
    * @param targetSquareOfMove where our piece is moving to
    * @param kingsSquare
    * @param blocksCheck        a cache of already processed squares, 2 if not on ray/does not block, 1 if a sq blocks the check, 0 if not yet
    *                           calculated
    * @return true if blocks the checker
    */
   private boolean moveToSquareBlocksCheck(PieceSquareInfo checkInfo, int targetSquareOfMove, int kingsSquare, int[] blocksCheck) {
      if (Pieces.isPawnOrKnight(checkInfo.piece())) { return false; } // cannot block these checks
      if (blocksCheck[targetSquareOfMove] != 0) {
         if (verbose) {
            System.out.println("moveToSquareBlocksCheck, used cache for targetSq: " + targetSquareOfMove + ", result: " + blocksCheck[targetSquareOfMove]);
         }
         return blocksCheck[targetSquareOfMove] == 1;
      }
      if (verbose) { System.out.println("moveToSquareBlocksCheck, checkInfo: " + checkInfo + ", targetSq: " + targetSquareOfMove); }
      Ray moveRay = Ray.findRayBetween(kingsSquare, targetSquareOfMove);
      if (moveRay == null) { // can't block a check if there's no ray between the moved piece and the king
         blocksCheck[targetSquareOfMove] = 2;
         return false;
      }
      if (moveRay != checkInfo.rayToKing()) { // not on same ray
         blocksCheck[targetSquareOfMove] = 2;
         return false;
      }
      // should never get here if the blocksCheck cache has been setup correctly
      boolean isBlocked = moveRay.squareBetween(targetSquareOfMove, kingsSquare, checkInfo.square());
      blocksCheck[targetSquareOfMove] = isBlocked ? 1 : 2;
      return isBlocked;
   }

   /**
    * Processes king's moves. Returns true if the king is (still) in check after moving.
    * 
    * @param posn      current posn
    * @param kingsMove the king's move
    * @param colour    king's colour
    * @return true if in check after move
    */
   /* package protected */ boolean kingIsInCheckAfterKingsMove(Position posn, Move kingsMove, Colour colour) {
      int captureSquare = kingsMove.isCapture() ? kingsMove.getTarget() : -1;
      Colour opponentsColour = colour.opposite();
      if (posn.isKingInCheck()) {
         // if already in check, cannot move along the same ray as the checker (unless it's a capture)
         List<PieceSquareInfo> checkSquares = posn.getCheckSquares();
         for (PieceSquareInfo checkInfo : checkSquares) {
            // ignore if a pawn (can move away from pawn on the same ray w/o any problem)
            if (Pieces.isPawn(checkInfo.piece())) { continue; }
            Ray rayBeforeMove = checkInfo.rayToKing() != null ? checkInfo.rayToKing() : Ray.findRayBetween(kingsMove.getOrigin(), checkInfo.square());
            // ignore if not on ray (==> knight check)
            if (rayBeforeMove != null) {
               if (checkInfo.rayToKing() == null) { checkInfo.setRayToKing(rayBeforeMove); }
               Ray rayAfterMove = Ray.findRayBetween(kingsMove.getTarget(), checkInfo.square());
               if (rayBeforeMove == rayAfterMove && captureSquare != checkInfo.square()) { return true; }
               if (rayAfterMove != null && rayBeforeMove == rayAfterMove.getOpposite()) { return true; }
            }
         }
      }

      // having reached this point, the king has successfully moved out of an existing check
      // now check if it's moved into a check

      for (int sq : pawnCaptures[colour.ordinal()][kingsMove.getTarget()]) {
         if (sq != captureSquare && posn.matchesPieceTypeAndColour(sq, Pieces.generatePiece(Piece.PAWN, opponentsColour))) { return true; }
      }

      for (int sq : knightMoves[kingsMove.getTarget()]) {
         if (sq != captureSquare && posn.matchesPieceTypeAndColour(sq, Pieces.generatePiece(Piece.KNIGHT, opponentsColour))) { return true; }
      }

      // now need to process all rays from the new king's square.
      // TODO *** Careful, the position still stores the _old_ king's position.

      Ray ignoreRay1 = null, ignoreRay2 = null;
      // if the king was _not in check_ before, we don't need to check the direction
      // of travel (or the opposite direction)
      // e.g. if moving Kg1-f1 (west), then don't need to check west or east ray
      // For a capture: Kg1xf1 (west), we _must_ still check the west ray; but can
      // safely ignore the east ray
      if (!posn.isKingInCheck()) {
         Ray moveDirection = Ray.findRayBetween(kingsMove.getOrigin(), kingsMove.getTarget());
         if (!kingsMove.isCapture()) { ignoreRay1 = moveDirection; }
         ignoreRay2 = moveDirection.getOpposite();
      }

      for (Ray ray : Ray.values()) {
         if (ray == ignoreRay1 || ray == ignoreRay2) { continue; }
         PieceSquareInfo enemyPieceInfo = posn.opponentsPieceOnRay(colour, kingsMove.getTarget(), ray);
         if (enemyPieceInfo.piece() != 0 && enemyPieceInfo.square() != captureSquare && Pieces.canSlideAlongRay(enemyPieceInfo.piece(), ray)) { return true; }
      }
      return false;
   }

   /**
    * Is the opponent's king (colour 'colour') at 'kingsSquare' in check after my move 'm'? If yes, the checking square(s) will be returned.
    * 
    * @param posn                 current position
    * @param m                    the move
    * @param kingsSquare          opponent's king
    * @param colour               colour of king
    * @param squaresAttackingKing information about squares which attack the opponent's king
    * @return a list containing the squares which check the king (an empty list if not in check)
    */
   /* package protected */ List<PieceSquareInfo> isOpponentsKingInCheckAfterMove(Position posn, Move m, int kingsSquare, Colour colour,
         RayCacheInfo[] squaresAttackingKing) {
      List<PieceSquareInfo> checkSquares = new ArrayList<>(2);
      boolean updateCache = squaresAttackingKing != null && !m.isCapture() && !m.isPromotion();

      // (1) does the moving piece check the king directly?
      addIfNotNull(checkSquares, moveAttacksSquare(posn, m, kingsSquare, squaresAttackingKing));

      // build cache of Rays from origin..king (and target..king) to save calling
      // findRayBetween

      // (2) is there a piece on the ray origin..kingssquare which _now_ attacks the
      // king (i.e. discovered check)?
      // TODO here check the 'squaresAttackingKing' cache!
      Ray rayFromOriginToKing = Ray.findRayBetween(m.getOrigin(), kingsSquare);
      if (rayFromOriginToKing != null) {
         Ray rayFromTargetToKing = Ray.findRayBetween(m.getTarget(), kingsSquare); // could be null
         // don't search if the move is to a square on the same king's ray (pawn move, or
         // r/b/q where no path to enemy king)
         if (rayFromOriginToKing != rayFromTargetToKing) {
            int interveningSquare = interveningSquaresAreEmpty(posn, m.getOrigin(), kingsSquare, -1, rayFromOriginToKing);
            if (interveningSquare == -1) {
               if (updateCache) { updateCacheClearPath(squaresAttackingKing, m.getOrigin(), kingsSquare, rayFromOriginToKing); }
               // squares from origin to king are empty, so see if there's an opponent's piece
               // on the opposite ray starting at origin
               PieceSquareInfo enemyPieceInfo = posn.opponentsPieceOnRay(colour, m.getOrigin(), rayFromOriginToKing.getOpposite());
               // enemy piece found, and capable of checking the king?
               if (enemyPieceInfo.piece() != 0 && Pieces.canSlideAlongRay(enemyPieceInfo.piece(), rayFromOriginToKing)) { checkSquares.add(enemyPieceInfo); }
            } else { // ray to king, but intervening squares not empty
               if (updateCache) { updateCacheNoClearPath(squaresAttackingKing, m.getOrigin(), interveningSquare, rayFromOriginToKing); }
            }
         }
      } else { // no ray to king
         if (updateCache) { squaresAttackingKing[m.getOrigin()] = RayCacheInfo.noRayToKing(); }
      }
      return checkSquares;
   }

   // update cache "clearPath" from origin to target. "origin" and "target"
   // themselves are not changed
   private void updateCacheClearPath(RayCacheInfo[] squaresAttackingKing, int origin, int target, Ray ray) {
      for (int sq : Ray.raysList[origin][ray.ordinal()]) {
         if (sq == target) { break; }
         squaresAttackingKing[sq] = RayCacheInfo.clearPath(ray);
         if (verbose) { System.out.println("cache: clear path, square " + Square.toSquare(sq)); }
      }
   }

   // update cache "noClearPath" from origin to target. "origin" and "target"
   // themselves are not changed
   private void updateCacheNoClearPath(RayCacheInfo[] squaresAttackingKing, int origin, int target, Ray ray) {
      for (int sq : Ray.raysList[origin][ray.ordinal()]) {
         if (sq == target) { break; }
         squaresAttackingKing[sq] = RayCacheInfo.noClearPath(ray);
         if (verbose) { System.out.println("cache: no clear path, square " + Square.toSquare(sq)); }
      }
   }

   /**
    * Returns true if the given move would attack the given square. Can be used e.g. to see if a move will check the opponent's king.
    * 
    * NB if a king move, always returns null.
    * 
    * @param posn                     current position
    * @param move                     the move to test
    * @param targetSq                 square which might be attacked by the move
    * @param squaresWhichAttackTarget stores the result of processed squares (for sliding pieces). Can be null.
    * @return either null or an object detailing the piece and checking square
    */
   private PieceSquareInfo moveAttacksSquare(Position posn, Move move, int targetSq, RayCacheInfo[] squaresWhichAttackTarget) {
      byte movingPiece = move.getMovingPiece();
      if (Pieces.isKing(movingPiece)) {
         int slot = move.isKingssideCastling() ? 0 : move.isQueenssideCastling() ? 1 : -1;
         if (slot != -1) {
            return pieceAttacksSquare(posn, Piece.ROOK, rooksSquareAfterCastling[move.getColourOfMovingPiece().ordinal()][slot], targetSq, -1, -1,
                  squaresWhichAttackTarget);
         } else {
            return null;
         }
      } else if (Pieces.isPawn(movingPiece)) {
         if (move.isPromotion()) {
            // TODO for now, don't use cache
            return pieceAttacksSquare(posn, Pieces.toPiece(move.getPromotedPiece()), move.getTarget(), targetSq, move.getOrigin(), -1, null);
         } else {
            for (int sq : pawnCaptures[move.getColourOfMovingPiece().ordinal()][move.getTarget()]) {
               if (sq == targetSq) { return new PieceSquareInfo(Pieces.generatePawn(move.getColourOfMovingPiece()), move.getTarget()); }
            }
            return null;
         }
      } else {
         return pieceAttacksSquare(posn, Pieces.toPiece(move.getMovingPiece()), move.getTarget(), targetSq, move.getOrigin(),
               move.isCapture() ? move.getTarget() : -1, squaresWhichAttackTarget);
      }
   }

   /**
    * Whether a (possibly hypothetical) piece 'piece' at 'origin' attacks 'target'. Should not be called for kings or pawns.
    * 
    * @param posn
    * @param piece
    * @param origin
    * @param target
    * @param squareToIgnore             if set,this square will be treated as 'empty'. Useful for pawn promotions.
    * @param cacheCaptureSquareToIgnore if set, the cached value for this square will not be used. (useful for captures). Also switches off
    *                                   updating the cache.
    * @param squaresWhichAttackTarget   stores the result of processed squares (for sliding pieces). <B>Will be ignored if cacheSquareToIgnore
    *                                   == -1</B>.
    * @return either null (does not attack square) or the piece / checking square info
    */
   private PieceSquareInfo pieceAttacksSquare(Position posn, Piece piece, int origin, int target, int squareToIgnore, int cacheCaptureSquareToIgnore,
         RayCacheInfo[] squaresWhichAttackTarget) {
      if (piece == Piece.KNIGHT) { return knightMoves[origin].contains(target) ? new PieceSquareInfo(Pieces.generateKnight(null), origin) : null; } // TODO
                                                                                                                                                    // colour
      // only use cache if this isn't a capture
      var canUseCache = squaresWhichAttackTarget != null && cacheCaptureSquareToIgnore == -1;

      // see if result has already been calculated (ignore if cacheSquareToIgnore is set)
      if (canUseCache) {
         RayCacheInfo cacheInfo = squaresWhichAttackTarget[origin];
         if (cacheInfo != null) {
            if (cacheInfo.state != RayCacheState.CLEAR_PATH_TO_KING) { return null; }
            return piece.canSlideAlongRay(cacheInfo.rayBetween) ? new PieceSquareInfo(Pieces.generatePiece(piece, null), origin) : null;
         }
      }

      Ray rayBetween = Ray.findRayBetween(origin, target);
      if (rayBetween == null) {
         if (squaresWhichAttackTarget != null) { squaresWhichAttackTarget[origin] = RayCacheInfo.noRayToKing(); }
         return null;
      }
      if (!piece.canSlideAlongRay(rayBetween)) { return null; }
      // now know that the piece at 'origin' could attack the targetSq, if there is a clear path
      int interveningSquare = interveningSquaresAreEmpty(posn, origin, target, squareToIgnore, rayBetween);
      if (interveningSquare == -1) {
         if (canUseCache) {
            if (verbose) { System.out.println("cache clear path, square " + Square.toSquare(origin)); }
            squaresWhichAttackTarget[origin] = RayCacheInfo.clearPath(rayBetween);
            updateCacheClearPath(squaresWhichAttackTarget, origin, target, rayBetween);
         }
         return new PieceSquareInfo(Pieces.generatePiece(piece, null), origin);
      } else {
         // also store a negative result
         if (canUseCache) {
            // can only cache squares up to intervening piece
            if (verbose) { System.out.println("cache noClearPath, square " + Square.toSquare(origin)); }
            squaresWhichAttackTarget[origin] = RayCacheInfo.noClearPath(rayBetween);
            updateCacheNoClearPath(squaresWhichAttackTarget, origin, interveningSquare, rayBetween);
         }
         return null;
      }
   }

   /**
    * Finds all pseudo-legal moves (i.e. not checked for pins) for a KING at 'startSq'.
    *
    * Only for King moves.
    * 
    * @param posn
    * @param startSq square to process
    * @param colour
    * @param moves   king moves will be added to this list
    */
   /* package */ void processKingsSquare(Position posn, int startSq, Colour colour, List<Move> moves) {
      if (posn.colourOfPieceAt(startSq) == colour) {
         if (!Pieces.isKing(posn.pieceAt(startSq))) {
            throw new IllegalStateException(String.format("called processKingsSquare with piece %s, posn: %s", posn.pieceAt(startSq), posn));
         }
         Square opponentsKing = Square.toSquare(posn.getKingsSquare(posn.getSideToMove().opposite()));
         for (int offset : Piece.KING.getMoveOffsets()) { // process each square along the ray
            int nextSq = Board.getMailboxSquare(startSq, offset);
            if (nextSq != -1) {
               if (!Square.toSquare(nextSq).adjacentTo(opponentsKing)) { // king would move adjacent to opponent's king?
                  addIfNotNull(moves, generateEitherMoveOrCapture(posn, startSq, nextSq, colour));
               }
            }
         }
      }
   }

   /**
    * Finds all pseudo-legal moves (i.e. not checked for pins) for a piece at 'startSq'.
    * 
    * If our king is currently in check, the move has to block the check or capture the checking piece.
    * 
    * King squares are not processed here, see processKingsSquare().
    * 
    * @param posn
    * @param startSq     square to process
    * @param colour
    * @param moves       moves will be added to this list
    * @param checkInfo   non-null if our king is currently in check
    * @param kingsSquare position of our king; relevant if checkInfo!=null
    * @param blocksCheck a cache of already processed squares, 2 if sq not on ray / does not block check, 1 if a sq blocks the check, 0 if not
    *                    yet calculated
    */
   private void processSquare(Position posn, int startSq, Colour colour, List<Move> moves, PieceSquareInfo checkInfo, int kingsSquare, int[] blocksCheck) {
      if (!posn.squareIsEmpty(startSq) && posn.colourOfPieceAt(startSq) == colour) {
         byte pt = posn.pieceAt(startSq);
         if (Pieces.isPawn(pt)) {
            moves.addAll(generatePawnMoves(posn, startSq, colour, checkInfo, kingsSquare, blocksCheck));
         } else if (Pieces.isKnight(pt)) {
            moves.addAll(generateKnightMoves(posn, startSq, kingsSquare, colour, checkInfo != null, blocksCheck));
         } else if (Pieces.isKing(pt)) {
            throw new IllegalStateException(String.format("called processSquare with King, posn: %s", posn));
         } else {
            // process sliding pieces ...
            for (int offset : Pieces.toPiece(pt).getMoveOffsets()) { // process each square along the ray
               int nextSq = startSq;
               while (true) {
                  nextSq = Board.getMailboxSquare(nextSq, offset);
                  if (nextSq == -1) {
                     break; // outside board
                  }
                  Move move = generateEitherMoveOrCapture(posn, startSq, nextSq, colour);
                  // stop processing ray if a friendly piece is occupying this ray (move==null) or
                  // a capture
                  if (move != null) {
                     if (checkInfo != null && moveBlocksCheck(posn, move, kingsSquare, blocksCheck) == BlockedCheckPossibility.NOT_BLOCKED) {
                        // ignore move since did not block the check
                     } else {
                        moves.add(move);
                     }
                     if (move.isCapture()) { break; }
                  } else {
                     // null move ==> a friendly piece is occupying this ray
                     break;
                  }
               }
            }
         }
      }
   }

   /**
    * Generates either a move or a capture from originSq to targetSq. Null if the targetSq contains a piece of our colour.
    * 
    * NB: does not check for king adjacent to opponent king.
    * 
    * @param posn
    * @param startSq
    * @param targetSq targetSq. If empty, the object generated will be a plain move. If contains an enemy piece, the move will be a capture. If
    *                 contains a friendly piece, no move object will be generated.
    * @param colour
    * @return a move object or null if the targetSq contains a piece of our colour
    */
   private Move generateEitherMoveOrCapture(Position posn, int startSq, int targetSq, Colour colour) {
      if (!posn.squareIsEmpty(targetSq) && colour == posn.colourOfPieceAt(targetSq)) { return null; }

      // the Move constructor will identify a capture if there's a piece on the targetSq
      return new Move(startSq, posn.pieceAt(startSq), targetSq, posn.pieceAt(targetSq), (byte) 0);
   }

   /**
    * Generates moves for knight at 'startSq'. If our king is already in check, checks if the move blocks the check/captures at the checking
    * square.
    * 
    * @param posn
    * @param startSq
    * @param colour
    * @param kingInCheck true if our king is in check
    * @param blocksCheck a cache of already processed squares, 1 if a sq blocks the check, 0 if not yet calculated
    * @return all knight moves
    */
   private List<Move> generateKnightMoves(Position posn, int startSq, int kingsSquare, Colour colour, boolean kingInCheck, int[] blocksCheck) {
      List<Move> moves = new ArrayList<>();
      for (int targetSq : knightMoves[startSq]) {
         Move move = generateEitherMoveOrCapture(posn, startSq, targetSq, colour);
         if (move != null) {
            if (kingInCheck && moveBlocksCheck(posn, move, kingsSquare, blocksCheck) == BlockedCheckPossibility.NOT_BLOCKED) { continue; }
            moves.add(move);
         }
      }
      return moves;
   }

   private <T> void addIfNotNull(List<T> list, T object) {
      if (object != null) { list.add(object); }
   }

   /**
    * Information about the state of a particular square along a ray from our king.
    */
   record SquareInfo(State state, int square) {
      enum State {
         /** piece is pinned against our king */
         PINNED,
         /** path to our king is blocked further along the ray */
         PATH_TO_KING_BLOCKED,
         /**
          * an enemy piece was found along the ray to our king, but it cannot check along this ray
          */
         ENEMY_PIECE_FOUND_CANNOT_CHECK,
         /** an enemy piece was not found along the ray */
         ENEMY_PIECE_NOT_FOUND,
         /** not yet analysed */
         UNKNOWN;
      }
   }

   /**
    * Is our king in check after the given move? I.e. is there an enemy piece capable of giving check on the appropriate ray.
    * 
    * @param posn
    * @param m                      the move to check
    * @param ray                    the ray to check
    * @param kingsSquare
    * @param colour
    * @param squareInfo             stores the squares of pinned pieces; use new int[] { -1, -1, -1, -1, -1, -1, -1, -1 } if not applicable.
    * @param squaresWhichAttackKing a cache of previously processed squares
    * @return true if the given move leaves the king in check
    */
   private boolean moveLeavesOurKingInCheckAlongRay(Position posn, Move m, Ray ray, int kingsSquare, Colour colour, SquareInfo[] squareInfo,
         RayCacheInfo[] squaresWhichAttackKing) {
      SquareInfo rayInfo;
      if (squareInfo[ray.ordinal()] == null) {
         rayInfo = new SquareInfo(SquareInfo.State.UNKNOWN, -1);
         squareInfo[ray.ordinal()] = rayInfo;
      } else {
         rayInfo = squareInfo[ray.ordinal()];
      }
      // return immediately if this move involves a pinned piece...
      if (rayInfo.state == SquareInfo.State.PINNED && m.getOrigin() == rayInfo.square) {
         // ... but a move along the ray is still ok (includes capture along same ray)
         if (moveOnSameRayOrOpposite(m, ray)) {
            if (verbose) { System.out.println(String.format("move %s with pinned piece is ok because along same ray as pin", m)); }
            return false;
         } else {
            if (verbose) { System.out.println(String.format("move %s illegal b/c piece at %s is pinned", m, Square.toSquare(rayInfo.square))); }
            return true;
         }
      }
      // no further processing if this move is 'further' along a ray where a pinned
      // piece has already been found
      // (nb this method is called with moves sorted with squares closest to king
      // first)
      if (rayInfo.state == State.PINNED) {
         if (verbose) { System.out.println(String.format("not processing move %s b/c piece at %s is pinned", m, Square.toSquare(rayInfo.square))); }
         return false;
      }
      // same again: if a piece is blocking the path to the king, the currently moving
      // piece cannot be pinned
      if (rayInfo.state == State.PATH_TO_KING_BLOCKED) {
         // one exception: enpassant where the pawn that's being captured is on the ray
         // to the king
         // we just keep going here in that case (ignoring the cache)
         if (!m.isEnpassant()) {
            if (verbose) { System.out.println(String.format("not processing move %s b/c ray to king is blocked", m)); }
            return false;
         }
      }
      if (verbose) {
         System.out.println(String.format("processing move %s: sq %s is on ray %s from King (%s)", m, Square.toSquare(m.getOrigin()), ray.getAbbreviation(),
               Square.toSquare(kingsSquare)));
      }

      if (m.isEnpassant()) {
         if (interveningSquaresAreEmpty(posn, kingsSquare, m.getOrigin(), m.getSquareOfPawnCapturedEnpassant(), ray) != -1) { return false; }
      } else {
         // has the ray already been analysed? Again, the cache must be ignored for
         // enpassant
         if (rayInfo.state == State.ENEMY_PIECE_NOT_FOUND || rayInfo.state == State.ENEMY_PIECE_FOUND_CANNOT_CHECK) {
            if (verbose) { System.out.println(String.format("not further checking move %s for enemy pieces since state is %s", m, rayInfo.state)); }
            return false;
         }
         if (moveOnSameRayOrOpposite(m, ray)) {
            if (verbose) { System.out.println(String.format("move %s cannot leave king in check because moving along same ray", m)); }
            return false;
         }
         // If there's a piece between the moving piece and the king, then the king
         // cannot be
         // left in check, so don't process this move anymore
         if (interveningSquaresAreEmpty(posn, kingsSquare, m.getOrigin(), -1, ray) != -1) {
            squareInfo[ray.ordinal()] = new SquareInfo(State.PATH_TO_KING_BLOCKED, m.getOrigin());
            if (verbose) { System.out.println(String.format("stored PATH_TO_KING_BLOCKED for square %s", Square.toSquare(m.getOrigin()))); }
            return false;
         }
      }

      // .. now see if there's an enemy piece on this ray
      PieceSquareInfo enemyPieceInfo = posn.opponentsPieceOnRay(colour, m.getOrigin(), ray, m.isEnpassant() ? m.getSquareOfPawnCapturedEnpassant() : -1);
      if (enemyPieceInfo.piece() != 0) {
         if (verbose) {
            System.out.println(String.format(".. found enemy piece %s at %s on same ray whilst processing move %s", enemyPieceInfo.piece(),
                  Square.toSquare(enemyPieceInfo.square()), m));
         }
         // ... which is capable of checking the king
         if (Pieces.canSlideAlongRay(enemyPieceInfo.piece(), ray)) {
            // be on the safe side, in case the pawn removed by enpassant is on the ray in question
            if (!m.isEnpassant()) { squareInfo[ray.ordinal()] = new SquareInfo(State.PINNED, m.getOrigin()); }
            if (!moveOnSameRayOrOpposite(m, ray)) {
               if (verbose) {
                  System.out.println(String.format("piece at %s is pinned by %s at %s", Square.toSquare(m.getOrigin()), enemyPieceInfo.piece(),
                        Square.toSquare(enemyPieceInfo.square())));
               }
               return true;
            } else {
               if (verbose) { System.out.println(String.format("move %s with pinned piece is ok because along same ray as pin", m)); }
               return false;
            }
         } else {
            if (verbose) { System.out.println(String.format("enemy piece found on ray %s but not capable of checking king", ray)); }
            squareInfo[ray.ordinal()] = new SquareInfo(State.ENEMY_PIECE_FOUND_CANNOT_CHECK, m.getOrigin());
         }
      } else {
         if (verbose) { System.out.println(String.format("no enemy piece found on ray %s", ray)); }
         squareInfo[ray.ordinal()] = new SquareInfo(State.ENEMY_PIECE_NOT_FOUND, m.getOrigin());
      }
      return false;
   }

   /**
    * Are all squares between origin and target empty?
    * 
    * @param origin         start square (e.g. kings square)
    * @param target         target square (e.g. move's origin square)
    * @param squareToIgnore optional square to ignore, i.e. this square will be treated as being empty. E.g. contains a pawn, which could be
    *                       taken enpassant
    * @param ray            the required ray to check
    * @return -1 if all squares between origin and target along the given ray are empty, otherwise the square which is not empty
    */
   private int interveningSquaresAreEmpty(Position posn, int origin, int target, int squareToIgnore, Ray ray) {
      IntFunction<Boolean> isEmptySquare = sq -> { return sq == squareToIgnore || posn.squareIsEmpty(sq); };
      return interveningSquaresAreEmpty(isEmptySquare, origin, target, ray);
   }

   /**
    * Are all squares between origin and target empty? 'empty' is defined by the isSquareEmptyPredicate (can be different for enpassant).
    * 
    * @param isSquareEmptyPredicate predicate to use to check if a square is empty
    * @param origin                 start square (e.g. kings square)
    * @param target                 target square (e.g. move's origin square)
    * @param ray                    the required ray to check
    * @return -1 if all squares between origin and target along the given ray are empty, otherwise the square which is not empty
    */
   private int interveningSquaresAreEmpty(IntFunction<Boolean> isSquareEmptyPredicate, int origin, int target, Ray ray) {
      for (int interveningSq : Ray.raysList[origin][ray.ordinal()]) {
         if (interveningSq == target) { return -1; }
         if (!isSquareEmptyPredicate.apply(interveningSq)) { return interveningSq; }
      }
      // have either hit the target square, or the ray is finished (==> origin and
      // target are not on the same ray)
      return -1;
   }

   /**
    * Inspects all moves <B>on the given rays</B> (must be sorted with origin squares closest to kingsSquare occurring first). If after the
    * move the king is in check, then the piece was pinned and the move will be removed from the list.
    * 
    * @param posn
    * @param kingsSquare
    * @param colour
    * @param movesOnRay  the moves to inspect, stored for each ray as seen from the king's square, sorted with origin squares closest to king
    *                    first
    */
   private void removeMovesLeavingKingInCheckAlongRay(Position posn, int kingsSquare, Colour colour, List<Move>[] movesOnRay) {
      SquareInfo[] pinnedPieces = new SquareInfo[8]; // stores squares of pinned pieces for each ray
      RayCacheInfo[] squaresWhichAttackKing = new RayCacheInfo[64]; // this stores the result of processed squares
      // (for sliding pieces)
      for (Ray ray : Ray.values()) {
         Iterator<Move> moveIter = movesOnRay[ray.ordinal()].iterator();
         while (moveIter.hasNext()) {
            Move m = moveIter.next();
            if (moveLeavesOurKingInCheckAlongRay(posn, m, ray, kingsSquare, colour, pinnedPieces, squaresWhichAttackKing)) { moveIter.remove(); }
         }
      }
   }

   /**
    * @param m   move
    * @param ray ray
    * @return true if the given move is along the given ray (or its opposite) (i.e. the piece is not pinned)
    */
   private boolean moveOnSameRayOrOpposite(Move m, Ray ray) {
      return ray.onSameRay(m.getOrigin(), m.getTarget()) || ray.getOpposite().onSameRay(m.getOrigin(), m.getTarget());
   }

   // if the king is in check is not verified by this method
   /* package */ boolean canCastleKingsside(Position posn, Colour colour) {
      if (!posn.canCastleKingsside(colour)) { return false; }
      int colourOrd = colour.ordinal();
      final Colour oppositeColour = colour.opposite();
      if (!Pieces.isRook(posn.pieceAt(rooksCastlingSquareIndex[colourOrd][0]))) {
         throw new IllegalStateException("rook not present for " + colour + " castling king's side? posn:\n" + posn);
      }

      for (int sq : unoccupiedSquaresKingssideCastling[colourOrd]) {
         if (!posn.squareIsEmpty(sq)) { return false; }
      }

      /*
       * @formatter:off
       *  
       * Cannot castle over a square in check.
       *
       * From White's POV:
       *   Previous algorithm looked for opponent's pawns on the 2nd rank, then knights, and then inspected the rays NW, N, NE from the two intervening squares f1, g1 for 'sliding pieces'.
       *   Now checks the knights and then just checks the rays, and if a pawn is found make sure it's on the 2nd rank.
       *   Still checks the knights first, since this is probably cheaper then checking the rays.
       *
       * @formatter:on
       */

      final byte oppositeColourKnight = Pieces.generateKnight(oppositeColour);
      for (int sq : knightSquaresKingssideCastling[colourOrd]) {
         if (posn.matchesPieceTypeAndColour(sq, oppositeColourKnight)) { return false; }
      }
      final int requiredPawnRank = colour == Colour.WHITE ? 1 : 6; // pawn must be on this rank to prevent castling
      for (int sq : unoccupiedSquaresKingssideCastling[colourOrd]) {
         for (Ray ray : Ray.RAYS_TO_CHECK_KINGSSIDE_CASTLING[colourOrd]) {
            PieceSquareInfo enemyPieceInfo = posn.opponentsPieceOnRay(colour, sq, ray);
            // found piece capable of checking the king? (sliding piece on the ray, or a pawn one rank away)
            if (enemyPieceInfo.piece() != 0 && //
                  ((Pieces.isPawn(enemyPieceInfo.piece()) && Square.toSquare(enemyPieceInfo.square()).rank() == requiredPawnRank)
                        || Pieces.canSlideAlongRay(enemyPieceInfo.piece(), ray))) {
               return false;
            }
         }
      }
      return true;
   }

   // if the king is in check is not verified by this method
   /* package */ boolean canCastleQueensside(Position posn, Colour colour) {
      if (!posn.canCastleQueensside(colour)) { return false; }
      int colourOrd = colour.ordinal();
      if (!Pieces.isRook(posn.pieceAt(rooksCastlingSquareIndex[colourOrd][1]))) {
         throw new IllegalStateException("rook not present for " + colour + " castling queen's side? posn:\n" + posn);
      }
      for (int sq : unoccupiedSquaresQueenssideCastling[colourOrd]) {
         if (!posn.squareIsEmpty(sq)) { return false; }
      }

      // cannot castle over a square in check
      // (see description in canCastleKingsside)
      final byte oppositeColourKnight = Pieces.generateKnight(colour.opposite());
      for (int sq : knightSquaresQueenssideCastling[colourOrd]) {
         if (posn.matchesPieceTypeAndColour(sq, oppositeColourKnight)) { return false; }
      }
      final int requiredPawnRank = colour == Colour.WHITE ? 1 : 6; // pawn must be on this rank to prevent castling
      for (int sq : unoccupiedSquaresQueenssideCastling[colourOrd]) {
         // the b1/b8 squares don't need to be inspected for the 'square in check' calculation:
         if (sq == Square.b1.index() || sq == Square.b8.index()) { continue; }
         for (Ray ray : Ray.RAYS_TO_CHECK_QUEENSSIDE_CASTLING[colourOrd]) {
            PieceSquareInfo enemyPieceInfo = posn.opponentsPieceOnRay(colour, sq, ray);
            // found piece capable of checking the king? (sliding piece on the ray, or a pawn one rank away)
            if (enemyPieceInfo.piece() != 0 && //
                  ((Pieces.isPawn(enemyPieceInfo.piece()) && Square.toSquare(enemyPieceInfo.square()).rank() == requiredPawnRank)
                        || Pieces.canSlideAlongRay(enemyPieceInfo.piece(), ray))) {
               return false;
            }
         }
      }
      return true;
   }

   /**
    * Generates moves for pawn at 'startSq'. If our king is already in check, then the move must capture at the checking square or block the
    * ray.
    * 
    * @param posn
    * @param startSq
    * @param colour
    * @param checkInfo   non-null if our king is in check
    * @param blocksCheck a cache of already processed squares, 1 if a sq blocks the check, 0 if not yet calculated
    * @return all pawn moves
    */
   private List<Move> generatePawnMoves(Position posn, int startSq, Colour colour, PieceSquareInfo checkInfo, int kingsSquare, int[] blocksCheck) {
      List<Move> moves = new ArrayList<>();
      // following cases ('reverse' for black pawns):
      // - pawn on 2nd rank can move 1 or two squares forward
      // - capturing diagonally
      // - pawn on 7th rank can promote
      // - enpassant

      // if king already in check, then a move has to block or capture:
      // - normal pawn move: must block ray
      // - pawn capture (including promotion capture or enpassant): captures checking piece or blocks ray

      // get the next square forward: returns -1 if then will be on the promotion rank
      int nextSq = colour == Colour.WHITE ? Board.getMailboxSquareForWhitePawnsOneSquareForward(startSq)
            : Board.getMailboxSquareForBlackPawnsOneSquareForward(startSq);
      // promotion
      if (nextSq == -1) {
         // Need to get the nextSq again (the previous call just returned -1 for "promotion" rank)
         // However, promotion does not occur very often, therefore better to optimise the normal case
         int forwardOffset = colour == Colour.WHITE ? -10 : 10;
         nextSq = Board.getMailboxSquare(startSq, forwardOffset);
         moves.addAll(generatePossibleNormalPromotionMoves(posn, startSq, nextSq, kingsSquare, checkInfo, colour, blocksCheck));
         for (int targetSq : pawnCaptures[colour.ordinal()][startSq]) {
            moves.addAll(generatePossibleCapturePromotionMoves(posn, startSq, targetSq, kingsSquare, checkInfo, colour, blocksCheck));
         }
      } else {
         // process 1 square forward, and then optionally followed by 2 squares forward
         // NB 'nextSq' square cannot be outside of the board, we've already checked for
         // the 'promotion' rank
         if (posn.squareIsEmpty(nextSq)) {
            if (checkInfo != null && !moveToSquareBlocksCheck(checkInfo, nextSq, kingsSquare, blocksCheck)) {
               // ignore, since move does not block the check
            } else {
               moves.add(generateMove(posn, startSq, nextSq));
            }
            if (onPawnStartRank(startSq, colour)) {
               nextSq = colour == Colour.WHITE ? Board.getMailboxSquareForWhitePawnsOneSquareForward(nextSq)
                     : Board.getMailboxSquareForBlackPawnsOneSquareForward(nextSq);
               if (posn.squareIsEmpty(nextSq)) {
                  if (checkInfo != null && !moveToSquareBlocksCheck(checkInfo, nextSq, kingsSquare, blocksCheck)) {
                     // ignore, since move does not block the check
                  } else {
                     moves.add(Move.createPawnTwoSquaresForwardMove(startSq, posn.pieceAt(startSq), nextSq));
                  }
               }
            }
         }
         // captures
         for (int targetSq : pawnCaptures[colour.ordinal()][startSq]) {
            Move move = generatePawnCaptureMoveIfPossible(posn, startSq, targetSq, colour);
            if (move != null) {
               if (checkInfo != null && moveBlocksCheck(posn, move, kingsSquare, blocksCheck) == BlockedCheckPossibility.NOT_BLOCKED) { continue; }
               moves.add(move);
            }
         }
      }
      if (posn.getEnpassantSquare() != null) {
         int epSquare = posn.getEnpassantSquare().index();
         for (int sq : enpassantSquares[epSquare]) {
            if (startSq == sq) {
               Move move = Move.createEnpassantMove(startSq, posn.pieceAt(startSq), epSquare);
               if (checkInfo != null && moveBlocksCheck(posn, move, kingsSquare, blocksCheck) == BlockedCheckPossibility.NOT_BLOCKED) { continue; }
               moves.add(move);
            }
         }
      }
      return moves;
   }

   // caters for pawn advancing one square and promoting
   private List<Move> generatePossibleNormalPromotionMoves(Position posn, int origin, int target, int kingsSquare, PieceSquareInfo checkInfo, Colour myColour,
         int[] blocksCheck) {
      List<Move> moves = new ArrayList<>();
      if (posn.squareIsEmpty(target)) {
         // ignore all promotion move candidates if they don't block the check
         if (checkInfo != null && !moveToSquareBlocksCheck(checkInfo, target, kingsSquare, blocksCheck)) { return moves; }
         for (Piece pt : new Piece[] { Piece.ROOK, Piece.KNIGHT, Piece.BISHOP, Piece.QUEEN }) {
            moves.add(Move.createPromotionMove(origin, posn.pieceAt(origin), target, Pieces.generatePiece(pt, myColour)));
         }
      }
      return moves;
   }

   // caters for pawn capturing a piece and promoting
   private List<Move> generatePossibleCapturePromotionMoves(Position posn, int origin, int target, int kingsSquare, PieceSquareInfo checkInfo, Colour myColour,
         int[] blocksCheck) {
      List<Move> moves = new ArrayList<>();
      if (!posn.squareIsEmpty(target) && myColour.opposes(posn.colourOfPieceAt(target))) {
         BlockedCheckPossibility checkBlockedAfterMove = null;
         // If the check still exists after this first promotion move, then it will for
         // the other moves too and therefore we don't need to evaluate them
         Move move = Move.createPromotionCaptureMove(origin, posn.pieceAt(origin), target, posn.pieceAt(target), Pieces.generateRook(myColour));
         if (checkInfo != null) {
            checkBlockedAfterMove = moveBlocksCheck(posn, move, kingsSquare, blocksCheck);
            if (checkBlockedAfterMove == BlockedCheckPossibility.NOT_BLOCKED) { return moves; }
         }
         moves.add(move);
         // now process the other promotion moves without regarding any existing check
         for (Piece pt : new Piece[] { Piece.KNIGHT, Piece.BISHOP, Piece.QUEEN }) {
            moves.add(Move.createPromotionCaptureMove(origin, posn.pieceAt(origin), target, posn.pieceAt(target), Pieces.generatePiece(pt, myColour)));
         }
      }
      return moves;
   }

   private Move generatePawnCaptureMoveIfPossible(Position posn, int from, int to, Colour colour) {
      if (!posn.squareIsEmpty(to)) {
         byte pieceAtTarget = posn.pieceAt(to);
         if (colour.opposes(Pieces.colourOf(pieceAtTarget))) { return Move.createCapture(from, posn.pieceAt(from), to, pieceAtTarget); }
         // TODO posn.pieceAt(from) never changes, pass in as parameter?
      }
      return null;
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
      return Move.createMove(from, posn.pieceAt(from), to);
   }

   private Move generateCapture(Position posn, int from, int to) {
      return Move.createCapture(from, posn.pieceAt(from), to, posn.pieceAt(to));
   }

   /**
    * Stores info about a square in relation to a particular target square (often, the opposing king's square).
    */
   public static class RayCacheInfo {
      public enum RayCacheState {
         NO_RAY_TO_KING, CLEAR_PATH_TO_KING, NO_CLEAR_PATH_TO_KING;
      }

      /**
       * stores the ray to the target square or null. Only set if 'state' != NO_RAY_TO_KING
       */
      Ray rayBetween;
      /** whether there's a clear path to the target */
      RayCacheState state;

      private RayCacheInfo(Ray rayBetween, RayCacheState state) {
         this.rayBetween = rayBetween;
         this.state = state;
      }

      public static RayCacheInfo clearPath(Ray rayBetween) {
         return new RayCacheInfo(rayBetween, RayCacheState.CLEAR_PATH_TO_KING);
      }

      public static RayCacheInfo noClearPath(Ray rayBetween) {
         return new RayCacheInfo(rayBetween, RayCacheState.NO_CLEAR_PATH_TO_KING);
      }

      public static RayCacheInfo noRayToKing() {
         return new RayCacheInfo(null, RayCacheState.NO_RAY_TO_KING);
      }

      @Override
      public String toString() {
         return state + (rayBetween == null ? "" : "-" + rayBetween);
      }
   }
}
