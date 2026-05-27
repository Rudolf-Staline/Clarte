package com.example.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository : AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    
    private val _currentUserEmail = MutableStateFlow(auth.currentUser?.email)
    override val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(
        if (auth.currentUser != null) AuthState.Authenticated else AuthState.Unauthenticated
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUserEmail.value = user?.email
            if (user != null) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        _authState.value = AuthState.Loading
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            _authState.value = AuthState.Authenticated
            Result.success(Unit)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Erreur de connexion")
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<Unit> {
        _authState.value = AuthState.Loading
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            _authState.value = AuthState.Authenticated
            Result.success(Unit)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Erreur d'inscription")
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            auth.signOut()
            _authState.value = AuthState.Unauthenticated
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
