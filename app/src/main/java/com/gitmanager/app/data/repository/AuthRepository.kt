package com.gitmanager.app.data.repository

import com.gitmanager.app.core.datastore.AuthPreferences
import com.gitmanager.app.core.network.GitHubApiService
import com.gitmanager.app.core.network.Resource
import com.gitmanager.app.core.utils.Constants
import com.gitmanager.app.data.model.GitHubUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: GitHubApiService,
    private val authPreferences: AuthPreferences
) {
    val tokenFlow = authPreferences.tokenFlow
    val darkThemeFlow = authPreferences.darkThemeFlow

    fun isLoggedIn(): Boolean = authPreferences.isLoggedIn()

    fun getAccessToken(): String? = authPreferences.getAccessToken()

    fun getCachedUser(): GitHubUser? = authPreferences.getCachedUser()

    fun setDarkTheme(enabled: Boolean) = authPreferences.setDarkTheme(enabled)

    fun logout() {
        authPreferences.clearSession()
    }

    suspend fun loginWithPersonalAccessToken(token: String): Resource<GitHubUser> = withContext(Dispatchers.IO) {
        try {
            val cleanToken = token.trim()
            if (cleanToken.isBlank()) {
                return@withContext Resource.Error("Token cannot be empty")
            }
            // Temporarily save token to execute validation request
            authPreferences.saveAccessToken(cleanToken)

            val response = apiService.getAuthenticatedUser()
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                authPreferences.saveCachedUser(user)
                Resource.Success(user)
            } else {
                authPreferences.clearSession()
                val errorMsg = when (response.code()) {
                    401 -> "Invalid Personal Access Token. Please check token scopes and expiration."
                    403 -> "API Rate limit exceeded or access forbidden."
                    else -> "Authentication failed: ${response.message()}"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            authPreferences.clearSession()
            Resource.Error(e.localizedMessage ?: "Network connection error")
        }
    }

    suspend fun handleOAuthCallback(code: String, clientSecret: String = ""): Resource<GitHubUser> = withContext(Dispatchers.IO) {
        try {
            // Exchange authorization code for token if secret is provided or handled
            if (clientSecret.isNotBlank()) {
                val tokenResponse = apiService.exchangeOAuthCode(
                    url = Constants.GITHUB_OAUTH_TOKEN_URL,
                    clientId = Constants.OAUTH_CLIENT_ID,
                    clientSecret = clientSecret,
                    code = code,
                    redirectUri = Constants.OAUTH_REDIRECT_URI
                )
                if (tokenResponse.isSuccessful && tokenResponse.body()?.accessToken != null) {
                    val token = tokenResponse.body()!!.accessToken!!
                    authPreferences.saveAccessToken(token)
                    val userResponse = apiService.getAuthenticatedUser()
                    if (userResponse.isSuccessful && userResponse.body() != null) {
                        val user = userResponse.body()!!
                        authPreferences.saveCachedUser(user)
                        return@withContext Resource.Success(user)
                    }
                }
            }
            // If direct code or token passed
            authPreferences.saveAccessToken(code)
            val userResponse = apiService.getAuthenticatedUser()
            if (userResponse.isSuccessful && userResponse.body() != null) {
                val user = userResponse.body()!!
                authPreferences.saveCachedUser(user)
                Resource.Success(user)
            } else {
                authPreferences.clearSession()
                Resource.Error("Could not authenticate with OAuth code. Please try with Access Token.")
            }
        } catch (e: Exception) {
            authPreferences.clearSession()
            Resource.Error(e.localizedMessage ?: "Failed to complete GitHub OAuth")
        }
    }
}
