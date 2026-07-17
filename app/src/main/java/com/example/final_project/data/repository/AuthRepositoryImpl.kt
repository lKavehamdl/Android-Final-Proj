package com.example.final_project.data.repository

import com.example.final_project.core.dispatchers.DispatchersProvider
import com.example.final_project.data.auth.AuthExceptionMapper
import com.example.final_project.domain.auth.AuthResult
import com.example.final_project.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.withContext

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
                AuthResult.Failure(AuthExceptionMapper.map(t))
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
                AuthResult.Failure(AuthExceptionMapper.map(t))
            }
        }

    override suspend fun signOut(): AuthResult =
        withContext(dispatchers.io) {
            try {
                supabase.auth.signOut()
                AuthResult.Success
            } catch (t: Throwable) {
                AuthResult.Failure(AuthExceptionMapper.map(t))
            }
        }
}
