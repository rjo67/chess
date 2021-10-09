package org.rjo.newchess.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author rich
 *
 */
public enum Ray {
   NORTH(0, false, true, "N"), //
   NORTHEAST(1, false, false, "NE"), //
   EAST(2, true, false, "E"), //
   SOUTHEAST(3, false, false, "SE"), //
   SOUTH(4, false, true, "S"), //
   SOUTHWEST(5, false, false, "SW"), //
   WEST(6, true, false, "W"), //
   NORTHWEST(7, false, false, "NW");

   public static final Ray[] RAY_TYPES_DIAGONAL = new Ray[] { Ray.SOUTHEAST, Ray.SOUTHWEST, Ray.NORTHEAST, Ray.NORTHWEST };
   public static final Ray[] RAY_TYPES_VERTICAL = new Ray[] { Ray.NORTH, Ray.SOUTH };
   public static final Ray[] RAY_TYPES_HORIZONTAL = new Ray[] { Ray.WEST, Ray.EAST };

   private final static Comparator<Integer> ASCENDING_COMPARATOR = new Comparator<Integer>() {
      @Override
      public int compare(Integer o1, Integer o2) {
         return o1 - o2;
      }
   };
   private final static Comparator<Integer> DESCENDING_COMPARATOR = new Comparator<Integer>() {
      @Override
      public int compare(Integer o1, Integer o2) {
         return o2 - o1;
      }
   };

   static {
      setOpposites(NORTH, SOUTH);
      setOpposites(NORTHWEST, SOUTHEAST);
      setOpposites(NORTHEAST, SOUTHWEST);
      setOpposites(WEST, EAST);
   }

   public static final Set<Integer>[][] raysSet;
   public static final List<Integer>[][] raysList;
   static {
      // raysSet: stores for each square on the board a set of squares emenating from
      // this square in all directions. The set is ordered so that the squares closest
      // to the origin are first. Not strictly necessary for the set, but useful for
      // creating 'raysList'.
      // This info is duplicated in the 'raysList' structure,
      // e.g. Ray.raysList[startSq][ray.ordinal()] is a List where the first
      // element is the sq closest to startSq in the given direction.
      raysSet = new Set[64][8];
      raysList = new List[64][8];
      int[] offset = new int[] { -10, -9, 1, 11, 10, 9, -1, -11 };
      for (int sq = 0; sq < 64; sq++) {
         for (Ray ray : Ray.values()) {
            Comparator<Integer> comparator = offset[ray.ordinal()] < 0 ? DESCENDING_COMPARATOR : ASCENDING_COMPARATOR;
            raysSet[sq][ray.ordinal()] = new TreeSet<Integer>(comparator);
            for (int raySq = Board.mailbox64(sq) + offset[ray.ordinal()]; //
                  raySq >= 0 && raySq < 120 && Board.mailbox(raySq) != -1; raySq += offset[ray.ordinal()]) {
               raysSet[sq][ray.ordinal()].add(Board.mailbox(raySq));
            }
            raysSet[sq][ray.ordinal()] = Collections.unmodifiableSet(raysSet[sq][ray.ordinal()]);
            raysList[sq][ray.ordinal()] = new ArrayList<>();
            for (Integer raySq : raysSet[sq][ray.ordinal()]) {
               raysList[sq][ray.ordinal()].add(raySq);
            }
            raysList[sq][ray.ordinal()] = Collections.unmodifiableList(raysList[sq][ray.ordinal()]);
         }
      }
   }

   private int index;
   private boolean horizontal;
   private boolean vertical;
   private String abbreviation;
   private Ray opposite;

   Ray(int index, boolean horizontal, boolean vertical, String abbrev) {
      this.index = index;
      this.horizontal = horizontal;
      this.vertical = vertical;
      this.abbreviation = abbrev;
   }

   /**
    * private helper to set up the opposing rays.
    */
   private static void setOpposites(Ray ray1, Ray ray2) {
      ray1.opposite = ray2;
      ray2.opposite = ray1;
   }

   public int getIndex() {
      return index;
   }

   public String getAbbreviation() {
      return abbreviation;
   }

   public boolean isHorizontal() {
      return horizontal;
   }

   public boolean isVertical() {
      return vertical;
   }

   public boolean isDiagonal() {
      return !vertical && !horizontal;
   }

   public Ray getOpposite() {
      return opposite;
   }

   /**
    * Are two given squares on the same ray?
    * 
    * @param  originSq
    * @param  targetSq
    * @return          true if origin and target are on this ray
    */
   public boolean onSameRay(int originSq, int targetSq) {
      return raysSet[originSq][this.ordinal()].contains(targetSq);
   }

   /**
    * Returns the ray connecting two given squares.
    * 
    * @param  originSq
    * @param  targetSq
    * @return          the ray between the two squares, or null if not on the same ray
    */
   public static Ray findRayBetween(int originSq, int targetSq) {
      for (Ray ray : Ray.values()) {
         if (ray.onSameRay(originSq, targetSq)) {
            return ray;
         }
      }
      return null;
   }
}
