package com.example.jagaduit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val amount: Double,
    val type: String,
    val category: String,
    val accountFrom: String,
    val accountTo: String?,
    val note: String,
    val imagePath: String? = null
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val name: String,
    val balance: Double = 0.0
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val password: String
)