package com.example.final_project.domain.auth

/**
 * Domain-level authentication failures — never expose raw backend exceptions to UI.
 */
sealed class AuthError {
    data object EmptyEmail : AuthError()
    data object EmptyPassword : AuthError()
    data object InvalidEmail : AuthError()
    data object WeakPassword : AuthError()
    data object PasswordMismatch : AuthError()
    data object InvalidCredentials : AuthError()
    data object UserNotFound : AuthError()
    data object EmailAlreadyRegistered : AuthError()
    data object EmailNotConfirmed : AuthError()
    data object Network : AuthError()
    data object Timeout : AuthError()
    data object Unknown : AuthError()
}

sealed class AuthResult {
    data object Success : AuthResult()
    /** Signup succeeded but email confirmation is required before a session exists. */
    data object SuccessNeedsEmailConfirmation : AuthResult()
    data class Failure(val error: AuthError) : AuthResult()
}
