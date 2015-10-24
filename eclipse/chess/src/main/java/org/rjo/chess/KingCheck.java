package org.rjo.chess;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;

import org.rjo.chess.pieces.Knight;
import org.rjo.chess.pieces.Pawn;
import org.rjo.chess.pieces.PieceType;
import org.rjo.chess.ray.Ray;
import org.rjo.chess.ray.RayFactory;
import org.rjo.chess.ray.RayType;
import org.rjo.chess.ray.RayUtils;

/**
 * Routines to discover if the king is in check.
 *
 * @author rich
 *
 */
public class KingCheck {

   private KingCheck() {
   }

   /**
    * Returns true if the king is currently in check. NB stops as soon as at least one check is discovered.
    * <p>
    * Examines opponent's knights and pawns for checks, and all rays. This is therefore suitable for a game situation
    * where the king has just moved, or the king was in check beforehand.
    * <p>
    * For an optimized version use {@link #isKingInCheck(Square, Colour, BitSet, Map, RayType)}, specifying the 5th
    * parameter.
    *
    * @param kingsSquare
    *           where the king is.
    * @param kingsColour
    *           colour of the king.
    * @param friendlyPieces
    *           bitset indicating location of the friendly pieces.
    * @param enemyPieces
    *           bitsets indicating location of the enemy pieces.
    * @return true if the king is in check.
    */
   public static boolean isKingInCheck(Square kingsSquare, Colour kingsColour, BitSet friendlyPieces,
         Map<PieceType, BitSet> enemyPieces) {
      return isKingInCheck(kingsSquare, kingsColour, friendlyPieces, enemyPieces, (RayType) null);
   }

   /**
    * Returns true if the king is currently in check. NB stops as soon as at least one check is discovered.
    * <p>
    * Suitable for multithreaded use, since does not modify any global state.
    * TODO: could be expanded to return which piece is checking.
    *
    * @param kingsSquare
    *           where the king is.
    * @param kingsColour
    *           colour of the king.
    * @param friendlyPieces
    *           bitset indicating location of the friendly pieces.
    * @param enemyPieces
    *           bitsets indicating location of the enemy pieces.
    * @param rayToExamine
    *           if set, JUST this ray will be examined. This is an optimization where the king was not in check
    *           beforehand. Then we only need to check the ray which has been vacated by the moving piece. <b>Do not
    *           set</b> if the king himself has moved.
    * @return true if the king is in check.
    */
   public static boolean isKingInCheck(Square kingsSquare, Colour kingsColour, BitSet friendlyPieces,
         Map<PieceType, BitSet> enemyPieces, RayType rayToExamine) {

      boolean optimizedRaySearch = (rayToExamine != null);

      /*
       * The algorithm first handles the special cases of pawn or knight checks.
       *
       * Then, for each ray emnating from the king's square, the squares on the ray get checked.
       * If the square contains an enemy piece then this is checked for a possible check.
       */

      if (!optimizedRaySearch) {
         // special cases: pawn and knight attacks
         if (Knight.attacksSquare(kingsSquare, enemyPieces.get(PieceType.KNIGHT))) {
            return true;
         }
         if (Pawn.attacksSquare(kingsSquare, Colour.oppositeColour(kingsColour), enemyPieces.get(PieceType.PAWN))) {
            return true;
         }
      }

      BitBoard allEnemyPieces = new BitBoard();
      for (PieceType pt : enemyPieces.keySet()) {
         allEnemyPieces.getBitSet().or(enemyPieces.get(pt));
      }

      RayType[] raysToCheck;
      if (optimizedRaySearch) {
         raysToCheck = new RayType[] { rayToExamine };
      } else {
         raysToCheck = RayType.values();
      }

      for (RayType rayType : raysToCheck) {
         Ray ray = RayFactory.getRay(rayType);
         Iterator<Integer> rayIter = ray.squaresFrom(kingsSquare);
         while (rayIter.hasNext()) {
            int bitIndex = rayIter.next();
            // stop at once if a friendly piece is on this ray
            if (friendlyPieces.get(bitIndex)) {
               break;
            }
            // an enemy piece is relevant for diagonal (queen/bishop) or file (queen/rook)
            if (allEnemyPieces.getBitSet().get(bitIndex)) {
               if (ray.isDiagonal() && (enemyPieces.get(PieceType.QUEEN).get(bitIndex)
                     || enemyPieces.get(PieceType.BISHOP).get(bitIndex))) {
                  return true;
               } else if (!ray.isDiagonal() && (enemyPieces.get(PieceType.QUEEN).get(bitIndex)
                     || enemyPieces.get(PieceType.ROOK).get(bitIndex))) {
                  return true;
               } else {
                  break;
               }
            }
         }

      }
      return false;
   }

