package com.gitmanager.app.core.datastore

import android.content.Context
import android.content.SharedPreferences
import com.gitmanager.app.core.utils.Constants
import com.gitmanager.app.data.model.GitHubUser
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _tokenFlow = MutableStateFlow<String?>(getAccessToken())
    val tokenFlow: StateFlow<String?> = _tokenFlow.asStateFlow()

    private val _darkThemeFlow = MutableStateFlow(isDarkTheme())
    val darkThemeFlow: StateFlow<Boolean> = _darkThemeFlow.asStateFlow()

    fun getAccessToken(): String? {
        return prefs.getString(Constants.KEY_ACCESS_TOKEN, null)
    }

    fun saveAccessToken(token: String) {
        prefs.edit().putString(Constants.KEY_ACCESS_TOKEN, token).apply()
        _tokenFlow.value = token
    }

    fun clearSession() {
        prefs.edit()
            .remove(Constants.KEY_ACCESS_TOKEN)
            .remove(Constants.KEY_CACHED_USER)
            .apply()
        _tokenFlow.value = null
    }

    fun saveCachedUser(user: GitHubUser) {
        val json = gson.toJson(user)
        prefs.edit().putString(Constants.KEY_CACHED_USER, json).apply()
    }

    fun getCachedUser(): GitHubUser? {
        val json = prefs.getString(Constants.KEY_CACHED_USER, null) ?: return null
        return try {
            gson.fromJson(json, GitHubUser::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun isDarkTheme(): Boolean {
        return prefs.getBoolean(Constants.KEY_DARK_THEME, false)
    }

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(Constants.KEY_DARK_THEME, enabled).apply()
        _darkThemeFlow.value = enabled
    }

    fun isLoggedIn(): Boolean {
        return !getAccessToken().isNullOrBlank()
    }
}
