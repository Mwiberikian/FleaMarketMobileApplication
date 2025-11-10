package com.labs.fleamarketapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labs.fleamarketapp.data.UiState
import com.labs.fleamarketapp.data.User
import com.labs.fleamarketapp.data.UserType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    
    private val _loginState = MutableLiveData<UiState<User>>()
    val loginState: LiveData<UiState<User>> = _loginState
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()
    
    private val _profileState = MutableLiveData<UiState<User>>()
    val profileState: LiveData<UiState<User>> = _profileState
    
    private val _signupState = MutableLiveData<UiState<User>>()
    val signupState: LiveData<UiState<User>> = _signupState
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            try {
                // TODO: Replace with actual API call
                // For now, simulate successful login
                val user = User(
                    id = "1",
                    email = email,
                    name = email.substringBefore("@")
                )
                _currentUser.value = user
                _loginState.value = UiState.Success(user)
            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.message ?: "Login failed")
            }
        }
    }
    
    fun logout() {
        _currentUser.value = null
    }
    
    fun updateProfile(user: User) {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            try {
                // TODO: Replace with actual API call
                _currentUser.value = user
                _profileState.value = UiState.Success(user)
            } catch (e: Exception) {
                _profileState.value = UiState.Error(e.message ?: "Update failed")
            }
        }
    }
    
    fun isValidStrathmoreEmail(email: String): Boolean {
        return email.endsWith("@strathmore.edu", ignoreCase = true)
    }
    
    fun setUser(user: User) {
        _currentUser.value = user
    }
    
    fun signup(email: String, password: String, name: String, userType: UserType, phone: String? = null) {
        viewModelScope.launch {
            _signupState.value = UiState.Loading
            try {
                // TODO: Replace with actual API call
                // For now, simulate successful signup
                val user = User(
                    id = System.currentTimeMillis().toString(),
                    email = email,
                    name = name,
                    phone = phone,
                    userType = userType
                )
                _currentUser.value = user
                _signupState.value = UiState.Success(user)
            } catch (e: Exception) {
                _signupState.value = UiState.Error(e.message ?: "Signup failed")
            }
        }
    }
}