   /**
    * Returns true if the king would be in check after the given move.
    * <p>
    * Helper-Method, delegates to {@link #isKingInCheckAfterMove(Square, Colour, BitSet, Map, Move, boolean)} with last
    * parameter==true.
    * <p>
    * <b>Use this procedure if the king was already in check before the given move.</b>
    *
    * @param kingsSquare
    *           where the king is. If 'move' indicated that the king has moved, this value will be ignored and the
    *           king's new square will be calculated.
    * @param kingsColour
    *           colour of the king.
    * @param friendlyPieces
    *           bitset indicating location of the friendly pieces (pre-move).
    * @param enemyPieces
    *           bitsets indicating location of the enemy pieces (pre-move).
    * @param move
    *           the move to make
    * @return true if the king would be in check after the move.
    */
   public static boolean isKingInCheckAfterMove_PreviouslyWasInCheck(Square kingsSquare, Colour kingsColour,
         BitSet friendlyPieces, Map<PieceType, BitSet> enemyPieces, Move move) {
      return isKingInCheckAfterMove(kingsSquare, kingsColour, friendlyPieces, enemyPieces, move, true);
   }

   /**
    * Returns true if the king would be in check after the given move.
    * <p>
    * Helper-Method, delegates to {@link #isKingInCheckAfterMove(Square, Colour, BitSet, Map, Move, boolean)} with last
    * parameter==false.
    * <p>
    * <b>Use this procedure if the king was NOT in check before the given move.</b>
    *
    * @param kingsSquare
    *           where the king is. If 'move' indicated that the king has moved, this value will be ignored and the
    *           king's new square will be calculated.
    * @param kingsColour
    *           colour of the king.
    * @param friendlyPieces
    *           bitset indicating location of the friendly pieces (pre-move).
    * @param enemyPieces
    *           bitsets indicating location of the enemy pieces (pre-move).
    * @param move
    *           the move to make
    * @return true if the king would be in check after the move.
    */
   public static boolean isKingInCheckAfterMove_PreviouslyNotInCheck(Square kingsSquare, Colour kingsColour,
         BitSet friendlyPieces, Map<PieceType, BitSet> enemyPieces, Move move) {
      return isKingInCheckAfterMove(kingsSquare, kingsColour, friendlyPieces, enemyPieces, move, false);
   }

   /**
    * Returns true if the king would be in check after the given move. This method can be called directly, but there are
    * helper methods available (see isKingInCheckAfterMove_PreviouslyNotInCheck and
    * isKingInCheckAfterMove_PreviouslyWasInCheck).
    * <p>
    * <b>If the king is already in check before the given move, you must set the parameter kingWasInCheck to true.</b>
    * <p>
    * An optimised search is used. Assuming the king himself has not moved, and the king was not previously in check,
    * only the ray given by the king's square and the vacated square needs to be examined.
    *
    * @param kingsSquare
    *           where the king is. If 'move' indicated that the king has moved, this value will be ignored and the new
    *           king's square will be calculated.
    * @param kingsColour
    *           colour of the king.
    * @param friendlyPieces
    *           bitset indicating location of the friendly pieces (pre-move).
    * @param enemyPieces
    *           bitsets indicating location of the enemy pieces (pre-move).
    * @param move
    *           the move to make
    * @param kingWasInCheck
    *           indicates that the king was in check before this move. Therefore, cannot use the optimized ray search.
    * @return true if the king would be in check after the move.
    */
   public static boolean isKingInCheckAfterMove(Square kingsSquare, Colour kingsColour, BitSet friendlyPieces,
         Map<PieceType, BitSet> enemyPieces, Move move, boolean kingWasInCheck) {

      friendlyPieces = (BitSet) friendlyPieces.clone();
      friendlyPieces.set(move.to().bitIndex());
      friendlyPieces.clear(move.from().bitIndex());

      // may not be strictly necessary, but is consistent
      if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
         friendlyPieces.set(move.getRooksCastlingMove().to().bitIndex());
         friendlyPieces.clear(move.getRooksCastlingMove().from().bitIndex());
      }

      if (move.isCapture()) {
         // need to modify BitSet for the opponent's captured piece,
         // therefore clone and resave in 'enemyPieces'
         BitSet opponentsCapturedPiece = (BitSet) enemyPieces.get(move.getCapturedPiece()).clone();
         enemyPieces.put(move.getCapturedPiece(), opponentsCapturedPiece);
         // .. and remove captured piece
         Square capturedPieceSquare = move.to();
         if (move.isEnpassant()) {
            capturedPieceSquare = Square.findMoveFromEnpassantSquare(move.to());
         }
         opponentsCapturedPiece.clear(capturedPieceSquare.bitIndex());
      }

      boolean kingMoved = false;

      // update 'kingsSquare' if king has moved
      if (move.getPiece() == PieceType.KING) {
         kingsSquare = move.to();
         kingMoved = true;
      }

      // no optimizations if the king moved or was in check beforehand
      if (kingMoved || kingWasInCheck) {
         return isKingInCheck(kingsSquare, kingsColour, friendlyPieces, enemyPieces);
      } else {
         // can optimize by only searching the ray given by the direction kingsSquare -> move.from()
         Ray ray = RayUtils.getRay(kingsSquare, move.from());
         if (ray == null) {
            return false;
         } else {
            return isKingInCheck(kingsSquare, kingsColour, friendlyPieces, enemyPieces, ray.getRayType());
         }
      }
   }

}
