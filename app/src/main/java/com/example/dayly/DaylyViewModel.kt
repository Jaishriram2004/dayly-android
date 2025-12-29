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

    val progress: StateFlow<Float> =
        MutableStateFlow(0f)

    init {
        loadActivities()
    }

    private fun loadActivities() {
        viewModelScope.launch {
            val saved = DaylyDataStore.loadActivities(context)
            _activities.value = if (saved.isNotEmpty()) {
                saved
            } else {
                defaultActivities()
            }
            updateProgress()
        }
    }
    fun addActivity(item: ActivityItem) {
        _activities.value = _activities.value + item
        updateProgress()

        viewModelScope.launch {
            DaylyDataStore.saveActivities(context, _activities.value)
        }
    }

    fun toggleActivity(index: Int, checked: Boolean) {
        _activities.value = _activities.value.toMutableList().also {
            it[index] = it[index].copy(completed = checked)
        }
        updateProgress()

        viewModelScope.launch {
            DaylyDataStore.saveActivities(context, _activities.value)
        }
    }

    private fun updateProgress() {
        val completed = _activities.value.count { it.completed }
        val total = _activities.value.size
        (progress as MutableStateFlow).value =
            if (total > 0) completed.toFloat() / total else 0f
    }

    private fun defaultActivities() = listOf(
        ActivityItem("06:00 – 07:00", "Morning Gym", false),
        ActivityItem("09:30 – 10:00", "Team Standup", true),
        ActivityItem("14:00 – 15:00", "Project Work", false),
        ActivityItem("21:00 – 21:30", "Reading", false)
    )
}
