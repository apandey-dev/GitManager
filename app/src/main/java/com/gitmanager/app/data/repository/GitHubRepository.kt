package com.gitmanager.app.data.repository

import com.gitmanager.app.core.network.GitHubApiService
import com.gitmanager.app.core.network.Resource
import com.gitmanager.app.data.model.CreateRepoRequest
import com.gitmanager.app.data.model.GitHubRepo
import com.gitmanager.app.data.model.GitHubUser
import com.gitmanager.app.data.model.ReadmeContent
import com.gitmanager.app.data.model.RepoSearchResponse
import com.gitmanager.app.data.model.UpdateRepoRequest
import com.gitmanager.app.data.model.UserSearchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GitHubRepository(
    private val apiService: GitHubApiService
) {

    suspend fun getAuthenticatedUser(): Resource<GitHubUser> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAuthenticatedUser()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message().ifBlank { "Failed to fetch profile" })
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getUserProfile(username: String): Resource<GitHubUser> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUser(username)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message().ifBlank { "User not found" })
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getUserRepos(
        page: Int = 1,
        visibility: String? = null,
        type: String? = null,
        sort: String = "updated"
    ): Resource<List<GitHubRepo>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserRepos(
                visibility = visibility,
                type = type,
                sort = sort,
                page = page
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message().ifBlank { "Failed to fetch repositories" })
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getPublicUserRepos(
        username: String,
        page: Int = 1
    ): Resource<List<GitHubRepo>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPublicUserRepos(
                username = username,
                page = page
            )
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message().ifBlank { "Failed to load user repositories" })
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getRepoDetail(owner: String, repo: String): Resource<GitHubRepo> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRepo(owner, repo)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message().ifBlank { "Failed to fetch repository" })
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun createRepository(
        name: String,
        description: String?,
        isPrivate: Boolean,
        autoInit: Boolean = true
    ): Resource<GitHubRepo> = withContext(Dispatchers.IO) {
        try {
            val request = CreateRepoRequest(
                name = name.trim(),
                description = description?.trim(),
                isPrivate = isPrivate,
                autoInit = autoInit
            )
            val response = apiService.createRepo(request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    422 -> "Repository name already exists or is invalid."
                    403 -> "You don't have permission to create repositories."
                    else -> response.message().ifBlank { "Failed to create repository" }
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun updateRepository(
        owner: String,
        repo: String,
        name: String? = null,
        description: String? = null,
        isPrivate: Boolean? = null
    ): Resource<GitHubRepo> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateRepoRequest(
                name = name?.trim(),
                description = description?.trim(),
                isPrivate = isPrivate
            )
            val response = apiService.updateRepo(owner, repo, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message().ifBlank { "Failed to update repository" })
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun deleteRepository(owner: String, repo: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteRepo(owner, repo)
            if (response.isSuccessful || response.code() == 204) {
                Resource.Success(Unit)
            } else {
                val errorMsg = when (response.code()) {
                    403 -> "Delete failed. Make sure token has 'delete_repo' scope."
                    404 -> "Repository not found or already deleted."
                    else -> response.message().ifBlank { "Failed to delete repository" }
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun isRepoStarred(owner: String, repo: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.checkIfRepoStarred(owner, repo)
            response.code() == 204
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleStarRepo(owner: String, repo: String, currentlyStarred: Boolean): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = if (currentlyStarred) {
                apiService.unstarRepo(owner, repo)
            } else {
                apiService.starRepo(owner, repo)
            }
            if (response.isSuccessful || response.code() == 204) {
                Resource.Success(!currentlyStarred)
            } else {
                Resource.Error("Failed to update star status")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun searchUsers(query: String, page: Int = 1): Resource<UserSearchResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchUsers(query = query, page = page)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message().ifBlank { "Failed to search users" })
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun searchRepos(query: String, page: Int = 1): Resource<RepoSearchResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchRepos(query = query, page = page)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message().ifBlank { "Failed to search repositories" })
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getRepoReadme(owner: String, repo: String): Resource<ReadmeContent> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRepoReadme(owner, repo)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("No README found for this repository")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load README")
        }
    }
}
