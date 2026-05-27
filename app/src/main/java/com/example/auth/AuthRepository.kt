package com.example.auth

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUserEmail: StateFlow<String?>
    val authState: StateFlow<AuthState>

    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
}
