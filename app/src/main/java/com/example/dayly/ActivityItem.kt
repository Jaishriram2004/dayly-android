package com.example.dayly

import kotlinx.serialization.Serializable

@Serializable
data class ActivityItem(
    val title: String,
    val completed: Boolean,

    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
)
