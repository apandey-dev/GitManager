package com.gitmanager.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import com.gitmanager.app.core.network.NetworkModule
import com.gitmanager.app.data.repository.AuthRepository
import com.gitmanager.app.data.repository.GitHubRepository

class GitManagerApp : Application(), ImageLoaderFactory {

    lateinit var authRepository: AuthRepository
        private set

    lateinit var gitHubRepository: GitHubRepository
        private set

    override fun onCreate() {
        super.onCreate()
        NetworkModule.initialize(this)

        val apiService = NetworkModule.getApiService(this)
        val authPrefs = NetworkModule.getAuthPreferences(this)

        authRepository = AuthRepository(apiService, authPrefs)
        gitHubRepository = GitHubRepository(apiService)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}
