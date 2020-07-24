package app

// EDIT THIS FILE

class AI() {
    /** EDIT THIS METHOD
     * Given the state of the game, make a move.
     * return the move to be made (0 = left, 1 = up, 2 = right, 3 = down)
     *
     * board is an array indexed [x][y] indicating what each cell contains (-1 = empty, 0 = us, 1 = enemy 1, 2 = enemy2, 3 = enemy3)
     * food is the location of the food on the board
     * heads are the locations of the head of each snake (index [0] = us, [1] = enemy1, [2] = enemy2, [3] = enemy3)
     */
    val cellAges = mutableMapOf<Pair<Int, Int>, Int>()
    val deadCells = mutableSetOf<Pair<Int, Int>>()
    var lastHeadPositions = Array(4) { Pair(12, 12) }

    init {
        // initialize cell ages
        for (x in 0 until 25) {
            for (y in 0 until 25) {
                cellAges[Pair(x, y)] = 0
            }
        }
    }

    fun findAllSnakes(board: Array<Array<Int>>): MutableList<MutableList<Pair<Int, Int>>> {
        val snakes = (0 until 4).map { mutableListOf<Pair<Int, Int>>() }.toMutableList()
        board.forEachIndexed { x, row ->
            row.forEachIndexed { y, cell ->
                if (cell != -1) {
                    snakes[cell].add(Pair(x, y))
                }
            }
        }
        return snakes
    }

    fun ageAllCells() {
        for (cell in cellAges.keys) {
            if (cell in deadCells) {
                cellAges[cell] = 100
                continue
            }

            val currentAge = cellAges[cell]?:0
            cellAges[cell] = maxOf(currentAge - 1, 0)
        }
    }

    fun doMove(board: Array<Array<Int>>, food: Pair<Int, Int>, heads: List<Pair<Int, Int>>): Int {
        // snakes
        val snakes = findAllSnakes(board)
        val ourSnake = snakes[0]
        val snakeLengths = snakes.map { it.size }

        // location of our head
        val ourHead = heads[0]
        // location of enemy heads
        val enemyHeads = heads.filterIndexed { i, _ -> i != 0 }
        // living snakes
        val livingHeads = heads.filterIndexed { index, head ->
            head != lastHeadPositions[index]
        }
        val deadHeads = heads.filter { head ->
            head !in livingHeads
        }

        val deadPlayers = mutableListOf<Int>()
        for (dh in deadHeads) {
            if (board[dh.first][dh.second] == heads.indexOf(dh)) {
                deadPlayers.add(heads.indexOf(dh))
            }
        }

        // update dead cells
        board.forEachIndexed { x, row ->
            row.forEachIndexed { y, cell ->
                if (cell in deadPlayers) deadCells.add(Pair(x, y))
            }
        }

        lastHeadPositions = heads.toTypedArray()

        // add age to cells as necessary
        ageAllCells()
        heads.forEachIndexed { index, head ->
            cellAges[head] = snakeLengths[index]
        }

        val walls = getCloseWalls(ourHead, cellAges).toMutableSet()
        val wallsWithoutCollisions = walls.toMutableSet()
        val deathPlaces = mutableSetOf<Pair<Int, Int>>()

        // create walls from cellAges and heads
        for (head in enemyHeads) {
            for (x in 0 until 25) {
                for (y in 0 until 25) {
                    if (manhattanDistance(head, Pair(x, y)) == 1) {
                        walls.add(Pair(x, y))
                        deathPlaces.add(Pair(x, y))
                    }
                }
            }
        }

        val shortestPaths = heads.map { dijkstra(it, food, wallsWithoutCollisions) }
        val shortestPath = shortestPaths[0]
        var closestPlayer = 1
        var closestDistance = bigNumber
        shortestPaths.forEachIndexed { index, path ->
            if (path != null) {
                if (path.size < closestDistance && heads[index] in livingHeads) {
                    closestDistance = path.size
                    closestPlayer = index
                }
            }
        }

        // we are the closest player, go towards food
        if (closestPlayer == 0 && shortestPath != null) {
            return getDirectionTo(ourHead, shortestPath[0])
        }

        println("Moving towards next food spawns.")

        val moves = listOf(0, 1, 2, 3).filter { move ->
            val newHead = applyMove(ourHead, move)
            posOnBoard(newHead) && cellFree(newHead, board)
        }

        var move = if (moves.isEmpty()) 0 else moves.random()

        // do the move that puts us near the most spawn positions
        var bestPercentage = 0.0
        for (possibleMove in moves) {
            val newHead = applyMove(ourHead, possibleMove)

            if (newHead in deathPlaces || newHead in walls) {
                continue
            }

            val newHeads = mutableListOf(ourHead)
            newHeads.addAll(enemyHeads)
            val possibleFoodSpawns = getPossibleFoodSpawns(board, newHeads)
            var closestCount = 0
            for (pfs in possibleFoodSpawns) {
                val closestHead = getClosestTo(walls, newHeads, pfs)
                if (closestHead == newHead) ++closestCount
            }
            val percentage = closestCount.toDouble() / possibleFoodSpawns.size.toDouble()

            if (percentage > bestPercentage) {
                bestPercentage = percentage
                move = possibleMove
            }
        }

        println(bestPercentage)

        return move
    }

    /*** Helper methods ***/

    /* given a position, apply a move to it (0, 1, 2, or 3) and return the resultant position */
    fun applyMove(position: Pair<Int, Int>, move: Int): Pair<Int, Int> {
        return when(move) {
            0 -> Pair(position.first - 1, position.second)
            1 -> Pair(position.first, position.second - 1)
            2 -> Pair(position.first + 1, position.second)
            3 -> Pair(position.first, position.second + 1)
            else -> {
                assert(false) // move must be 0, 1, 2, or 3
                Pair(0, 0)
            }
        }
    }

    /* check if a position is on the board or not */
    fun posOnBoard(position: Pair<Int, Int>): Boolean {
        return position.first >= 0 && position.first < 25 && position.second >= 0 && position.second < 25
    }

    /* check if a cell on the board is occupied or not */
    fun cellFree(position: Pair<Int, Int>, board: Array<Array<Int>>): Boolean {
        return board[position.first][position.second] == -1
    }
}
