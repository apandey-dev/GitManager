package com.gitmanager.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.gitmanager.app.core.theme.GitManagerTheme
import com.gitmanager.app.ui.navigation.AppNavHost
import com.gitmanager.app.ui.screens.auth.AuthViewModel

class MainActivity : ComponentActivity() {

    private lateinit var app: GitManagerApp
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        app = application as GitManagerApp
        authViewModel = AuthViewModel(app.authRepository)

        handleIntent(intent)

        setContent {
            val isDarkTheme by app.authRepository.darkThemeFlow.collectAsState()

            GitManagerTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavHost(
                        authRepository = app.authRepository,
                        gitHubRepository = app.gitHubRepository,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null && data.scheme == "gitmanager" && data.host == "oauth") {
            val code = data.getQueryParameter("code")
            if (!code.isNullOrBlank()) {
                authViewModel.handleOAuthCallback(code)
            }
        }
    }
}
