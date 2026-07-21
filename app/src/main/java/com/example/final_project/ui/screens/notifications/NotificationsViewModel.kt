package com.example.final_project.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project.domain.model.AppNotification
import com.example.final_project.domain.repository.NotificationsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val notificationsRepository: NotificationsRepository,
) : ViewModel() {

    val notifications: StateFlow<List<AppNotification>> =
        notificationsRepository.observeNotifications()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    init {
        // Opening the screen clears the unread badge dot.
        viewModelScope.launch { notificationsRepository.markAllRead() }
    }

    fun onDelete(id: String) {
        viewModelScope.launch { notificationsRepository.deleteNotification(id) }
    }

    fun onClearAll() {
        viewModelScope.launch { notificationsRepository.deleteAll() }
    }
}
