package com.gitmanager.app.data.model

import com.google.gson.annotations.SerializedName

data class GitHubRepo(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("full_name") val fullName: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("html_url") val htmlUrl: String = "",
    @SerializedName("clone_url") val cloneUrl: String? = null,
    @SerializedName("ssh_url") val sshUrl: String? = null,
    @SerializedName("private") val isPrivate: Boolean = false,
    @SerializedName("fork") val isFork: Boolean = false,
    @SerializedName("stargazers_count") val stargazersCount: Int = 0,
    @SerializedName("forks_count") val forksCount: Int = 0,
    @SerializedName("watchers_count") val watchersCount: Int = 0,
    @SerializedName("open_issues_count") val openIssuesCount: Int = 0,
    @SerializedName("language") val language: String? = null,
    @SerializedName("default_branch") val defaultBranch: String = "main",
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("pushed_at") val pushedAt: String? = null,
    @SerializedName("owner") val owner: GitHubUser? = null,
    @SerializedName("permissions") val permissions: RepoPermissions? = null
)

data class RepoPermissions(
    @SerializedName("admin") val admin: Boolean = false,
    @SerializedName("push") val push: Boolean = false,
    @SerializedName("pull") val pull: Boolean = true
)
