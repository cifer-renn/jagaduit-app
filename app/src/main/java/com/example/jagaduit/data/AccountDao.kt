package com.example.jagaduit.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccount(account: AccountEntity)

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>
    // pake nama bukan id
    @Query("SELECT * FROM accounts WHERE name = :accountName")
    suspend fun getAccountByName(accountName: String): AccountEntity?

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("SELECT SUM(balance) FROM accounts")
    fun getTotalBalance(): Flow<Double?>
}