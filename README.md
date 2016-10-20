# chess #

chess computer programming

## General overview

A **Position** stores information about a particular chess position and is immutable.
A **MovePosition** augments this information with the details of the move to get to this position.
And finally a **Game** object stores a list of MovePosition objects.

## TODO ##

- [ ] Cleanup KingChecker, KingCheck, ... ?! Position:isKingInCheck should take parameter isKingInCheck and call the
appropriate KingCheck method.
- [ ] implement interface for BitSet, and 'normal' / immutable versions
- [x] change various methods to return immutable version
- [ ] iterative deepening
- [ ] thread for search, interrupt from uci controller


[Diary](diary.md)



