package com.example.final_project.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.Message
import com.example.final_project.domain.model.UserProfile
import com.example.final_project.domain.repository.ChatRepository
import com.example.final_project.domain.repository.SocialRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatConversationUiState(
    val otherUser: UserProfile? = null,
    val messages: List<Message> = emptyList(),
    val currentUserId: String = "",
    val inputText: String = "",
    val isSending: Boolean = false,
    val isOtherUserTyping: Boolean = false
)

class ChatConversationViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val socialRepository: SocialRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val otherUserId: String = checkNotNull(savedStateHandle["userId"])

    private val _uiState = MutableStateFlow(ChatConversationUiState())
    val uiState: StateFlow<ChatConversationUiState> = _uiState.asStateFlow()

    init {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: ""
        _uiState.update { it.copy(currentUserId = currentUserId) }
        
        loadProfile()
        
        viewModelScope.launch {
            chatRepository.getConversation(otherUserId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
                // Mark as read when viewing
                if (messages.any { it.receiverId == currentUserId && it.status.name != "READ" }) {
                    chatRepository.markAsRead(otherUserId)
                }
            }
        }
        
        viewModelScope.launch {
            chatRepository.observeTypingStatus(otherUserId).collect { isTyping ->
                _uiState.update { it.copy(isOtherUserTyping = isTyping) }
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            when (val result = socialRepository.getUserProfile(otherUserId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(otherUser = result.data) }
                }
                is Resource.Error -> {}
                is Resource.Loading -> {}
            }
        }
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
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, inputText = "") }
            chatRepository.setTypingStatus(receiverId = otherUserId, isTyping = false)
            chatRepository.sendMessage(receiverId = otherUserId, content = text)
            _uiState.update { it.copy(isSending = false) }
        }
    }
}
