package com.example.final_project.core.notification

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Tracks which chat conversation (other user's id) is currently open on screen,
 * so incoming-message notifications can be suppressed for the chat the user is
 * already looking at. Set by ChatConversationViewModel; read by ChatRepository.
 */
object ActiveChatHolder {
    val activeChatUserId = MutableStateFlow<String?>(null)

    fun setActive(userId: String?) {
        activeChatUserId.value = userId
    }
}
