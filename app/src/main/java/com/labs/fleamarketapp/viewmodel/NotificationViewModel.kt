package com.labs.fleamarketapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labs.fleamarketapp.data.UiState
import kotlinx.coroutines.launch

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

class NotificationViewModel : ViewModel() {
    
    private val _notificationsState = MutableLiveData<UiState<List<Notification>>>()
    val notificationsState: LiveData<UiState<List<Notification>>> = _notificationsState
    
    private val _unreadCount = MutableLiveData<Int>()
    val unreadCount: LiveData<Int> = _unreadCount
    
    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            _notificationsState.value = UiState.Loading
            try {
                // TODO: Replace with actual API call
                val notifications = emptyList<Notification>()
                _notificationsState.value = UiState.Success(notifications)
                updateUnreadCount(notifications)
            } catch (e: Exception) {
                _notificationsState.value = UiState.Error(e.message ?: "Failed to load notifications")
            }
        }
    }
    
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                // TODO: Replace with actual API call
                val current = _notificationsState.value
                if (current is UiState.Success) {
                    val updated = current.data.map {
                        if (it.id == notificationId) it.copy(isRead = true) else it
                    }
                    _notificationsState.value = UiState.Success(updated)
                    updateUnreadCount(updated)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    private fun updateUnreadCount(notifications: List<Notification>) {
        _unreadCount.value = notifications.count { !it.isRead }
    }
}

