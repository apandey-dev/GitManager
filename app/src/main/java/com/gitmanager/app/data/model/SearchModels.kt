package com.gitmanager.app.data.model

import com.google.gson.annotations.SerializedName

data class UserSearchResponse(
    @SerializedName("total_count") val totalCount: Int = 0,
    @SerializedName("incomplete_results") val incompleteResults: Boolean = false,
    @SerializedName("items") val items: List<GitHubUser> = emptyList()
)

data class RepoSearchResponse(
    @SerializedName("total_count") val totalCount: Int = 0,
    @SerializedName("incomplete_results") val incompleteResults: Boolean = false,
    @SerializedName("items") val items: List<GitHubRepo> = emptyList()
)

data class ReadmeContent(
    @SerializedName("name") val name: String = "",
    @SerializedName("path") val path: String = "",
    @SerializedName("content") val content: String? = null,
    @SerializedName("encoding") val encoding: String? = null,
    @SerializedName("download_url") val downloadUrl: String? = null,
    @SerializedName("html_url") val htmlUrl: String? = null
)
