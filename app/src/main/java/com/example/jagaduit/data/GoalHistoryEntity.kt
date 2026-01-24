package com.example.jagaduit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal_history")
data class GoalHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,       // Kunci tamu: Tabungan ini milik Goal yang mana?
    val amount: Double,    // Jumlah uang masuk
    val timestamp: Long    // Waktu (Tanggal & Jam)
)