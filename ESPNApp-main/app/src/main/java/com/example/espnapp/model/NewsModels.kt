// model/espn/NewsModels.kt
package com.example.espnapp.model.espn

import com.google.gson.annotations.SerializedName

data class NewsResponse(
    @SerializedName("header") val header: String?,
    @SerializedName("articles") val articles: List<Article>?
)

data class Article(
    @SerializedName("headline") val headline: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("published") val published: String?, // ISO: 2025-10-03T08:31Z
    @SerializedName("links") val links: Links?,
    @SerializedName("images") val images: List<Image>?
)

data class Links(
    @SerializedName("web") val web: Href?,
    @SerializedName("mobile") val mobile: Href?
)

data class Href(
    @SerializedName("href") val href: String?
)

data class Image(
    @SerializedName("url") val url: String?,
    @SerializedName("width") val width: Int?,
    @SerializedName("height") val height: Int?
)
