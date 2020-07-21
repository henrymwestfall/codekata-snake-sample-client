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
        val enemyHeads = heads.filterIndexed { i, _ -> i != 0}
        // living snakes
        val livingHeads = heads.filterIndexed { index, head ->
            head != lastHeadPositions[index]
        }

        lastHeadPositions = heads.toTypedArray()

        // add age to cells as necessary
        ageAllCells()
        heads.forEachIndexed { index, head ->
            cellAges[head] = snakeLengths[index]
        }

        // create walls from cellAges and heads
        val walls = cellAges.toMutableMap()
        for (head in enemyHeads) {
            for (x in 0 until 25) {
                for (y in 0 until 25) {
                    if (manhattanDistance(head, Pair(x, y)) == 1) {
                        walls[Pair(x, y)] = 2
                    }
                }
            }
        }

        val shortestPaths = heads.map { dijkstra(it, food, walls) }
        val shortestPath = shortestPaths[0]
        var closestPlayer = 0
        var closestDistance = bigNumber
        shortestPaths.forEachIndexed { index, path ->
            if (path != null) {
                if (path.size < closestDistance && heads[index] in livingHeads) {
                    closestDistance = path.size
                    closestPlayer = index
                }
            }
        }

        val nextFoodPos = getNextFoodPos(board, livingHeads)

        val shortestPathsToNextFood = heads.map { dijkstra(it, nextFoodPos, walls) }
        val shortestPathToNextFood = shortestPaths[0]
        var closestPlayerToNextFood = 0
        var closestDistanceToNextFood = bigNumber
        shortestPathsToNextFood.forEachIndexed { index, path ->
            if (path != null) {
                if (path.size < closestDistanceToNextFood && heads[index] in livingHeads) {
                    closestDistanceToNextFood = path.size
                    closestPlayerToNextFood = index
                }
            }
        }

        val pathBetweenFoods = dijkstra(food, nextFoodPos, walls)
        var midwayPoint: Pair<Int, Int>? = null
        var pathToMidway: List<Pair<Int, Int>>? = null
        if (pathBetweenFoods != null) {
            midwayPoint = pathBetweenFoods[pathBetweenFoods.size / 2]
            pathToMidway = dijkstra(ourHead, midwayPoint, walls)
        }

        // we are closest to existing food
        if (closestPlayer == 0 && shortestPath != null) {
            println("Moving towards current food.")
            return getDirectionTo(ourHead, shortestPath[0])
        }

        // we are closest to next food
        if (closestPlayerToNextFood == 0 && shortestPathToNextFood != null) {
            println("Moving towards next food.")
            return getDirectionTo(ourHead, shortestPathToNextFood[0])
        }

        // we are not closest to either, go to midway
        if (pathToMidway != null) {
            println("Moving towards midway point.")
            return getDirectionTo(ourHead, pathToMidway[0])
        }

        // all else fails, move randomly
        println("Moving randomly.")

        /* Basic AI (REPLACE THIS)
        don't go off the board, don't run into other snakes. otherwise, move randomly */
        val moves = listOf(0, 1, 2, 3).filter { move ->
            val newHead = applyMove(ourHead, move)

            posOnBoard(newHead) && cellFree(newHead, board)
        }

        return if(moves.isEmpty()) 0
        else moves.random()
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
