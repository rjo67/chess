package chess;

public class MoveDistance {
   /*
    * An approach with a 225 element table for king move distance, as well for other piece move distances, directions,
    * vector attacks and increment vectors, was used in Pioneer as described by Boris Stilman. The 8x8 array is
    * superimposed on the array 15x15 in such a way that square x coincides with the central square (7,7) of the array
    * 15x15. (This is the square with value 0.)
    * http://chessprogramming.wikispaces.com/Distance
    */
   private static int[][] moveDistances = new int[][] {
      //@formatter:off
      { 7,  7,  7,  7,  7,  7,  7,  7,  7,  7,  7,  7,  7,  7,  7 },
      { 7,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  7 },
      { 7,  6,  5,  5,  5,  5,  5,  5,  5,  5,  5,  5,  5,  6,  7 },
      { 7,  6,  5,  4,  4,  4,  4,  4,  4,  4,  4,  4,  5,  6,  7 },
      { 7,  6,  5,  4,  3,  3,  3,  3,  3,  3,  3,  4,  5,  6,  7 },
      { 7,  6,  5,  4,  3,  2,  2,  2,  2,  2,  3,  4,  5,  6,  7 },
      { 7,  6,  5,  4,  3,  2,  1,  1,  1,  2,  3,  4,  5,  6,  7 },
      { 7,  6,  5,  4,  3,  2,  1,  0,  1,  2,  3,  4,  5,  6,  7 },
      { 7,  6,  5,  4,  3,  2,  1,  1,  1,  2,  3,  4,  5,  6,  7 },
      { 7,  6,  5,  4,  3,  2,  2,  2,  2,  2,  3,  4,  5,  6,  7 },
      { 7,  6,  5,  4,  3,  3,  3,  3,  3,  3,  3,  4,  5,  6,  7 },
      { 7,  6,  5,  4,  4,  4,  4,  4,  4,  4,  4,  4,  5,  6,  7 },
      { 7,  6,  5,  5,  5,  5,  5,  5,  5,  5,  5,  5,  5,  6,  7 },
      { 7,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  7 },
      { 7,  7,  7,  7,  7,  7,  7,  7,  7,  7,  7,  7,  7,  7,  7 }
      //@formatter:on
   };
   // these constants specify the location of the slot with value '0' in the above array.
   private static int CENTRE_RANK = 7;
   private static int CENTRE_FILE = 7;

   /**
    * Returns the 'Chebyshev distance' (the minimal number of king moves between two squares on the otherwise empty
    * board).
    * 
    * @param start
    *           start square
    * @param finish
    *           finish square
    * @return the distance between the two squares (0..7).
    */
   public static int calculateDistance(Square start, Square finish) {
      // place 8x8 board so that the square 'start' is located at (7,7) in the above array
      int rankOffset = CENTRE_RANK - start.rank();
      int fileOffset = CENTRE_FILE - start.file();
      return moveDistances[rankOffset + finish.rank()][fileOffset + finish.file()];
   }
}
