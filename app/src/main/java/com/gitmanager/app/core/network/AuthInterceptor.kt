package com.gitmanager.app.core.network

import com.gitmanager.app.core.datastore.AuthPreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val authPreferences: AuthPreferences) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = authPreferences.getAccessToken()

        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/vnd.github.v3+json")
            .header("User-Agent", "GitManager-Android-App")

        if (!token.isNullOrBlank() && originalRequest.header("Authorization") == null) {
            // Support both Bearer format and token format
            val authHeader = if (token.startsWith("Bearer ") || token.startsWith("token ")) {
                token
            } else {
                "Bearer $token"
            }
            requestBuilder.header("Authorization", authHeader)
        }

        return chain.proceed(requestBuilder.build())
    }
}
