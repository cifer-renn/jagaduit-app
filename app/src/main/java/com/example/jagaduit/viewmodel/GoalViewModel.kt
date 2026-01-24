package com.example.jagaduit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jagaduit.data.GoalDao
import com.example.jagaduit.data.GoalEntity
import com.example.jagaduit.data.GoalHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class GoalViewModel(private val dao: GoalDao) : ViewModel() {

    val allGoals: Flow<List<GoalEntity>> = dao.getAllGoals()

    fun addGoal(name: String, target: Double, deadline: Long, imagePath: String?) {
        viewModelScope.launch {
            dao.insertGoal(
                GoalEntity(name = name, targetAmount = target, deadline = deadline, imagePath = imagePath)
            )
        }
    }

    fun updateGoal(goal: GoalEntity) {
        viewModelScope.launch { dao.updateGoal(goal) }
    }

    // --- FUNGSI INI DIUBAH: Update Saldo + Catat History ---
    fun saveDeposit(goal: GoalEntity, amount: Double) {
        viewModelScope.launch {
            // 1. Update Goal Utama
            val newAmount = goal.currentAmount + amount
            val isAchieved = newAmount >= goal.targetAmount
            dao.updateGoal(goal.copy(currentAmount = newAmount, isAchieved = isAchieved))

            // 2. Catat ke History
            val history = GoalHistoryEntity(
                goalId = goal.id,
                amount = amount,
                timestamp = System.currentTimeMillis() // Waktu sekarang
            )
            dao.insertHistory(history)
        }
    }

    // --- FUNGSI BARU: Ambil Data History ---
    fun getHistory(goalId: Int): Flow<List<GoalHistoryEntity>> {
        return dao.getHistoryByGoalId(goalId)
    }

    suspend fun getGoalById(id: Int): GoalEntity? {
        return dao.getGoalById(id)
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            dao.deleteGoal(goal)
        }
    }
}