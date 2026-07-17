package com.example.final_project.data.auth

import com.example.final_project.domain.auth.AuthError
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.ktor.client.plugins.HttpRequestTimeoutException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Maps backend / transport failures to domain [AuthError] — never expose raw exceptions to UI.
 */
object AuthExceptionMapper {

    fun map(t: Throwable): AuthError = when (t) {
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
