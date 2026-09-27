package com.gitmanager.app.core.network

import com.gitmanager.app.data.model.CreateRepoRequest
import com.gitmanager.app.data.model.GitHubRepo
import com.gitmanager.app.data.model.GitHubUser
import com.gitmanager.app.data.model.OAuthTokenResponse
import com.gitmanager.app.data.model.ReadmeContent
import com.gitmanager.app.data.model.RepoSearchResponse
import com.gitmanager.app.data.model.UpdateRepoRequest
import com.gitmanager.app.data.model.UserSearchResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface GitHubApiService {

    // --- User Endpoints ---
    @GET("user")
    suspend fun getAuthenticatedUser(): Response<GitHubUser>

    @GET("users/{username}")
    suspend fun getUser(
        @Path("username") username: String
    ): Response<GitHubUser>

    // --- Repository Endpoints (CRUD) ---

    // Read: Authenticated user's repos with pagination & filter
    @GET("user/repos")
    suspend fun getUserRepos(
        @Query("visibility") visibility: String? = null, // all, public, private
        @Query("affiliation") affiliation: String? = "owner,collaborator",
        @Query("type") type: String? = null, // all, owner, member
        @Query("sort") sort: String? = "updated", // created, updated, pushed, full_name
        @Query("direction") direction: String? = "desc",
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): Response<List<GitHubRepo>>

    // Read: Public user's repos
    @GET("users/{username}/repos")
    suspend fun getPublicUserRepos(
        @Path("username") username: String,
        @Query("sort") sort: String? = "updated",
        @Query("direction") direction: String? = "desc",
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): Response<List<GitHubRepo>>

    // Read: Specific Repo
    @GET("repos/{owner}/{repo}")
    suspend fun getRepo(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<GitHubRepo>

    // Create: New Repository
    @POST("user/repos")
    suspend fun createRepo(
        @Body request: CreateRepoRequest
    ): Response<GitHubRepo>

    // Update: Edit Repository
    @PATCH("repos/{owner}/{repo}")
    suspend fun updateRepo(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body request: UpdateRepoRequest
    ): Response<GitHubRepo>

    // Delete: Delete Repository
    @DELETE("repos/{owner}/{repo}")
    suspend fun deleteRepo(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<Unit>

    // --- Star Actions ---
    @GET("user/starred/{owner}/{repo}")
    suspend fun checkIfRepoStarred(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<Unit>

    @PUT("user/starred/{owner}/{repo}")
    suspend fun starRepo(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<Unit>

    @DELETE("user/starred/{owner}/{repo}")
    suspend fun unstarRepo(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<Unit>

    // --- Search Endpoints with Pagination ---
    @GET("search/users")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("sort") sort: String? = null,
        @Query("order") order: String? = "desc",
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): Response<UserSearchResponse>

    @GET("search/repositories")
    suspend fun searchRepos(
        @Query("q") query: String,
        @Query("sort") sort: String? = "updated",
        @Query("order") order: String? = "desc",
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): Response<RepoSearchResponse>

    // --- Readme Content ---
    @GET("repos/{owner}/{repo}/readme")
    suspend fun getRepoReadme(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<ReadmeContent>

    // --- OAuth Token Exchange ---
    @Headers("Accept: application/json")
    @POST
    @FormUrlEncoded
    suspend fun exchangeOAuthCode(
        @Url url: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String
    ): Response<OAuthTokenResponse>
}
