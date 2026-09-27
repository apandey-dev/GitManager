package com.gitmanager.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitmanager.app.core.network.Resource
import com.gitmanager.app.data.model.GitHubRepo
import com.gitmanager.app.data.model.GitHubUser
import com.gitmanager.app.data.repository.GitHubRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserSearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val users: List<GitHubUser> = emptyList(),
    val totalCount: Int = 0,
    val currentPage: Int = 1,
    val canPaginate: Boolean = false,
    val errorMessage: String? = null
)

data class UserProfileUiState(
    val isLoading: Boolean = false,
    val user: GitHubUser? = null,
    val repos: List<GitHubRepo> = emptyList(),
    val errorMessage: String? = null
)

class SearchViewModel(
    private val gitHubRepository: GitHubRepository
) : ViewModel() {

    private val _searchState = MutableStateFlow(UserSearchUiState())
    val searchState: StateFlow<UserSearchUiState> = _searchState.asStateFlow()

    private val _profileState = MutableStateFlow(UserProfileUiState())
    val profileState: StateFlow<UserProfileUiState> = _profileState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _searchState.value = _searchState.value.copy(query = newQuery)
        searchJob?.cancel()
        if (newQuery.trim().isBlank()) {
            _searchState.value = _searchState.value.copy(
                isLoading = false,
                users = emptyList(),
                totalCount = 0,
                canPaginate = false
            )
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce 500ms
            performUserSearch(isInitial = true)
        }
    }

    fun performUserSearch(isInitial: Boolean = false) {
        val query = _searchState.value.query.trim()
        if (query.isBlank()) return

        val currentState = _searchState.value
        val page = if (isInitial) 1 else currentState.currentPage + 1

        if (!isInitial && (!currentState.canPaginate || currentState.isPaginating)) return

        viewModelScope.launch {
            if (isInitial) {
                _searchState.value = currentState.copy(isLoading = true, errorMessage = null)
            } else {
                _searchState.value = currentState.copy(isPaginating = true)
            }

            when (val result = gitHubRepository.searchUsers(query, page)) {
                is Resource.Success -> {
                    val newItems = result.data.items
                    val updatedUsers = if (isInitial) newItems else currentState.users + newItems
                    val hasMore = updatedUsers.size < result.data.totalCount && newItems.isNotEmpty()

                    _searchState.value = _searchState.value.copy(
                        isLoading = false,
                        isPaginating = false,
                        users = updatedUsers,
                        totalCount = result.data.totalCount,
                        currentPage = page,
                        canPaginate = hasMore
                    )
                }
                is Resource.Error -> {
                    _searchState.value = _searchState.value.copy(
                        isLoading = false,
                        isPaginating = false,
                        errorMessage = result.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun loadUserProfile(username: String) {
        viewModelScope.launch {
            _profileState.value = UserProfileUiState(isLoading = true)

            val profileResult = gitHubRepository.getUserProfile(username)
            val reposResult = gitHubRepository.getPublicUserRepos(username, page = 1)

            if (profileResult is Resource.Success) {
                _profileState.value = UserProfileUiState(
                    isLoading = false,
                    user = profileResult.data,
                    repos = (reposResult as? Resource.Success)?.data ?: emptyList(),
                    errorMessage = null
                )
            } else {
                _profileState.value = UserProfileUiState(
                    isLoading = false,
                    errorMessage = (profileResult as? Resource.Error)?.message ?: "Failed to load user profile"
                )
            }
        }
    }
}
