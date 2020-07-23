# Overview

If we are the closest to the food, move
towards it. Otherwise, maximize the number
of possible spawn tiles that are closest to
us.

# Maximizing Spawn Tiles
For each of the 3 possible directions, check
what percentage of possible spawn locations
we are the closest to (A* path length, not 
Manhattan Distance). Choose the move that maximizes this.