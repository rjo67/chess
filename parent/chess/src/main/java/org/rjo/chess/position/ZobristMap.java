package org.rjo.chess.position;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.rjo.chess.SystemFlags;
import org.rjo.chess.base.eval.SearchResult;

/**
 * Stores a map of zobrist hashs and positions.
 *
 * @author rich
 * @since 2018-01-13
 */
public class ZobristMap {

	private Map<Long, ZobristInfo> zobristMap;

	public ZobristMap() {
		this.zobristMap = new HashMap<>(4000);
	}

	/**
	 * empty the map.
	 */
	public void clear() {
		this.zobristMap.clear();
	}

	/**
	 * check if the given position has already been evaluated. If so, returns its PositionScore.
	 *
	 * @param posn the position
	 * @return positionscore if found.
	 */
	public Optional<ZobristInfo> checkZobrist(Position posn) {
		if (SystemFlags.USE_ZOBRIST) {
			ZobristInfo previouslyProcessedPosition = zobristMap.get(posn.getZobristHash());
			if (previouslyProcessedPosition != null) {
				return Optional.of(previouslyProcessedPosition);
			} else {
				return Optional.empty();
			}
		} else {
			return Optional.empty();
		}
	}

	public void updateZobristMap(Position posn,
			int depth,
			SearchResult searchResult) {
		if (SystemFlags.USE_ZOBRIST) {
			ZobristInfo previouslyProcessedPosition = zobristMap.get(posn.getZobristHash());
			if ((previouslyProcessedPosition == null) || (previouslyProcessedPosition.depth < depth)) {
				// store (or re-store with 'better' evaluation)
				zobristMap.put(posn.getZobristHash(), new ZobristInfo(depth, searchResult));
			}
		}
	}

	public static class ZobristInfo {
		private SearchResult searchResult;
		private int depth;

		public ZobristInfo(int depth, SearchResult searchResult) {
			this.depth = depth;
			this.searchResult = searchResult;
		}

		public SearchResult getSearchResult() {
			return searchResult;
		}

		public int getDepth() {
			return depth;
		}
	}
}
