package com.example.final_project.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Message
import com.example.final_project.domain.model.UserProfile
import com.example.final_project.domain.repository.ChatRepository
import com.example.final_project.domain.repository.SocialRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatConversationUiState(
    val otherUser: UserProfile? = null,
    val currentUserId: String = "",
    val inputText: String = "",
    val isSending: Boolean = false,
    val isOtherUserTyping: Boolean = false,
)

class ChatConversationViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val socialRepository: SocialRepository,
    private val supabaseClient: SupabaseClient,
) : ViewModel() {

    private val otherUserId: String = checkNotNull(savedStateHandle["userId"])

    private val _uiState = MutableStateFlow(ChatConversationUiState())
    val uiState: StateFlow<ChatConversationUiState> = _uiState.asStateFlow()

    val messages: Flow<PagingData<Message>> =
        chatRepository.getConversationPaged(otherUserId).cachedIn(viewModelScope)

    init {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: ""
        _uiState.update { it.copy(currentUserId = currentUserId) }

        // Suppress notifications for the chat we're currently viewing.
        com.example.final_project.core.notification.ActiveChatHolder.setActive(otherUserId)

        loadProfile()

        viewModelScope.launch {
            chatRepository.markAsRead(otherUserId)
        }

        viewModelScope.launch {
            chatRepository.observeTypingStatus(otherUserId).collect { isTyping ->
                _uiState.update { it.copy(isOtherUserTyping = isTyping) }
            }
        }

        // Refresh the other user's profile periodically so their online / last-seen
        // status stays current while the conversation is open.
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(30_000)
                loadProfile()
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            when (val result = socialRepository.getUserProfile(otherUserId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(otherUser = result.data) }
                }
                is Resource.Error -> Unit
                is Resource.Loading -> Unit
            }
        }
    }

    fun onDeleteMessage(message: Message) {
        viewModelScope.launch { chatRepository.deleteMessage(message.id) }
    }

    fun onInputTextChanged(text: String) {
        val wasEmpty = _uiState.value.inputText.isEmpty()
        val isEmpty = text.isEmpty()
        _uiState.update { it.copy(inputText = text) }

        if (wasEmpty != isEmpty) {
            viewModelScope.launch {
                chatRepository.setTypingStatus(receiverId = otherUserId, isTyping = !isEmpty)
            }
        }
    }

    fun onSendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return

        // Clear the box immediately; sendMessage inserts an optimistic local row
        // so the bubble shows instantly. Network work runs off the critical path
        // (no isSending spinner, no blocking on the typing-status broadcast).
        _uiState.update { it.copy(inputText = "") }
        viewModelScope.launch { chatRepository.setTypingStatus(receiverId = otherUserId, isTyping = false) }
        viewModelScope.launch { chatRepository.sendMessage(receiverId = otherUserId, content = text) }
    }

    override fun onCleared() {
        super.onCleared()
        // Only clear if we're still the active chat (avoid clobbering a newer one).
        if (com.example.final_project.core.notification.ActiveChatHolder.activeChatUserId.value == otherUserId) {
            com.example.final_project.core.notification.ActiveChatHolder.setActive(null)
        }
    }
}
