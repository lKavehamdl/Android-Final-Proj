package com.example.final_project.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
    val sessionStatus by supabaseClient.auth.sessionStatus.collectAsStateWithLifecycle()

    LaunchedEffect(sessionStatus) {
        if (sessionStatus is SessionStatus.Authenticated) {
            onAuthenticated()
        }
    }

    if (sessionStatus !is SessionStatus.Authenticated) {
        AuthScreen()
    }
}
