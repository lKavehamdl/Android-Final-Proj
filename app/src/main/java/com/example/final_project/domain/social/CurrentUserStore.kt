package com.example.final_project.domain.social

import com.example.final_project.core.result.Resource
import com.example.final_project.domain.model.UserProfile
import com.example.final_project.domain.repository.SocialRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Single source of truth for the signed-in user's own profile (avatar, username, …).
 * Lets [com.example.final_project.ui.components.MelodifyTopBar] and chat screens show
 * the same avatar as the Profile tab without each needing their own network call.
 */
class CurrentUserStore(
    private val socialRepository: SocialRepository,
    private val supabase: SupabaseClient,
    private val appScope: CoroutineScope,
) {
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    init {
        supabase.auth.sessionStatus
            .map { it is SessionStatus.Authenticated }
            .distinctUntilChanged()
            .onEach { authenticated ->
                if (authenticated) refresh() else _profile.value = null
            }
            .launchIn(appScope)
    }

    suspend fun refresh() {
        val userId = socialRepository.getCurrentUserId() ?: return
        val result = socialRepository.getUserProfile(userId)
        if (result is Resource.Success) {
            _profile.value = result.data
        }
    }

    fun set(profile: UserProfile) {
        _profile.value = profile
    }

    fun refreshAsync() {
        appScope.launch { refresh() }
    }
}
