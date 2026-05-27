package com.example.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    val authState = authRepository.authState
    val currentUserEmail = authRepository.currentUserEmail

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val result = authRepository.login(email, pass)
            if (result.isFailure) {
                _errorMsg.value = mapAuthError(result.exceptionOrNull())
            } else {
                _errorMsg.value = null
            }
        }
    }

    fun register(email: String, pass: String) {
        viewModelScope.launch {
            val result = authRepository.register(email, pass)
            if (result.isFailure) {
                _errorMsg.value = mapAuthError(result.exceptionOrNull())
            } else {
                _errorMsg.value = null
            }
        }
    }

    private fun mapAuthError(e: Throwable?): String {
        val msg = e?.message?.lowercase() ?: return "Erreur inconnue."
        return when {
            msg.contains("invalid email") || msg.contains("badly formatted") -> "Adresse e-mail invalide."
            msg.contains("wrong password") || msg.contains("invalid credential") || msg.contains("invalid_password") -> "Mot de passe incorrect."
            msg.contains("user not found") || msg.contains("no user record") -> "Aucun compte trouvé avec cette adresse."
            msg.contains("weak password") || msg.contains("at least 6 characters") -> "Le mot de passe doit contenir au moins 6 caractères."
            msg.contains("network error") || msg.contains("offline") || msg.contains("unable to resolve host") -> "Connexion indisponible. Réessaie lorsque tu seras en ligne."
            msg.contains("email already in use") || msg.contains("already exists") -> "Un compte existe déjà avec cette adresse."
            else -> "Erreur de connexion. Veuillez réessayer."
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun clearError() {
        _errorMsg.value = null
    }
}
