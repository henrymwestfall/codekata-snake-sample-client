package app

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson

// manages api interactions

// json structs expected to be returned
// response to all post requests. error will be non null if failure
data class PostResponse(val error: String?)

data class BoardResponse(val board: Array<Array<Int>>, val food: Array<Int>, val heads: Array<Array<Int>>)

class API(val url: String, val key: String) {
    val gson = Gson()

    // GET /api/move_needed
    fun getMoveNeeded(): Boolean {
        val (_, response, _) = "${url}/api/move_needed".httpGet(
                listOf(Pair("key", key))
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), Boolean::class.java)
    }

    // POST /api/move
    fun doMove(move: Int): PostResponse {
        val (_, response, _) = "${url}/api/move".httpPost(
                listOf(Pair("key", key), Pair("move", move))
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), PostResponse::class.java)
    }

    // GET /api/board
    fun getBoard(): BoardResponse {
        val (_, response, _) = "${url}/api/board".httpGet(
                listOf(Pair("key", key))
        ).responseString()

        return gson.fromJson(response.body().asString("application/json"), BoardResponse::class.java)
    }
}