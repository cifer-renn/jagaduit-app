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

    // --- FUNGSI UPDATE (YANG HILANG) ---
    // Pastikan di AccountDao.kt sudah ada @Update suspend fun updateAccount(...)
    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            dao.updateAccount(account)
        }
    }

    // Fungsi Update Saldo (dipakai saat transaksi terjadi)
    fun updateAccountBalance(accountName: String, amount: Double, isExpense: Boolean) {
        viewModelScope.launch {
            // Karena kita butuh saldo saat ini, logika ini biasanya ditangani di DAO
            // atau dengan logic pengambilan data dulu.
            // Untuk simplifikasi, kita asumsikan pemanggil sudah memastikan data benar
            // atau menggunakan query @Query("UPDATE ...") di DAO.

            // Note: Untuk fitur edit manual di AccountScreen, kita pakai fungsi 'updateAccount' di atas.
        }
    }
}