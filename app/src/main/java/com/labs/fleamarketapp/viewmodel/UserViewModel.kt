package com.labs.fleamarketapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labs.fleamarketapp.api.ApiClient
import com.labs.fleamarketapp.api.models.LoginRequest
import com.labs.fleamarketapp.data.UiState
import com.labs.fleamarketapp.data.User
import com.labs.fleamarketapp.data.UserType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale

class UserViewModel : ViewModel() {
    
    private val api = ApiClient.api
    
    private val _loginState = MutableLiveData<UiState<User>>()
    val loginState: LiveData<UiState<User>> = _loginState
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()
    
    private val _authToken = MutableStateFlow<String?>(null)
    val authToken: StateFlow<String?> = _authToken.asStateFlow()
    
    private val _profileState = MutableLiveData<UiState<User>>()
    val profileState: LiveData<UiState<User>> = _profileState
    
    private val _signupState = MutableLiveData<UiState<User>>()
    val signupState: LiveData<UiState<User>> = _signupState
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            try {
                val response = api.login(LoginRequest(email = email, password = password))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val token = body.data.token
                        val serverUser = body.data.user
                        val user = User(
                            id = serverUser.id,
                            email = serverUser.email,
                            name = listOf(serverUser.firstName, serverUser.lastName)
                                .joinToString(" ").trim(),
                            phone = serverUser.phone,
                            rating = (serverUser.rating ?: 0.0).toFloat(),
                            userType = when (serverUser.role.uppercase(Locale.US)) {
                                "SELLER" -> UserType.SELLER
                                else -> UserType.BUYER
                            },
                            authToken = token
                        )
                        _authToken.value = token
                        _currentUser.value = user
                        _loginState.value = UiState.Success(user)
                    } else {
                        val message = body?.message ?: "Login failed"
                        _loginState.value = UiState.Error(message)
                    }
                } else {
                    val errorMessage = extractErrorMessage(response.errorBody()?.string()) ?: "Login failed"
                    _loginState.value = UiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.message ?: "Login failed")
            }
        }
    }
    
    fun logout() {
        _currentUser.value = null
        _authToken.value = null
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
        _authToken.value = user.authToken
    }
    
    fun signup(email: String, password: String, name: String, userType: UserType, phone: String? = null) {
        viewModelScope.launch {
            _signupState.value = UiState.Loading
            try {
                val (firstName, lastName) = splitName(name)
                val payload = mutableMapOf(
                    "email" to email,
                    "password" to password,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "role" to userType.name
                )
                phone?.let { payload["phone"] = it }
                
                val response = api.register(payload)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        val token = body.data.token
                        val serverUser = body.data.user
                        val user = User(
                            id = serverUser.id,
                            email = serverUser.email,
                            name = listOf(serverUser.firstName, serverUser.lastName)
                                .joinToString(" ").trim(),
                            phone = serverUser.phone,
                            rating = (serverUser.rating ?: 0.0).toFloat(),
                            userType = when (serverUser.role.uppercase(Locale.US)) {
                                "SELLER" -> UserType.SELLER
                                else -> UserType.BUYER
                            },
                            authToken = token
                        )
                        _authToken.value = token
                        _currentUser.value = user
                        _signupState.value = UiState.Success(user)
                    } else {
                        val message = body?.message ?: "Signup failed"
                        _signupState.value = UiState.Error(message)
                    }
                } else {
                    val errorMessage = extractErrorMessage(response.errorBody()?.string()) ?: "Signup failed"
                    _signupState.value = UiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _signupState.value = UiState.Error(e.message ?: "Signup failed")
            }
        }
    }
    
    private fun splitName(fullName: String): Pair<String, String> {
        val parts = fullName.trim().split("\\s+".toRegex(), limit = 2)
        val first = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: "User"
        val last = parts.getOrNull(1)?.takeIf { it.isNotBlank() } ?: first
        return first to last
    }
    
    private fun extractErrorMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            val json = JSONObject(raw)
            when {
                json.optJSONArray("message") != null -> {
                    val array = json.optJSONArray("message")
                    if (array != null && array.length() > 0) {
                        val messages = mutableListOf<String>()
                        for (i in 0 until array.length()) {
                            val entry = array.opt(i)
                            if (entry != null) {
                                messages.add(entry.toString())
                            }
                        }
                        messages.joinToString(", ")
                    } else {
                        null
                    }
                }
                json.optString("message").isNotBlank() -> json.optString("message")
                json.optString("error").isNotBlank() -> json.optString("error")
                else -> raw
            }
        } catch (_: Exception) {
            raw
        }
    }
}

