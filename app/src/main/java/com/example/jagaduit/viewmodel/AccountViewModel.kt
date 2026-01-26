package com.example.jagaduit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jagaduit.data.AccountDao
import com.example.jagaduit.data.AccountEntity
import kotlinx.coroutines.launch

class AccountViewModel(private val dao: AccountDao) : ViewModel() {

    // Mengambil semua data akun secara realtime (Flow)
    val allAccounts = dao.getAllAccounts()

    // Fungsi Tambah Akun
    fun addAccount(name: String, balance: Double) {
        viewModelScope.launch {
            dao.insertAccount(AccountEntity(name = name, balance = balance))
        }
    }

    // Fungsi Hapus Akun
    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            dao.deleteAccount(account)
        }
    }

    // --- FUNGSI UPDATE ---
    // Pastikan di AccountDao.kt sudah ada @Update suspend fun updateAccount(...)
    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            dao.updateAccount(account)
        }
    }
}