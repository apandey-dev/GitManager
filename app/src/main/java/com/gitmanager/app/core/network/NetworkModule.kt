package com.gitmanager.app.core.network

import android.content.Context
import com.gitmanager.app.core.datastore.AuthPreferences
import com.gitmanager.app.core.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private var apiService: GitHubApiService? = null
    private var authPreferences: AuthPreferences? = null

    fun initialize(context: Context) {
        if (authPreferences == null) {
            authPreferences = AuthPreferences(context.applicationContext)
        }
    }

    fun getAuthPreferences(context: Context): AuthPreferences {
        if (authPreferences == null) {
            authPreferences = AuthPreferences(context.applicationContext)
        }
        return authPreferences!!
    }

    fun getApiService(context: Context): GitHubApiService {
        if (apiService == null) {
            val prefs = getAuthPreferences(context)

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(prefs))
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(Constants.GITHUB_API_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(GitHubApiService::class.java)
        }
        return apiService!!
    }
}
