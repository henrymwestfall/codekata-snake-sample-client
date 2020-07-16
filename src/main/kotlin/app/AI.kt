package app

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sign
import kotlin.time.toDuration

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
    fun doMove(board: Array<Array<Int>>, food: Pair<Int, Int>, heads: List<Pair<Int, Int>>): Int {
        // location of our head
        val head = heads[0]
        // location of enemy heads
        val enemyHeads = heads.filterIndexed { i, _ -> i != 0}

        /* Basic AI (REPLACE THIS)
        don't go off the board, don't run into other snakes. otherwise, move randomly */
        val moves = listOf(0, 1, 2, 3).filter { move ->
            val newHead = applyMove(head, move)

            posOnBoard(newHead) && cellFree(newHead, board)
        }

        if(moves.size == 0) return 0
        else return moves.random()
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
