package com.example.final_project.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project.core.dispatchers.DispatchersProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(
    private val supabaseClient: SupabaseClient,
    private val dispatchers: DispatchersProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                supabaseClient.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                _uiState.update { it.copy(isLoading = false, error = null) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Sign in failed") }
            }
        }
    }
    
    fun signUp(email: String, password: String) {
        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                supabaseClient.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                _uiState.update { it.copy(isLoading = false, error = null) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Sign up failed") }
            }
        }
    }
}
