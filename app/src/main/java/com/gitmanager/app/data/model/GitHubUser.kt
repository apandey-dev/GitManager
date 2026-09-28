package com.gitmanager.app.data.model

import com.google.gson.annotations.SerializedName

data class GitHubUser(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("login") val login: String = "",
    @SerializedName("name") val name: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String = "",
    @SerializedName("html_url") val htmlUrl: String = "",
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("public_repos") val publicRepos: Int = 0,
    @SerializedName("total_private_repos") val totalPrivateRepos: Int = 0,
    @SerializedName("followers") val followers: Int = 0,
    @SerializedName("following") val following: Int = 0,
    @SerializedName("company") val company: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("blog") val blog: String? = null,
    @SerializedName("twitter_username") val twitterUsername: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
 