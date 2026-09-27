package com.gitmanager.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.gitmanager.app.ui.components.RepoActionBottomSheet
import com.gitmanager.app.ui.components.RepoCardItem
import com.gitmanager.app.ui.components.RepoQrShareBottomSheet

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToRepos: () -> Unit,
    onNavigateToRepoDetail: (String, String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onCreateRepoClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedRepoForActionSheet by remember { mutableStateOf<GitHubRepo?>(null) }
    var selectedRepoForShareCard by remember { mutableStateOf<GitHubRepo?>(null) }

    Scaffold(
        topBar = {
            MonochromeTopBar(
                title = "Dashboard",
                subtitle = "GitHub Workspace",
                actions = {
                    IconButton(onClick = viewModel::loadDashboardData) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (uiState.isLoading) {
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
        } else if (uiState.errorMessage != null && uiState.user == null) {
            ErrorStateView(
                message = uiState.errorMessage!!,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onRetry = viewModel::loadDashboardData
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // User Profile Hero Card
                item {
                    uiState.user?.let { user ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
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

                                // Quick Metrics Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MetricItem(
                                        label = "Public Repos",
                                        value = "${user.publicRepos}"
                                    )
                                    MetricItem(
                                        label = "Private Repos",
                                        value = "${user.totalPrivateRepos}"
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

                // Quick Action Chips Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionChip(
                            icon = Icons.Outlined.Add,
                            label = "New Repo",
                            onClick = onCreateRepoClick,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionChip(
                            icon = Icons.Outlined.Folder,
                            label = "All Repos",
                            onClick = onNavigateToRepos,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionChip(
                            icon = Icons.Outlined.Search,
                            label = "Explore",
                            onClick = onNavigateToSearch,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Recent Repositories Section Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Repositories",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = onNavigateToRepos) {
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (uiState.recentRepos.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "No Repositories Yet",
                            message = "Create your first repository using the New Repo button above.",
                            icon = Icons.Outlined.Code
                        )
                    }
                } else {
                    items(uiState.recentRepos, key = { it.id }) { repo ->
                        RepoCardItem(
                            repo = repo,
                            onClick = {
                                val owner = repo.owner?.login ?: uiState.user?.login ?: ""
                                onNavigateToRepoDetail(owner, repo.name)
                            },
                            onLongClick = {
                                selectedRepoForActionSheet = repo
                            },
                            onMoreClick = {
                                selectedRepoForActionSheet = repo
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(96.dp)) // Clearance for floating bottom nav
                }
            }
        }
    }

    // Long Press Action Sheet
    selectedRepoForActionSheet?.let { repo ->
        RepoActionBottomSheet(
            repo = repo,
            onDismissRequest = { selectedRepoForActionSheet = null },
            onViewQrClick = { selectedRepoForShareCard = repo },
            onShareLinkClick = { ShareUtils.shareRepoUrl(context, repo) },
            onShareCardClick = { ShareUtils.shareRepoCardImage(context, repo) }
        )
    }

    // QR & Preview Card Bottom Sheet
    selectedRepoForShareCard?.let { repo ->
        RepoQrShareBottomSheet(
            repo = repo,
            onDismissRequest = { selectedRepoForShareCard = null }
        )
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuickActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
