package com.example.espnapp.network

import com.example.espnapp.model.espn.NewsResponse
import com.example.espnapp.model.espn.ScoreboardResponse
import com.example.espnapp.model.espn.Team
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface EspnApiService {
    // News by league
    @GET("apis/site/v2/sports/soccer/{league}/news")
    fun getLeagueNews(@Path("league") league: String): Call<NewsResponse>



    // Scoreboard by league and date yyyymmdd
    @GET("apis/site/v2/sports/soccer/{league}/scoreboard")
    fun getLeagueScoreboard(
        @Path("league") league: String,
        @Query("dates") yyyymmdd: String
    ): Call<ScoreboardResponse>

    // Teams by league (used for search)
    @GET("apis/site/v2/sports/soccer/{league}/teams")
    fun getLeagueTeams(@Path("league") league: String): Call<TeamsResponse>

    // Dynamic news endpoint (if required elsewhere)
    @GET
    fun getNewsDynamic(@Url relativeUrl: String): Call<NewsResponse>
}

// Minimal response tree for /teams endpoint.
// We reuse your existing model Team (from Scoreboard models) at the leaf.
data class TeamsResponse(val sports: List<SportX>?)
data class SportX(val leagues: List<LeagueX>?)
data class LeagueX(val teams: List<TeamX>?)
data class TeamX(val team: Team?)

// Singleton
val espnApi: EspnApiService by lazy {
    EspnRetrofit.retrofit.create(EspnApiService::class.java)
}
