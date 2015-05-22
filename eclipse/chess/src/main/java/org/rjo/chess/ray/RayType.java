package org.rjo.chess.ray;

public enum RayType {
   //@formatter:off
   NORTH(NorthRay.instance()),
   NORTHEAST(NorthEastRay.instance()),
   EAST(EastRay.instance()),
   SOUTHEAST(SouthEastRay.instance()),
   SOUTH(SouthRay.instance()),
   SOUTHWEST(SouthWestRay.instance()),
   WEST(WestRay.instance()),
   NORTHWEST(NorthWestRay.instance());
   //@formatter:on

   private Ray instance;
   public static final RayType[] RAY_TYPES_DIAGONAL = new RayType[] { RayType.SOUTHEAST, RayType.SOUTHWEST,
         RayType.NORTHEAST, RayType.NORTHWEST };
   public static final RayType[] RAY_TYPES_VERTICAL = new RayType[] { RayType.NORTH, RayType.SOUTH };
   public static final RayType[] RAY_TYPES_HORIZONTAL = new RayType[] { RayType.WEST, RayType.EAST };

   RayType(Ray instance) {
      this.instance = instance;
   }

   public Ray getInstance() {
      return instance;
   }
}
