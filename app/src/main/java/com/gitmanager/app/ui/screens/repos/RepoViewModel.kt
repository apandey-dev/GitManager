package com.gitmanager.app.ui.screens.repos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitmanager.app.core.network.Resource
import com.gitmanager.app.data.model.GitHubRepo
import com.gitmanager.app.data.model.ReadmeContent
import com.gitmanager.app.data.repository.GitHubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class RepoFilter(val label: String, val apiValue: String?) {
    ALL("All", null),
    PUBLIC("Public", "public"),
    PRIVATE("Private", "private"),
    FORKS("Forks", "forks")
}

data class RepoListUiState(
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val repos: List<GitHubRepo> = emptyList(),
    val filteredRepos: List<GitHubRepo> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: RepoFilter = RepoFilter.ALL,
    val currentPage: Int = 1,
    val canPaginate: Boolean = true,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null
)

data class RepoDetailUiState(
    val isLoading: Boolean = false,
    val repo: GitHubRepo? = null,
    val isStarred: Boolean = false,
    val isStarLoading: Boolean = false,
    val readmeContent: String? = null,
    val isReadmeLoading: Boolean = false,
    val errorMessage: String? = null,
    val isDeleted: Boolean = false
)

class RepoViewModel(
    private val gitHubRepository: GitHubRepository
) : ViewModel() {

    private val _listState = MutableStateFlow(RepoListUiState(isLoading = true))
    val listState: StateFlow<RepoListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow(RepoDetailUiState())
    val detailState: StateFlow<RepoDetailUiState> = _detailState.asStateFlow()

    init {
        loadRepositories(isInitial = true)
    }

    fun loadRepositories(isInitial: Boolean = false) {
        viewModelScope.launch {
            val currentState = _listState.value
            if (isInitial) {
                _listState.value = currentState.copy(isLoading = true, currentPage = 1, canPaginate = true, errorMessage = null)
            } else {
                if (!currentState.canPaginate || currentState.isPaginating) return@launch
                _listState.value = currentState.copy(isPaginating = true)
            }

            val pageToLoad = if (isInitial) 1 else currentState.currentPage + 1
            val filter = currentState.activeFilter

            val result = gitHubRepository.getUserRepos(
                page = pageToLoad,
                visibility = if (filter == RepoFilter.PUBLIC || filter == RepoFilter.PRIVATE) filter.apiValue else null,
                type = if (filter == RepoFilter.FORKS) "all" else null
            )

            when (result) {
                is Resource.Success -> {
                    val newRepos = result.data
                    val updatedList = if (isInitial) newRepos else currentState.repos + newRepos
                    val hasMore = newRepos.size >= 30

                    _listState.value = _listState.value.copy(
                        isLoading = false,
                        isPaginating = false,
                        repos = updatedList,
                        currentPage = pageToLoad,
                        canPaginate = hasMore
                    )
                    applyFilterAndSearch()
                }
                is Resource.Error -> {
                    _listState.value = _listState.value.copy(
                        isLoading = false,
                        isPaginating = false,
                        errorMessage = result.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun setFilter(filter: RepoFilter) {
        _listState.value = _listState.value.copy(activeFilter = filter)
        loadRepositories(isInitial = true)
    }

    fun onSearchQueryChanged(query: String) {
        _listState.value = _listState.value.copy(searchQuery = query)
        applyFilterAndSearch()
    }

    private fun applyFilterAndSearch() {
        val state = _listState.value
        val query = state.searchQuery.trim().lowercase()

        var list = state.repos
        if (state.activeFilter == RepoFilter.FORKS) {
            list = list.filter { it.isFork }
        } else if (state.activeFilter == RepoFilter.PUBLIC) {
            list = list.filter { !it.isPrivate }
        } else if (state.activeFilter == RepoFilter.PRIVATE) {
            list = list.filter { it.isPrivate }
        }

        if (query.isNotBlank()) {
            list = list.filter {
                it.name.lowercase().contains(query) ||
                (it.description?.lowercase()?.contains(query) == true) ||
                (it.language?.lowercase()?.contains(query) == true)
            }
        }

        _listState.value = state.copy(filteredRepos = list)
    }

    // --- CRUD Operations ---

    fun createRepository(
        name: String,
        description: String?,
        isPrivate: Boolean,
        onSuccess: (GitHubRepo) -> Unit
    ) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true)
            when (val result = gitHubRepository.createRepository(name, description, isPrivate)) {
                is Resource.Success -> {
                    val createdRepo = result.data
                    // Immediately prepend to local list for instant feedback
                    val updatedList = listOf(createdRepo) + _listState.value.repos.filterNot { it.id == createdRepo.id }
                    _listState.value = _listState.value.copy(
                        isLoading = false,
                        repos = updatedList,
                        actionSuccessMessage = "Repository '${createdRepo.name}' created!"
                    )
                    applyFilterAndSearch()
                    onSuccess(createdRepo)
                }
                is Resource.Error -> {
                    _listState.value = _listState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun loadRepoDetail(owner: String, repo: String) {
        viewModelScope.launch {
            _detailState.value = RepoDetailUiState(isLoading = true, isReadmeLoading = true)

            val repoResult = gitHubRepository.getRepoDetail(owner, repo)
            val isStarred = gitHubRepository.isRepoStarred(owner, repo)
            val readmeResult = gitHubRepository.getRepoReadme(owner, repo)

            var parsedReadme: String? = null
            if (readmeResult is Resource.Success) {
                parsedReadme = try {
                    val encoded = readmeResult.data.content ?: ""
                    val cleanBase64 = encoded.replace("\n", "").replace("\r", "")
                    String(android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT))
                } catch (e: Exception) {
                    "Unable to decode README preview."
                }
            }

            if (repoResult is Resource.Success) {
                _detailState.value = RepoDetailUiState(
                    isLoading = false,
                    repo = repoResult.data,
                    isStarred = isStarred,
                    readmeContent = parsedReadme,
                    isReadmeLoading = false,
                    errorMessage = null
                )
            } else {
                _detailState.value = RepoDetailUiState(
                    isLoading = false,
                    isReadmeLoading = false,
                    errorMessage = (repoResult as? Resource.Error)?.message ?: "Failed to load repo details"
                )
            }
        }
    }

    fun toggleStar(owner: String, repo: String) {
        viewModelScope.launch {
            val currentStarred = _detailState.value.isStarred
            _detailState.value = _detailState.value.copy(isStarLoading = true)

            when (val result = gitHubRepository.toggleStarRepo(owner, repo, currentStarred)) {
                is Resource.Success -> {
                    val updatedRepo = _detailState.value.repo?.let {
                        val count = if (result.data) it.stargazersCount + 1 else (it.stargazersCount - 1).coerceAtLeast(0)
                        it.copy(stargazersCount = count)
                    }
                    _detailState.value = _detailState.value.copy(
                        isStarLoading = false,
                        isStarred = result.data,
                        repo = updatedRepo
                    )
                }
                is Resource.Error -> {
                    _detailState.value = _detailState.value.copy(
                        isStarLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun updateRepository(
        owner: String,
        repo: String,
        name: String?,
        description: String?,
        isPrivate: Boolean?
    ) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isLoading = true)
            when (val result = gitHubRepository.updateRepository(owner, repo, name, description, isPrivate)) {
                is Resource.Success -> {
                    val updated = result.data
                    _detailState.value = _detailState.value.copy(
                        isLoading = false,
                        repo = updated
                    )
                    // Immediately update local list in memory so UI reflects change instantly
                    val updatedRepos = _listState.value.repos.map {
                        if (it.name.equals(repo, ignoreCase = true)) updated else it
                    }
                    _listState.value = _listState.value.copy(repos = updatedRepos)
                    applyFilterAndSearch()
                }
                is Resource.Error -> {
                    _detailState.value = _detailState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> Unit
            }
        }
    }

    fun deleteRepository(owner: String, repo: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isLoading = true)
            // Immediately remove from local list for 0ms instant UI feedback
            val updatedRepos = _listState.value.repos.filterNot {
                it.name.equals(repo, ignoreCase = true)
            }
            _listState.value = _listState.value.copy(
                repos = updatedRepos,
                actionSuccessMessage = "Repository '$repo' deleted"
            )
            applyFilterAndSearch()

            when (val result = gitHubRepository.deleteRepository(owner, repo)) {
                is Resource.Success -> {
                    _detailState.value = _detailState.value.copy(
                        isLoading = false,
                        isDeleted = true
                    )
                    onDeleted()
                }
                is Resource.Error -> {
                    _detailState.value = _detailState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                    // Reload if error occurred
                    loadRepositories(isInitial = true)
                }
                else -> Unit
            }
        }
    }

    fun clearMessages() {
        _listState.value = _listState.value.copy(errorMessage = null, actionSuccessMessage = null)
        _detailState.value = _detailState.value.copy(errorMessage = null)
    }
}
