package org.rjo.chess.ray;

import java.util.HashMap;
import java.util.Map;

public class RayFactory {

   private static final Map<RayType, Ray> map = new HashMap<>(8);

   static {
      map.put(RayType.NORTH, NorthRay.instance());
      map.put(RayType.NORTHEAST, NorthEastRay.instance());
      map.put(RayType.EAST, EastRay.instance());
      map.put(RayType.SOUTHEAST, SouthEastRay.instance());
      map.put(RayType.SOUTH, SouthRay.instance());
      map.put(RayType.SOUTHWEST, SouthWestRay.instance());
      map.put(RayType.WEST, WestRay.instance());
      map.put(RayType.NORTHWEST, NorthWestRay.instance());
   }

   private RayFactory() {
   }

   public static Ray getRay(RayType type) {
      return map.get(type);
   }

}
