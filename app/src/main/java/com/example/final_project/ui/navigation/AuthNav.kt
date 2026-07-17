package com.example.final_project.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.final_project.ui.screens.auth.AuthScreen
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import org.koin.compose.koinInject

@Composable
fun AuthNav(
    onAuthenticated: () -> Unit,
) {
    val supabaseClient: SupabaseClient = koinInject()
    val sessionStatus by supabaseClient.auth.sessionStatus.collectAsStateWithLifecycle(
        initialValue = SessionStatus.Initializing,
    )

    LaunchedEffect(sessionStatus) {
        if (sessionStatus is SessionStatus.Authenticated) {
            onAuthenticated()
        }
    }

    when (sessionStatus) {
        SessionStatus.Initializing,
        is SessionStatus.RefreshFailure,
        -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        is SessionStatus.NotAuthenticated -> AuthScreen()
        is SessionStatus.Authenticated -> Unit
    }
}
