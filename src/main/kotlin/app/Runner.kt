package app

import kotlin.system.exitProcess
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson
import kotlin.concurrent.thread

/**
 * DON'T EDIT THIS FILE (edit AI.kt instead)
 *
 * This file contains the Runner class, which manages the connection with the server
 */

class Runner(val ai: AI, val apiUrl: String, val apiKey: String) {
    val api = API(apiUrl, apiKey)
    var refreshRate: Long = 500

    fun waitForTurn() {
        while(!api.getMoveNeeded()) Thread.sleep(refreshRate, 0)
    }

    fun doMove() {
        val state = api.getBoard()
        api.doMove(
                ai.doMove(
                        state.board,
                        Pair(state.food[0], state.food[1]),
                        state.heads.map { head -> Pair(head[0], head[1])}))
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 2) {
                println("Expected API url as first command line argument and API key as second")
                exitProcess(1)
            }

            for (i in 1 until args.size) {
                thread {
                    val run = Runner(AI(), args[0], args[i])
                    println("Starting. API URL: ${args[0]}, API KEY: ${args[1]}")
                    println("Connecting to server...")
                    while (true) {
                        run.waitForTurn()
                        println("Starting Turn")
                        run.doMove()
                        println("Ending turn")
                    }
                }
            }
        }
    }
}
