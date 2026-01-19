package com.example.jagaduit.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    // Sesuaikan query ke tabel "accounts"
    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)
}