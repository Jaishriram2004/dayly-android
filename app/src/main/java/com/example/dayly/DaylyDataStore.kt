package com.example.dayly

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "dayly_prefs")

private val ACTIVITIES_KEY = stringPreferencesKey("activities_json")

object DaylyDataStore {

    suspend fun saveActivities(context: Context, activities: List<ActivityItem>) {
        val json = Json.encodeToString(activities)
        context.dataStore.edit { prefs ->
            prefs[ACTIVITIES_KEY] = json
        }
    }

    suspend fun loadActivities(context: Context): List<ActivityItem> {
        val prefs = context.dataStore.data.first()
        val json = prefs[ACTIVITIES_KEY] ?: return emptyList()
        return Json.decodeFromString(json)
    }
}
