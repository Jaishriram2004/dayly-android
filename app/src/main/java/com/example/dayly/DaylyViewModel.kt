package com.example.dayly

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DaylyViewModel(
    private val context: Context
) : ViewModel() {

    private val _activities = MutableStateFlow<List<ActivityItem>>(emptyList())
    val activities: StateFlow<List<ActivityItem>> = _activities

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    init {
        loadActivities()
    }

    // --------------------
    // Load + sort
    // --------------------
    private fun loadActivities() {
        viewModelScope.launch {
            val saved = DaylyDataStore.loadActivities(context)

            val initial = if (saved.isNotEmpty()) {
                saved
            } else {
                defaultActivities()
            }

            val sorted = sortByTime(initial)

            _activities.value = sorted
            updateProgress()

            // Save once to ensure order is persisted
            DaylyDataStore.saveActivities(context, sorted)
        }
    }

    // --------------------
    // Add new activity
    // --------------------
    fun addActivity(item: ActivityItem) {
        val updated = sortByTime(_activities.value + item)

        _activities.value = updated
        updateProgress()

        viewModelScope.launch {
            DaylyDataStore.saveActivities(context, updated)
        }
    }

    // --------------------
    // Toggle completion
    // --------------------
    fun toggleActivity(index: Int, checked: Boolean) {
        val updated = _activities.value.toMutableList().also {
            it[index] = it[index].copy(completed = checked)
        }

        _activities.value = updated
        updateProgress()

        viewModelScope.launch {
            DaylyDataStore.saveActivities(context, updated)
        }
    }

    // --------------------
    // Progress calculation
    // --------------------
    private fun updateProgress() {
        val completed = _activities.value.count { it.completed }
        val total = _activities.value.size
        _progress.value = if (total > 0) completed.toFloat() / total else 0f
    }

    // --------------------
    // Sort by start time
    // --------------------
    private fun sortByTime(items: List<ActivityItem>): List<ActivityItem> {
        return items.sortedBy {
            it.startHour * 60 + it.startMinute
        }
    }

    // --------------------
    // Default seed data
    // --------------------
    private fun defaultActivities() = listOf(
        ActivityItem(
            title = "Morning Gym",
            completed = false,
            startHour = 6,
            startMinute = 0,
            endHour = 7,
            endMinute = 0
        ),
        ActivityItem(
            title = "Team Standup",
            completed = true,
            startHour = 9,
            startMinute = 30,
            endHour = 10,
            endMinute = 0
        ),
        ActivityItem(
            title = "Project Work",
            completed = false,
            startHour = 14,
            startMinute = 0,
            endHour = 15,
            endMinute = 0
        ),
        ActivityItem(
            title = "Reading",
            completed = false,
            startHour = 21,
            startMinute = 0,
            endHour = 21,
            endMinute = 30
        )
    )
}
