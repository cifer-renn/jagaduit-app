package com.example.jagaduit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1. Tabel Transaksi
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

// 2. Tabel Kategori
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String
)

// 3. Tabel Akun
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val name: String,
    val balance: Double = 0.0
)