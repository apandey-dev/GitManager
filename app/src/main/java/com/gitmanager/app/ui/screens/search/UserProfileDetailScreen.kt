package com.gitmanager.app.ui.screens.search

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gitmanager.app.core.utils.ShareUtils
import com.gitmanager.app.data.model.GitHubRepo
import com.gitmanager.app.ui.components.EmptyStateView
import com.gitmanager.app.ui.components.ErrorStateView
import com.gitmanager.app.ui.components.MonochromeCard
import com.gitmanager.app.ui.components.MonochromeTopBar
import com.gitmanager.app.ui.components.RepoCardItem
import com.gitmanager.app.ui.components.RepoQrShareBottomSheet
import com.gitmanager.app.ui.screens.dashboard.MetricItem

@Composable
fun UserProfileDetailScreen(
    username: String,
    viewModel: SearchViewModel,
    onNavigateToRepoDetail: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.profileState.collectAsState()
    var selectedRepoForShare by remember { mutableStateOf<GitHubRepo?>(null) }

    LaunchedEffect(username) {
        viewModel.loadUserProfile(username)
    }

    Scaffold(
        topBar = {
            MonochromeTopBar(
                title = "@$username",
                subtitle = "Public Profile",
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }
        } else if (state.errorMessage != null && state.user == null) {
            ErrorStateView(
                message = state.errorMessage!!,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onRetry = { viewModel.loadUserProfile(username) }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Hero Card
                item {
                    state.user?.let { user ->
                        MonochromeCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(user.avatarUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Avatar",
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            text = user.name ?: user.login,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "@${user.login}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (!user.location.isNullOrBlank()) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.LocationOn,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = user.location,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                if (!user.bio.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = user.bio,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    MetricItem(
                                        label = "Public Repos",
                                        value = "${user.publicRepos}"
                                    )
                                    MetricItem(
                                        label = "Followers",
                                        value = "${user.followers}"
                                    )
                                    MetricItem(
                                        label = "Following",
                                        value = "${user.following}"
                                    )
                                }
                            }
                        }
                    }
                }

                // Public Repositories Section
                item {
                    Text(
                        text = "Public Repositories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (state.repos.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "No Public Repositories",
                            message = "This user hasn't published any public repositories yet.",
                            icon = Icons.Outlined.Code
                        )
                    }
                } else {
                    items(state.repos, key = { it.id }) { repo ->
                        RepoCardItem(
                            repo = repo,
                            onClick = { onNavigateToRepoDetail(repo.owner?.login ?: username, repo.name) },
                            onLongClick = { selectedRepoForShare = repo },
                            onMoreClick = { selectedRepoForShare = repo }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Share & QR Bottom Sheet
    selectedRepoForShare?.let { repo ->
        RepoQrShareBottomSheet(
            repo = repo,
            onDismissRequest = { selectedRepoForShare = null }
        )
    }
}
