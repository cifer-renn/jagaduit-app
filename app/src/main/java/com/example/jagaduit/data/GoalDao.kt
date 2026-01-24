package com.example.jagaduit.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY id DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Int): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    // --- TAMBAHAN BARU UNTUK HISTORY ---

    // 1. Simpan catatan history
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: GoalHistoryEntity)

    // 2. Ambil history berdasarkan ID Goal (Diurutkan dari yang terbaru)
    @Query("SELECT * FROM goal_history WHERE goalId = :goalId ORDER BY timestamp DESC")
    fun getHistoryByGoalId(goalId: Int): Flow<List<GoalHistoryEntity>>
}