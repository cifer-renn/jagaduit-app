package com.example.jagaduit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jagaduit.data.JagaDuitDatabase
import com.example.jagaduit.data.UserEntity
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = JagaDuitDatabase.getDatabase(application).appDao()

    fun login(email: String, password: String, onResult: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            val user = dao.loginUser(email, password)
            onResult(user)
        }
    }

    fun register(name: String, email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val existingUser = dao.getUserByEmail(email)
            if (existingUser != null) {
                onResult(false)
            } else {
                val newUser = UserEntity(name = name, email = email, password = password)
                dao.registerUser(newUser)
                onResult(true)
            }
        }
    }
}