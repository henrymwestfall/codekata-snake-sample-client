package app

import kotlin.math.absoluteValue


const val bigNumber = 100000
var lastWalls = setOf<Pair<Int, Int>>()
var currentGraph = mutableMapOf<Pair<Int, Int>, MutableSet<Pair<Int, Int>>>()

fun manhattanDistance(a: Pair<Int, Int>, b: Pair<Int, Int>): Int {
    /** A function to find the manhattan distance between two points a and b **/

    val dx = (a.first - b.first).absoluteValue
    val dy = (a.second - b.second).absoluteValue
    return dx + dy
}

fun getCloseWalls(ref: Pair<Int, Int>, walls: Map<Pair<Int, Int>, Int>): Set<Pair<Int, Int>> {
    /** A function to get a set of walls that are close enough to be relevant **/

    val closeWalls = walls.filter { (wallPos, timeLeft) ->
        manhattanDistance(ref, wallPos) <= timeLeft
    }
    return closeWalls.keys
}

fun getGridPoints(gridSize: Int): List<Pair<Int, Int>> {
    val points = mutableListOf<Pair<Int, Int>>()
    for (x in 0 until gridSize) {
        for (y in 0 until gridSize) {
            points.add(Pair(x, y))
        }
    }
    return points.toList()
}

fun createGraph(points: List<Pair<Int, Int>>, walls: Set<Pair<Int, Int>>):
        MutableMap<Pair<Int, Int>, MutableSet<Pair<Int, Int>>> {
    val graph = mutableMapOf<Pair<Int, Int>, MutableSet<Pair<Int, Int>>>()
    for (parent in points) {
        graph[parent] = mutableSetOf()
        for (child in points) {
            if (child in walls) continue

            if (manhattanDistance(parent, child) == 1) {
                graph[parent]?.add(child)
            }
        }
    }
    return graph
}

fun dijkstra(start: Pair<Int, Int>,
                 end: Pair<Int, Int>,
                 walls: Map<Pair<Int, Int>, Int>,
                 gridSize: Int = 25
): List<Pair<Int, Int>>? {
    /** Implements dijkstraAlgorithm.
     * walls: maps position to time until it disappears **/

    // first, filter out walls that are too far away
    val closeWalls = getCloseWalls(start, walls)

    // create a graph
    val allPoints = getGridPoints(gridSize)
    val graph = createGraph(allPoints, closeWalls)

    return dijkstraAlgorithm(start, end, graph)
}

fun dijkstra(start: Pair<Int, Int>,
             end: Pair<Int, Int>,
             walls: Set<Pair<Int, Int>>,
             gridSize: Int = 25
): List<Pair<Int, Int>>? {
    /** Implements dijkstraAlgorithm.
     * walls: close walls **/

    // create a graph
    val allPoints = getGridPoints(gridSize)
    if (walls != lastWalls) {
        currentGraph = createGraph(allPoints, walls)
        lastWalls = walls.toSet()
    }

    return dijkstraAlgorithm(start, end, currentGraph)
}



fun getDirectionTo(src: Pair<Int, Int>, target: Pair<Int, Int>): Int {
    val diff = Pair(target.first - src.first, target.second - src.second)
    when (diff.first) {
        1 -> return 2 // right
        -1 -> return 0 // left
    }

    when (diff.second) {
        1 -> return 3 // down
        -1 -> return 1 // up
    }

    println("Error: had to choose left by default.")
    return 0
}

fun dijkstraAlgorithm(src: Pair<Int, Int>,
             target: Pair<Int, Int>,
             graph: MutableMap<Pair<Int, Int>, MutableSet<Pair<Int, Int>>>
): List<Pair<Int, Int>>? {
    /** Return the distance and next move of the shortest path from src
     * to target on graph **/

    val vertices = graph.keys
    val dist = mutableMapOf<Pair<Int, Int>, Int>()
    val tree = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>?>()
    val visitedSet = mutableSetOf<Pair<Int, Int>>()
    for (v in vertices) {
        dist[v] = bigNumber
        tree[v] = null
    }
    dist[src] = 0

    var current = src

    var pathLength = bigNumber

    for (iteration in 0 until vertices.size) {
        val unvisitedNeighbors = graph[current]!!.filter { it !in visitedSet }

        for (un in unvisitedNeighbors) {
            val tentativeDist = dist[current]!! + 1
            if (tentativeDist < dist[un]!!) {
                dist[un] = tentativeDist
                tree[un] = current
            }
        }

        visitedSet.add(current)

        var minDist = bigNumber
        var minH = bigNumber
        for (v in vertices) {
            val h = manhattanDistance(v, target) // heuristic
            if ((dist[v]!! + h < minDist + minH) && (v !in visitedSet)) {
                minDist = dist[v]!!
                minH = h
                current = v
            }
        }

        if (minDist == bigNumber || current == target) {
            pathLength = minDist
            break
        }
    }

    // make sequence if one exists
    val shortestSequence = mutableListOf<Pair<Int, Int>>()
    if (pathLength == bigNumber) {
        return null
    }
    else {
        var prev = target
        while (prev != src) {
            shortestSequence.add(prev)
            prev = tree[prev]!!
        }
    }

    return shortestSequence.reversed().toList()
}
fun getPossibleFoodSpawns(board: Array<Array<Int>>,
                          livingHeads: List<Pair<Int, Int>>,
                          boardSize: Int = 25): List<Pair<Int, Int>> {

    val validLocs = mutableListOf<Pair<Int, Int>>()
    for (x in 0 until boardSize) {
        for (y in 0 until boardSize) {
            val occupied = board[x][y] != -1
            if (occupied) continue

            val dists = livingHeads.map {
                manhattanDistance(it, Pair(x, y))
            }

            if ((dists.min() ?: bigNumber) < 10) continue

            validLocs.add(Pair(x, y))
        }
    }

    return validLocs.toList()
}

fun getClosestTo(walls: Set<Pair<Int, Int>>, points: List<Pair<Int, Int>>, target: Pair<Int, Int>): Pair<Int, Int> {
    var closestIndex = 0
    var closestDistance = bigNumber
    points.forEachIndexed { index, pt ->
        val path = dijkstra(pt, target, walls)
        if (path != null) {
            if (path.size < closestDistance) {
                closestDistance = path.size
                closestIndex = index
            }
        }
    }

    return Pair(closestIndex, closestDistance)
}