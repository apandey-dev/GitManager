package com.gitmanager.app.ui.screens.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitmanager.app.core.network.Resource
import com.gitmanager.app.core.utils.Constants
import com.gitmanager.app.data.model.GitHubUser
import com.gitmanager.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: GitHubUser) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _tokenInput = MutableStateFlow("")
    val tokenInput: StateFlow<String> = _tokenInput.asStateFlow()

    fun onTokenChange(token: String) {
        _tokenInput.value = token
    }

    fun loginWithToken() {
        val token = _tokenInput.value.trim()
        if (token.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your Personal Access Token")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.loginWithPersonalAccessToken(token)) {
                is Resource.Success -> {
                    _uiState.value = AuthUiState.Success(result.data)
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                else -> Unit
            }
        }
    }

    fun launchGitHubOAuth(context: Context) {
        val oauthUrl = "${Constants.GITHUB_OAUTH_AUTHORIZE_URL}?client_id=${Constants.OAUTH_CLIENT_ID}&scope=${Constants.OAUTH_SCOPES}&redirect_uri=${Constants.OAUTH_REDIRECT_URI}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(oauthUrl))
        context.startActivity(intent)
    }

    fun handleOAuthCallback(code: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authRepository.handleOAuthCallback(code, clientSecret = Constants.OAUTH_CLIENT_SECRET)) {
                is Resource.Success -> {
                    _uiState.value = AuthUiState.Success(result.data)
                }
                is Resource.Error -> {
                    _uiState.value = AuthUiState.Error(result.message)
                }
                else -> Unit
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
        _tokenInput.value = ""
    }

    fun logout() {
        authRepository.logout()
        resetState()
    }
}
