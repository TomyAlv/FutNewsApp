// model/espn/ScoreboardModels.kt
package com.example.espnapp.model.espn

import com.google.gson.annotations.SerializedName

data class ScoreboardResponse(
    @SerializedName("leagues") val leagues: List<League>?,
    @SerializedName("events")  val events:  List<Event>?
)

data class League(
    @SerializedName("name") val name: String?,
    @SerializedName("abbreviation") val abbr: String?,
    @SerializedName("events") val events: List<Event>?
)

data class Event(
    @SerializedName("id") val id: String,
    @SerializedName("date") val date: String?, // ISO
    val name: String?,
    @SerializedName("competitions") val competitions: List<Competition>?
)

data class Competition(
    @SerializedName("status") val status: Status?,
    @SerializedName("competitors") val competitors: List<Competitor>?
)

data class Status(
    @SerializedName("type") val type: StatusType?,
    @SerializedName("displayClock") val displayClock: String?,
    val period: Int?
)
data class StatusType(
    @SerializedName("state") val state: String?, // "pre","in","post"
    @SerializedName("shortDetail") val shortDetail: String? // "16:00", "FT", "45'+2"
)

data class Competitor(
    @SerializedName("homeAway") val homeAway: String?, // "home" / "away"
    @SerializedName("score") val score: String?,
    @SerializedName("team") val team: Team?,
    @SerializedName("form") val form: String? // // sometimes the streak appears (EGGPP)
)

data class Team(
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("shortDisplayName") val shortDisplayName: String?,
    @SerializedName("abbreviation") val abbreviation: String?,
    @SerializedName("logos") val logos: List<Logo>?
)

data class Logo(@SerializedName("href") val href: String?)
