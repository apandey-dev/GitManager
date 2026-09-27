package com.gitmanager.app.ui.screens.search

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gitmanager.app.data.model.GitHubUser
import com.gitmanager.app.ui.components.EmptyStateView
import com.gitmanager.app.ui.components.ErrorStateView
import com.gitmanager.app.ui.components.MonochromeCard
import com.gitmanager.app.ui.components.MonochromeTextField
import com.gitmanager.app.ui.components.MonochromeTopBar
import com.gitmanager.app.ui.components.PaginationLoadingItem

@Composable
fun UserSearchScreen(
    viewModel: SearchViewModel,
    onNavigateToUserProfile: (String) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.searchState.collectAsState()
    val listState = rememberLazyListState()

    val shouldPaginate = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 2
        }
    }

    LaunchedEffect(shouldPaginate.value) {
        if (shouldPaginate.value && state.canPaginate && !state.isPaginating && !state.isLoading) {
            viewModel.performUserSearch(isInitial = false)
        }
    }

    Scaffold(
        topBar = {
            MonochromeTopBar(
                title = "Explore Users",
                subtitle = if (state.totalCount > 0) "${state.totalCount} users found" else "Search GitHub profiles"
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                MonochromeTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = "Search users...",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (state.query.isNotBlank()) {
                            IconButton(
                                onClick = { viewModel.onQueryChange("") },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
            }

            if (state.isLoading && state.users.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            } else if (state.errorMessage != null && state.users.isEmpty()) {
                ErrorStateView(
                    message = state.errorMessage!!,
                    modifier = Modifier.fillMaxSize(),
                    onRetry = { viewModel.performUserSearch(isInitial = true) }
                )
            } else if (state.query.isBlank()) {
                EmptyStateView(
                    title = "Discover GitHub Users",
                    message = "Type a username or query above to find profiles, repositories, and developers.",
                    icon = Icons.Outlined.PersonSearch
                )
            } else if (state.users.isEmpty()) {
                EmptyStateView(
                    title = "No Users Found",
                    message = "No matching GitHub users found for '${state.query}'."
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.users, key = { it.id }) { user ->
                        UserSearchCardItem(
                            user = user,
                            onClick = { onNavigateToUserProfile(user.login) }
                        )
                    }

                    if (state.isPaginating) {
                        item {
                            PaginationLoadingItem()
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(96.dp)) // Clearance for floating bottom nav
                    }
                }
            }
        }
    }
}

@Composable
fun UserSearchCardItem(
    user: GitHubUser,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    MonochromeCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(user.avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.login,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!user.htmlUrl.isNullOrBlank()) {
                    Text(
                        text = user.htmlUrl,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
