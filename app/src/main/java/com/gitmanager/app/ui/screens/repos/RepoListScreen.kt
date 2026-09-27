package com.gitmanager.app.ui.screens.repos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gitmanager.app.core.theme.MonoDangerMuted
import com.gitmanager.app.core.utils.ShareUtils
import com.gitmanager.app.data.model.GitHubRepo
import com.gitmanager.app.ui.components.CreateEditRepoBottomSheet
import com.gitmanager.app.ui.components.DeleteRepoBottomSheet
import com.gitmanager.app.ui.components.EmptyStateView
import com.gitmanager.app.ui.components.ErrorStateView
import com.gitmanager.app.ui.components.MonochromePrimaryButton
import com.gitmanager.app.ui.components.MonochromeTextField
import com.gitmanager.app.ui.components.MonochromeTopBar
import com.gitmanager.app.ui.components.PaginationLoadingItem
import com.gitmanager.app.ui.components.RepoActionBottomSheet
import com.gitmanager.app.ui.components.RepoCardItem
import com.gitmanager.app.ui.components.RepoQrShareBottomSheet

@Composable
fun RepoListScreen(
    viewModel: RepoViewModel,
    onNavigateToRepoDetail: (String, String) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.listState.collectAsState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedRepoForActionSheet by remember { mutableStateOf<GitHubRepo?>(null) }
    var selectedRepoForShareCard by remember { mutableStateOf<GitHubRepo?>(null) }
    var repoToEdit by remember { mutableStateOf<GitHubRepo?>(null) }
    var repoToDelete by remember { mutableStateOf<GitHubRepo?>(null) }
    var deleteConfirmationInput by remember { mutableStateOf("") }

    // Toast/Snackbar notifications
    LaunchedEffect(state.actionSuccessMessage, state.errorMessage) {
        state.actionSuccessMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    // Pagination detection
    val shouldPaginate = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 2
        }
    }

    LaunchedEffect(shouldPaginate.value) {
        if (shouldPaginate.value && state.canPaginate && !state.isPaginating && !state.isLoading) {
            viewModel.loadRepositories(isInitial = false)
        }
    }

    Scaffold(
        topBar = {
            MonochromeTopBar(
                title = "Repositories",
                subtitle = "${state.filteredRepos.size} repositories"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 72.dp) // Offset above floating bottom nav
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "New Repository"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search & Filter Tabs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                MonochromeTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = "Search repositories...",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = { viewModel.onSearchQueryChanged("") },
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

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Tabs (All, Public, Private, Forks)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RepoFilter.values().forEach { filter ->
                        val isSelected = state.activeFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                                .clickable { viewModel.setFilter(filter) }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Repos List Content
            if (state.isLoading && state.repos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            } else if (state.errorMessage != null && state.repos.isEmpty()) {
                ErrorStateView(
                    message = state.errorMessage!!,
                    modifier = Modifier.fillMaxSize(),
                    onRetry = { viewModel.loadRepositories(isInitial = true) }
                )
            } else if (state.filteredRepos.isEmpty()) {
                EmptyStateView(
                    title = "No Repositories Found",
                    message = if (state.searchQuery.isNotBlank()) "No repositories match your filter query." else "You don't have any repositories in this view.",
                    icon = Icons.Outlined.Code
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.filteredRepos, key = { it.id }) { repo ->
                        RepoCardItem(
                            repo = repo,
                            onClick = {
                                val owner = repo.owner?.login ?: ""
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

                    if (state.isPaginating) {
                        item {
                            PaginationLoadingItem()
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(96.dp)) // Clearance for floating pill bottom nav
                    }
                }
            }
        }
    }

    // Long Press / More Actions Bottom Sheet
    selectedRepoForActionSheet?.let { repo ->
        RepoActionBottomSheet(
            repo = repo,
            onDismissRequest = { selectedRepoForActionSheet = null },
            onViewQrClick = { selectedRepoForShareCard = repo },
            onShareLinkClick = { ShareUtils.shareRepoUrl(context, repo) },
            onShareCardClick = { ShareUtils.shareRepoCardImage(context, repo) },
            onEditClick = { repoToEdit = repo },
            onDeleteClick = { repoToDelete = repo }
        )
    }

    // QR & Preview Card Bottom Sheet
    selectedRepoForShareCard?.let { repo ->
        RepoQrShareBottomSheet(
            repo = repo,
            onDismissRequest = { selectedRepoForShareCard = null }
        )
    }

    // Create Repo Bottom Sheet
    if (showCreateDialog) {
        CreateEditRepoBottomSheet(
            onDismissRequest = { showCreateDialog = false },
            onConfirm = { name, description, isPrivate ->
                showCreateDialog = false
                viewModel.createRepository(name, description, isPrivate) { createdRepo ->
                    val owner = createdRepo.owner?.login ?: ""
                    onNavigateToRepoDetail(owner, createdRepo.name)
                }
            }
        )
    }

    // Edit Repo Bottom Sheet
    repoToEdit?.let { repo ->
        CreateEditRepoBottomSheet(
            initialRepo = repo,
            onDismissRequest = { repoToEdit = null },
            onConfirm = { name, desc, isPrivate ->
                repoToEdit = null
                viewModel.updateRepository(
                    owner = repo.owner?.login ?: "",
                    repo = repo.name,
                    name = name,
                    description = desc,
                    isPrivate = isPrivate
                )
            }
        )
    }

    // Safe Delete Confirmation Bottom Sheet
    repoToDelete?.let { repo ->
        DeleteRepoBottomSheet(
            repo = repo,
            onDismissRequest = { repoToDelete = null },
            onConfirmDelete = {
                val owner = repo.owner?.login ?: ""
                val name = repo.name
                repoToDelete = null
                viewModel.deleteRepository(owner, name) {}
            }
        )
    }
}
