package com.gitmanager.app.data.model

import com.google.gson.annotations.SerializedName

data class CreateRepoRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("private") val isPrivate: Boolean = false,
    @SerializedName("auto_init") val autoInit: Boolean = true,
    @SerializedName("has_issues") val hasIssues: Boolean = true,
    @SerializedName("has_projects") val hasProjects: Boolean = true,
    @SerializedName("has_wiki") val hasWiki: Boolean = true
)

data class UpdateRepoRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("private") val isPrivate: Boolean? = null,
    @SerializedName("has_issues") val hasIssues: Boolean? = null
)
