package com.example.dayly

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DaylyViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DaylyViewModel::class.java)) {
            return DaylyViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
