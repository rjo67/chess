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

   RayType(Ray instance) {
      this.instance = instance;
   }

   public Ray getInstance() {
      return instance;
   }
}
