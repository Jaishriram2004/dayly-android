package com.example.dayly

import kotlinx.serialization.Serializable

@Serializable
data class ActivityItem(
    val time: String,
    val title: String,
    val completed: Boolean
)
