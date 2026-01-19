package com.example.jagaduit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jagaduit.data.AccountEntity
import com.example.jagaduit.data.CategoryEntity
import com.example.jagaduit.data.JagaDuitDatabase
import com.example.jagaduit.data.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = JagaDuitDatabase.getDatabase(application).appDao()

    val transactionList: Flow<List<TransactionEntity>> = dao.getAllTransactions()

    // Data untuk dropdown (Real-time update dari db)
    val allAccounts: Flow<List<AccountEntity>> = dao.getAllAccounts()

    // Fungsi untuk mengambil kategori berdasarkan tipe
    fun getCategories(type: String): Flow<List<CategoryEntity>> {
        return dao.getCategoriesByType(type)
    }

    // Fungsi Delete
    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            dao.deleteTransaction(transaction)
        }
    }

    // Fungsi Ambil Data by ID (biar bisa di edit)
    suspend fun getTransactionById(id: Int): TransactionEntity? {
        return dao.getTransactionById(id)
    }

    // Update Save Function
    fun saveTransaction(
        id: Int = 0,
        date: Long,
        amount: Double,
        type: String,
        category: String,
        accountFrom: String,
        accountTo: String?,
        note: String
    ) {
        viewModelScope.launch {
            val transaction = TransactionEntity(
                id = id, // Jika 0 = Insert Baru, Jika >0 = Update
                date = date,
                amount = amount,
                type = type,
                category = category,
                accountFrom = accountFrom,
                accountTo = accountTo,
                note = note
            )
            dao.insertTransaction(transaction)
        }
    }

    fun addCategory(name: String, type: String) {
        viewModelScope.launch {
            dao.insertCategory(CategoryEntity(name = name, type = type))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            dao.deleteCategory(category)
        }
    }
}