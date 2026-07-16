package com.example.final_project.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project.domain.model.Message
import com.example.final_project.domain.model.UserProfile
import com.example.final_project.domain.repository.ChatRepository
import com.example.final_project.domain.repository.SocialRepository
import com.example.final_project.core.result.Resource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatListUiState(
    val conversations: List<ConversationItem> = emptyList(),
    val isLoading: Boolean = true
)

data class ConversationItem(
    val otherUser: UserProfile,
    val lastMessage: Message
)

class ChatListViewModel(
    private val chatRepository: ChatRepository,
    private val socialRepository: SocialRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch { chatRepository.startListeningForMessages() }
            
            chatRepository.getRecentConversations().collect { recentMessages ->
                val currentUserId = supabaseClient.auth.currentUserOrNull()?.id 
                if (currentUserId == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@collect
                }
                
                // For each recent message, fetch the other user's profile
                val items = mutableListOf<ConversationItem>()
                for (msg in recentMessages) {
                    val otherUserId = if (msg.senderId == currentUserId) msg.receiverId else msg.senderId
                    when (val result = socialRepository.getUserProfile(otherUserId)) {
                        is Resource.Success -> {
                            items.add(ConversationItem(result.data, msg))
                        }
                        is Resource.Error -> {}
                        is Resource.Loading -> {}
                        else -> {}
                    }
                }
                
                _uiState.update { it.copy(conversations = items, isLoading = false) }
            }
        }
    }
}
