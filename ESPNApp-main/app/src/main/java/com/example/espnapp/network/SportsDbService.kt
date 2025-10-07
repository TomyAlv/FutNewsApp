package com.example.espnapp.network

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Model for The Sports DB
data class SportsDbResponse(
    val events: List<SportsDbEvent>?
)

data class SportsDbEvent(
    val idEvent: String,
    val strEvent: String,
    val strHomeTeam: String,
    val strAwayTeam: String,
    val intHomeScore: String?,
    val intAwayScore: String?,
    val strStatus: String,
    val dateEvent: String,
    val strTime: String?,
    val strLeague: String,
    val strSeason: String
)

interface SportsDbService {

    @GET("api/v1/json/3/eventsseason.php")
    fun getEventsBySeason(
        @Query("id") leagueId: String,
        @Query("s") season: String // "2023-2024"
    ): Call<SportsDbResponse>
}

object SportsDbClient {
    private const val BASE_URL = "https://www.thesportsdb.com/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

val sportsDbApi: SportsDbService by lazy {
    SportsDbClient.retrofit.create(SportsDbService::class.java)
}