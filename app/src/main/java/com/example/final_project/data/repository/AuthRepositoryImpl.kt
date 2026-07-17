package com.example.final_project.data.repository

import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.domain.auth.AuthError
import com.example.final_project.domain.auth.AuthResult
import com.example.final_project.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthRepositoryImpl(
    private val supabase: SupabaseClient,
    private val dispatchers: DispatchersProvider,
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): AuthResult =
        withContext(dispatchers.io) {
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email.trim()
                    this.password = password
                }
                AuthResult.Success
            } catch (t: Throwable) {
                AuthResult.Failure(mapThrowable(t))
            }
        }

    override suspend fun signUp(email: String, password: String): AuthResult =
        withContext(dispatchers.io) {
            try {
                supabase.auth.signUpWith(Email) {
                    this.email = email.trim()
                    this.password = password
                }
                when (supabase.auth.sessionStatus.value) {
                    is SessionStatus.Authenticated -> AuthResult.Success
                    else -> AuthResult.SuccessNeedsEmailConfirmation
                }
            } catch (t: Throwable) {
                AuthResult.Failure(mapThrowable(t))
            }
        }

    override suspend fun signOut(): AuthResult =
        withContext(dispatchers.io) {
            try {
                supabase.auth.signOut()
                AuthResult.Success
            } catch (t: Throwable) {
                AuthResult.Failure(mapThrowable(t))
            }
        }

    private fun mapThrowable(t: Throwable): AuthError = when (t) {
        is AuthWeakPasswordException -> AuthError.WeakPassword
        is AuthRestException -> mapAuthCode(t.errorCode, t.error)
        is SocketTimeoutException,
        is HttpRequestTimeoutException,
        -> AuthError.Timeout
        is UnknownHostException,
        is IOException,
        -> AuthError.Network
        else -> {
            val cause = t.cause
            when {
                cause is AuthRestException -> mapAuthCode(cause.errorCode, cause.error)
                cause is SocketTimeoutException || cause is HttpRequestTimeoutException -> AuthError.Timeout
                cause is IOException -> AuthError.Network
                else -> AuthError.Unknown
            }
        }
    }

    private fun mapAuthCode(code: AuthErrorCode?, raw: String?): AuthError = when (code) {
        AuthErrorCode.InvalidCredentials -> AuthError.InvalidCredentials
        AuthErrorCode.UserNotFound -> AuthError.UserNotFound
        AuthErrorCode.EmailExists,
        AuthErrorCode.UserAlreadyExists,
        -> AuthError.EmailAlreadyRegistered
        AuthErrorCode.WeakPassword -> AuthError.WeakPassword
        AuthErrorCode.EmailNotConfirmed -> AuthError.EmailNotConfirmed
        AuthErrorCode.OverRequestRateLimit,
        AuthErrorCode.RequestTimeout,
        AuthErrorCode.HookTimeout,
        AuthErrorCode.HookTimeoutAfterRetry,
        -> AuthError.Timeout
        else -> when (raw?.lowercase()) {
            "invalid_credentials" -> AuthError.InvalidCredentials
            "email_exists", "user_already_exists" -> AuthError.EmailAlreadyRegistered
            "user_not_found" -> AuthError.UserNotFound
            "weak_password" -> AuthError.WeakPassword
            "email_not_confirmed" -> AuthError.EmailNotConfirmed
            else -> AuthError.Unknown
        }
    }
}
