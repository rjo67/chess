# chess #

chess computer programming

## General overview

A **Position** stores information about a particular chess position and is immutable.
A **MovePosition** augments this information with the details of the move to get to this position.
And finally a **Game** object stores a list of MovePosition objects.

## TODO ##

- [x] Cleanup KingChecker, KingCheck, ... ?! Position:isKingInCheck should take parameter isKingInCheck and call the
appropriate KingCheck method.
- [x] implement interface for BitSet
- [x] change various methods to return immutable version
- [ ] iterative deepening
- [ ] thread for search, interrupt from uci controller
- [ ] search for moves in parallel
- [ ] move search starting with pieces instead of pawns

Branch 'moveswithcheck': rewrites Piece::findMoves to return all possible moves (including non-legal).
This list is then refined in Position::findMoves to remove all moves which leave my king in check.
The hope is to be able to optimize this procedure if it is performed centrally.
Flag: Position::GENERATE_ILLEGAL_MOVES to switch this behaviour on (default: off)


http://www.open-chess.org/viewtopic.php?f=5&t=2855#p22102
Checking legality of each move in the move generator is indeed needlessly expensive,when not in check. In qperft I don't check any moves for legality. The reason it does not generate illegal non-King moves is not that they are somehow rejected after generation. It is because it first detects pinned pieces, and uses a special move-generation code for those, only allowing moves along the pin ray. This actually saves time, because you generate fewer moves.
For in-check positions it uses a special move generator depending on the type of check: double (King moves only), contact (King moves + capture of the checker) or distant (King moves + checker capture + interposition).


[Diary](diary.md)



