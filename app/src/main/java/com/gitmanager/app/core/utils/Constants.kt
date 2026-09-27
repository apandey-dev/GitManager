package com.gitmanager.app.core.utils

object Constants {
    const val GITHUB_API_BASE_URL = "https://api.github.com/"
    const val GITHUB_OAUTH_AUTHORIZE_URL = "https://github.com/login/oauth/authorize"
    const val GITHUB_OAUTH_TOKEN_URL = "https://github.com/login/oauth/access_token"

    // Default OAuth Client ID (Can be replaced with user's own GitHub OAuth App client ID)
    const val OAUTH_CLIENT_ID = "Ov23litXexample" // Configurable in app/build or GitHub Developer settings
    const val OAUTH_REDIRECT_URI = "gitmanager://oauth"
    const val OAUTH_SCOPES = "repo,user,delete_repo,read:org"

    const val DEFAULT_PAGE_SIZE = 30
    const val PREFS_NAME = "git_manager_auth_prefs"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_CACHED_USER = "cached_user"
    const val KEY_DARK_THEME = "dark_theme"
}
