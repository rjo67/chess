package chess.pieces;

import java.util.Set;

import org.junit.Test;

import chess.Colour;
import chess.Square;

import static org.junit.Assert.assertTrue;

public class BishopTest {

   @Test
   public void locations() {
      Bishop b = new Bishop(Colour.WHITE);
      b.initPosition();
      Set<Square> set = b.getLocations();
      assertTrue(set.contains(Square.c1));
      assertTrue(set.contains(Square.f1));
   }

}
