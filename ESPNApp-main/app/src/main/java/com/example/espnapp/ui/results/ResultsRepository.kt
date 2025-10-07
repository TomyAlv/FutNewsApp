package com.example.espnapp.ui.results

import com.example.espnapp.model.espn.ScoreboardResponse
import com.example.espnapp.network.EspnApiService
import com.example.espnapp.network.espnApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResultsRepository(
    private val api: EspnApiService = espnApi
) {
    fun getLeagueScoreboard(
        league: String,
        yyyymmddUtc: String,
        onResult: (Result<ScoreboardResponse?>) -> Unit
    ) {
        api.getLeagueScoreboard(league, yyyymmddUtc)
            .enqueue(object : Callback<ScoreboardResponse> {
                override fun onResponse(
                    call: Call<ScoreboardResponse>,
                    response: Response<ScoreboardResponse>
                ) {
                    if (response.isSuccessful) onResult(Result.success(response.body()))
                    else onResult(Result.failure(IllegalStateException("HTTP ${response.code()}")))
                }

                override fun onFailure(call: Call<ScoreboardResponse>, t: Throwable) {
                    onResult(Result.failure(t))
                }
            })
    }

    ///** Calls several leagues in parallel and merges the results */
    fun getManyLeaguesScoreboards(
        leagues: Map<String, String>, // code -> title
        yyyymmddUtc: String,
        onResult: (Result<Map<String, ScoreboardResponse?>>) -> Unit
    ) {
        if (leagues.isEmpty()) {
            onResult(Result.success(emptyMap()))
            return
        }
        val acc = mutableMapOf<String, ScoreboardResponse?>()
        var pending = leagues.size
        var anyError: Throwable? = null

        leagues.forEach { (code, _) ->
            getLeagueScoreboard(code, yyyymmddUtc) { res ->
                res.onSuccess { body -> acc[code] = body }
                    .onFailure { anyError = it }
                if (--pending == 0) {
                    if (acc.isEmpty() && anyError != null) onResult(Result.failure(anyError!!))
                    else onResult(Result.success(acc))
                }
            }
        }
    }
}
