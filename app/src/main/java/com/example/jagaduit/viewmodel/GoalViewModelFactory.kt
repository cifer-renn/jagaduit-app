package com.example.jagaduit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.jagaduit.data.GoalDao

class GoalViewModelFactory(private val dao: GoalDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GoalViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}