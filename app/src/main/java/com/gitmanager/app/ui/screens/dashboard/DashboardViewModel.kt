package com.gitmanager.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitmanager.app.core.network.Resource
import com.gitmanager.app.data.model.GitHubRepo
import com.gitmanager.app.data.model.GitHubUser
import com.gitmanager.app.data.repository.GitHubRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val user: GitHubUser? = null,
    val recentRepos: List<GitHubRepo> = emptyList(),
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val gitHubRepository: GitHubRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val userDeferred = async { gitHubRepository.getAuthenticatedUser() }
            val reposDeferred = async { gitHubRepository.getUserRepos(page = 1, sort = "updated") }

            val userResult = userDeferred.await()
            val reposResult = reposDeferred.await()

            if (userResult is Resource.Success && reposResult is Resource.Success) {
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    user = userResult.data,
                    recentRepos = reposResult.data.take(6),
                    errorMessage = null
                )
            } else {
                val error = (userResult as? Resource.Error)?.message
                    ?: (reposResult as? Resource.Error)?.message
                    ?: "Failed to load dashboard data"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error
                )
            }
        }
    }
}
