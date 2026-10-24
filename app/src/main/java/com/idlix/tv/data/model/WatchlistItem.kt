package com.idlix.tv.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WatchlistItem(
    val slug: String,
    val contentType: String,
    val title: String,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val addedTime: Long = System.currentTimeMillis()
)
