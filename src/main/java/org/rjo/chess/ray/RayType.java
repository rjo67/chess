package org.rjo.chess.ray;

public enum RayType {
   NORTH(), NORTHEAST(), EAST(), SOUTHEAST(), SOUTH(), SOUTHWEST(), WEST(), NORTHWEST();

   public static final RayType[] RAY_TYPES_DIAGONAL = new RayType[] { RayType.SOUTHEAST, RayType.SOUTHWEST,
         RayType.NORTHEAST, RayType.NORTHWEST };
   public static final RayType[] RAY_TYPES_VERTICAL = new RayType[] { RayType.NORTH, RayType.SOUTH };
   public static final RayType[] RAY_TYPES_HORIZONTAL = new RayType[] { RayType.WEST, RayType.EAST };

}
