/**
 * @author rich
 * @since 2019-02-27
 */
module org.rjo.chess.base {
   exports org.rjo.chess.base;
   exports org.rjo.chess.base.bits;
   exports org.rjo.chess.base.eval;
   exports org.rjo.chess.base.ray;

   // requires javolution;
   requires org.apache.lucene.core;
}
