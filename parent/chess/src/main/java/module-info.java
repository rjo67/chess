/**
 * @author rich
 * @since 2019-02-27
 */
module org.rjo.chess {
	exports org.rjo.chess;
	exports org.rjo.chess.pieces;
	exports org.rjo.chess.position;

	requires transitive org.rjo.chess.base;
	requires org.apache.logging.log4j;
	requires org.apache.commons.lang3;
}