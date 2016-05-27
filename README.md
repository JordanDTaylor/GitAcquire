- move generation
- value of the board - comparing gamestates
    - We'll calculate your net value

## Tile Effects

- Merge chains
- Grow chain
- Create chain
- Lone tile

## Phases

Transitions between phases is based on how many tiles have been placed.

----------------------------------------------------------------------------------------------------
Early-Game
----------------------------------------------------------------------------------------------------

#### General Strategy

- Create new chains, favored towards the center
- Diversify stocks

#### Tile Placement

- Be on the winning end of an early merger

#### Merging

- Do not trade

#### Stock Purchasing

- Diversify stocks while its cheap

#### Switching to the Mid-Game

Either:
- More than 3 chains have been created AND more than 20 tiles have been placed.
- A chain is safe.

----------------------------------------------------------------------------------------------------
Mid-Game
----------------------------------------------------------------------------------------------------

#### General Strategy

- Prepare for end-game by owning majority stocks in the upcoming large chains.

#### Tile Placement

- Calculate tile value
  - How close it is to the center
  - See effects?

#### Merging

- Consider trading up if it gets you towards the majority

#### Stock Purchasing

- Favor purchasing cheap stocks that are about to be expensive stocks
- Favor being the majority stock holder

#### Switching to the End-Game

- There are more (or equal amount of) safe chains than non-safe chains.

----------------------------------------------------------------------------------------------------
End-Game
----------------------------------------------------------------------------------------------------

- Now we can generate all possible moves.

#### General Strategy

- 1-2 large chains: defend your position as a majority stock holder in large chains.
- 2+ large chains: acquire enough stock in as many as you can to secure the second place bonus.
- If behind, prevent ending the game by creating new chains.

#### Tile Placement

- If behind: create new chains
- If ahead: secure position by growing your large chains

#### Merging

- Directly compare the merge choices and choose the option that gives you the most net value

#### Stock Purchasing

- 1-2 large chains: defend your position as a majority stock holder in large chains.
- 2+ large chains: acquire enough stock in as many as you can to secure the second place bonus.

#### Ending the Game

- The game should end at the end of your turn if the game can end, and the player is winning.
- Consider ending the game if a higher rank is unreachable (e.g. you are in second place, cannot
  reach first place, and do not want to drop into third place, so you end the game).

----------------------------------------------------------------------------------------------------
