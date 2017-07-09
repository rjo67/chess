# chess #

chess computer programming

## General overview

A **Position** stores information about a particular chess position and is immutable.
A **MovePosition** augments this information with the details of the move to get to this position.
And finally a **Game** object stores a list of MovePosition objects.

## TODO ##

- [x] Cleanup KingChecker, KingCheck, ... ?! Position:isKingInCheck should take parameter isKingInCheck and call the
appropriate KingCheck method.
- [ ] implement interface for BitSet, and 'normal' / immutable versions
- [x] change various methods to return immutable version
- [ ] iterative deepening
- [ ] thread for search, interrupt from uci controller


Branch 'moveswithcheck': rewrites Piece::findMoves to return all possible moves (including non-legal).
This list is then refined in Position::findMoves to remove all moves which leave my king in check.
The hope is to be able to optimize this procedure if it is performed centrally.
Flag: Position::GENERATE_ILLEGAL_MOVES to switch this behaviour on (default: off)


[Diary](diary.md)



