package com.example.final_project.ui.screens.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.final_project.R
import com.example.final_project.domain.auth.AuthError
import com.example.final_project.domain.auth.AuthResult
import com.example.final_project.domain.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.regex.Pattern

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isSignUpMode: Boolean = false,
    val isLoading: Boolean = false,
    @StringRes val emailErrorRes: Int? = null,
    @StringRes val passwordErrorRes: Int? = null,
    @StringRes val confirmPasswordErrorRes: Int? = null,
    @StringRes val formErrorRes: Int? = null,
)

sealed interface AuthUiEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : AuthUiEffect
}

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _effects = Channel<AuthUiEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(email = value, emailErrorRes = null, formErrorRes = null)
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(password = value, passwordErrorRes = null, formErrorRes = null)
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(confirmPassword = value, confirmPasswordErrorRes = null, formErrorRes = null)
        }
    }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                isSignUpMode = !it.isSignUpMode,
                confirmPassword = "",
                emailErrorRes = null,
                passwordErrorRes = null,
                confirmPasswordErrorRes = null,
                formErrorRes = null,
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        val validation = validate(state)
        val hasFieldErrors = validation.emailErrorRes != null ||
            validation.passwordErrorRes != null ||
            validation.confirmPasswordErrorRes != null
        if (hasFieldErrors) {
            _uiState.value = validation
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    formErrorRes = null,
                    emailErrorRes = null,
                    passwordErrorRes = null,
                    confirmPasswordErrorRes = null,
                )
            }

            val result = if (state.isSignUpMode) {
                authRepository.signUp(state.email, state.password)
            } else {
                authRepository.signIn(state.email, state.password)
            }

            when (result) {
                AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    // SessionStatus.Authenticated drives navigation in MelodifyApp.
                }
                AuthResult.SuccessNeedsEmailConfirmation -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSignUpMode = false,
                            password = "",
                            confirmPassword = "",
                        )
                    }
                    _effects.send(AuthUiEffect.ShowMessage(R.string.auth_verify_email))
                }
                is AuthResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            formErrorRes = result.error.toMessageRes(),
                        )
                    }
                }
            }
        }
    }

    private fun validate(state: AuthUiState): AuthUiState {
        var emailError: Int? = null
        var passwordError: Int? = null
        var confirmError: Int? = null

        val email = state.email.trim()
        when {
            email.isEmpty() -> emailError = R.string.auth_error_empty_email
            !EMAIL_PATTERN.matcher(email).matches() -> emailError = R.string.auth_error_invalid_email
        }

        when {
            state.password.isEmpty() -> passwordError = R.string.auth_error_empty_password
            state.isSignUpMode && state.password.length < MIN_PASSWORD_LENGTH ->
                passwordError = R.string.auth_error_weak_password
        }

        if (state.isSignUpMode) {
            when {
                state.confirmPassword.isEmpty() ->
                    confirmError = R.string.auth_error_empty_confirm_password
                state.confirmPassword != state.password ->
                    confirmError = R.string.auth_error_password_mismatch
            }
        }

        return state.copy(
            emailErrorRes = emailError,
            passwordErrorRes = passwordError,
            confirmPasswordErrorRes = confirmError,
            formErrorRes = null,
        )
    }

    private fun AuthError.toMessageRes(): Int = when (this) {
        AuthError.EmptyEmail -> R.string.auth_error_empty_email
        AuthError.EmptyPassword -> R.string.auth_error_empty_password
        AuthError.InvalidEmail -> R.string.auth_error_invalid_email
        AuthError.WeakPassword -> R.string.auth_error_weak_password
        AuthError.PasswordMismatch -> R.string.auth_error_password_mismatch
        AuthError.InvalidCredentials -> R.string.auth_error_invalid_credentials
        AuthError.UserNotFound -> R.string.auth_error_user_not_found
        AuthError.EmailAlreadyRegistered -> R.string.auth_error_email_exists
        AuthError.EmailNotConfirmed -> R.string.auth_error_email_not_confirmed
        AuthError.Network -> R.string.auth_error_network
        AuthError.Timeout -> R.string.auth_error_timeout
        AuthError.Unknown -> R.string.auth_error_unknown
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
        private val EMAIL_PATTERN: Pattern = Pattern.compile(
            "[a-zA-Z0-9+._%\\-]{1,256}" +
                "@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+",
        )
    }
}
