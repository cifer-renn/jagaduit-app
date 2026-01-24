package com.example.jagaduit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: Long,
    val imagePath: String? = null,
    val isAchieved: Boolean = false,
    val isFavorite: Boolean = false
)