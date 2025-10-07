package com.example.espnapp.ui.news

import com.example.espnapp.model.espn.Article
import com.example.espnapp.model.espn.NewsResponse
import com.example.espnapp.network.EspnApiService
import com.example.espnapp.network.espnApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewsRepository(
    private val api: EspnApiService = espnApi
) {
    // Collects news from several leagues in parallel.
    fun getLeaguesNews(
        leagues: List<String>,
        onResult: (Result<List<Article>>) -> Unit
    ) {
        if (leagues.isEmpty()) {
            onResult(Result.success(emptyList()))
            return
        }
        val acc = mutableListOf<Article>()
        var pending = leagues.size
        var anyError: Throwable? = null

        leagues.forEach { lg ->
            api.getLeagueNews(lg).enqueue(object : Callback<NewsResponse> {
                override fun onResponse(call: Call<NewsResponse>, resp: Response<NewsResponse>) {
                    resp.body()?.articles?.let(acc::addAll)
                    if (--pending == 0) {
                        if (anyError != null && acc.isEmpty()) onResult(Result.failure(anyError!!))
                        else onResult(Result.success(acc))
                    }
                }
                override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                    anyError = t
                    if (--pending == 0) {
                        if (acc.isEmpty()) onResult(Result.failure(t))
                        else onResult(Result.success(acc))
                    }
                }
            })
        }
    }

    // Tests candidate endpoints sequentially until it gets news.
    fun getNewsFromCandidates(
        candidates: List<String>,
        onResult: (Result<List<Article>>) -> Unit
    ) {
        fun tryIndex(i: Int) {
            if (i >= candidates.size) {
                onResult(Result.success(emptyList()))
                return
            }
            api.getNewsDynamic(candidates[i]).enqueue(object : Callback<NewsResponse> {
                override fun onResponse(call: Call<NewsResponse>, resp: Response<NewsResponse>) {
                    if (resp.isSuccessful) {
                        val list = resp.body()?.articles.orEmpty()
                        if (list.isNotEmpty()) onResult(Result.success(list))
                        else tryIndex(i + 1)
                    } else tryIndex(i + 1)
                }
                override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                    tryIndex(i + 1)
                }
            })
        }
        tryIndex(0)
    }
}
