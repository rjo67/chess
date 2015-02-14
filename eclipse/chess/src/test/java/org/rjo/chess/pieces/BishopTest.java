package org.rjo.chess.pieces;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.rjo.chess.Colour;
import org.rjo.chess.Square;

import static org.junit.Assert.assertTrue;

public class BishopTest {

   @Test
   public void locations() {
      Bishop b = new Bishop(Colour.WHITE);
      b.initPosition();
      Square[] locn = b.getLocations();
      Set<Square> set = new HashSet<>(Arrays.asList(locn));
      assertTrue(set.contains(Square.c1));
      assertTrue(set.contains(Square.f1));
   }

}
