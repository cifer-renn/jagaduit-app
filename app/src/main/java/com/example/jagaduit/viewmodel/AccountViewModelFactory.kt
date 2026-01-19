package com.example.jagaduit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.jagaduit.data.AccountDao

class AccountViewModelFactory(private val dao: AccountDao) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            return AccountViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}