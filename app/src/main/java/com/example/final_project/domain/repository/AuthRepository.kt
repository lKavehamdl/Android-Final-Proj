package com.example.final_project.domain.repository

import com.example.final_project.domain.auth.AuthResult

interface AuthRepository {
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signUp(email: String, password: String): AuthResult
    suspend fun signOut(): AuthResult
}
