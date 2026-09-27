package com.gitmanager.app.core.utils

object Constants {
    const val GITHUB_API_BASE_URL = "https://api.github.com/"
    const val GITHUB_OAUTH_AUTHORIZE_URL = "https://github.com/login/oauth/authorize"
    const val GITHUB_OAUTH_TOKEN_URL = "https://github.com/login/oauth/access_token"

    // GitHub OAuth App Configuration
    // Paste your Client ID and Client Secret from GitHub Developer settings below:
    const val OAUTH_CLIENT_ID = "YOUR_CLIENT_ID_HERE"
    const val OAUTH_CLIENT_SECRET = "YOUR_CLIENT_SECRET_HERE"
    const val OAUTH_REDIRECT_URI = "gitmanager://oauth"
    const val OAUTH_SCOPES = "repo,user,delete_repo,read:org"

    const val DEFAULT_PAGE_SIZE = 30
    const val PREFS_NAME = "git_manager_auth_prefs"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_CACHED_USER = "cached_user"
    const val KEY_DARK_THEME = "dark_theme"
}
