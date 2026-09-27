package com.gitmanager.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gitmanager.app.data.repository.AuthRepository
import com.gitmanager.app.data.repository.GitHubRepository
import com.gitmanager.app.ui.components.MonochromeBottomNav
import com.gitmanager.app.ui.screens.auth.AuthViewModel
import com.gitmanager.app.ui.screens.auth.LoginScreen
import com.gitmanager.app.ui.components.CreateEditRepoBottomSheet
import com.gitmanager.app.ui.screens.dashboard.DashboardScreen
import com.gitmanager.app.ui.screens.dashboard.DashboardViewModel
import com.gitmanager.app.ui.screens.repos.RepoDetailScreen
import com.gitmanager.app.ui.screens.repos.RepoListScreen
import com.gitmanager.app.ui.screens.repos.RepoViewModel
import com.gitmanager.app.ui.screens.search.SearchViewModel
import com.gitmanager.app.ui.screens.search.UserProfileDetailScreen
import com.gitmanager.app.ui.screens.search.UserSearchScreen
import com.gitmanager.app.ui.screens.settings.SettingsScreen

@Composable
fun AppNavHost(
    authRepository: AuthRepository,
    gitHubRepository: GitHubRepository,
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = remember { AuthViewModel(authRepository) },
    repoViewModel: RepoViewModel = remember { RepoViewModel(gitHubRepository) },
    dashboardViewModel: DashboardViewModel = remember { DashboardViewModel(gitHubRepository) },
    searchViewModel: SearchViewModel = remember { SearchViewModel(gitHubRepository) }
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelRoute = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Repositories.route,
        Screen.UserSearch.route,
        Screen.Settings.route
    )

    var showGlobalCreateRepoDialog by remember { mutableStateOf(false) }

    val startDestination = if (authRepository.isLoggedIn()) Screen.Dashboard.route else Screen.Login.route

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            // Auth Screen
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        dashboardViewModel.loadDashboardData()
                        repoViewModel.loadRepositories(isInitial = true)
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Dashboard Screen
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToRepos = {
                        navController.navigate(Screen.Repositories.route)
                    },
                    onNavigateToRepoDetail = { owner, repo ->
                        navController.navigate(Screen.RepoDetail.createRoute(owner, repo))
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.UserSearch.route)
                    },
                    onCreateRepoClick = { showGlobalCreateRepoDialog = true }
                )
            }

            // Repositories Screen
            composable(Screen.Repositories.route) {
                RepoListScreen(
                    viewModel = repoViewModel,
                    onNavigateToRepoDetail = { owner, repo ->
                        navController.navigate(Screen.RepoDetail.createRoute(owner, repo))
                    }
                )
            }

            // Repo Detail Screen
            composable(
                route = Screen.RepoDetail.route,
                arguments = listOf(
                    navArgument("owner") { type = NavType.StringType },
                    navArgument("repo") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val owner = backStackEntry.arguments?.getString("owner") ?: ""
                val repo = backStackEntry.arguments?.getString("repo") ?: ""
                RepoDetailScreen(
                    owner = owner,
                    repoName = repo,
                    viewModel = repoViewModel,
                    currentUsername = authRepository.getCachedUser()?.login,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Explore Users Screen
            composable(Screen.UserSearch.route) {
                UserSearchScreen(
                    viewModel = searchViewModel,
                    onNavigateToUserProfile = { username ->
                        navController.navigate(Screen.UserProfile.createRoute(username))
                    }
                )
            }

            // User Profile Detail Screen
            composable(
                route = Screen.UserProfile.route,
                arguments = listOf(
                    navArgument("username") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: ""
                UserProfileDetailScreen(
                    username = username,
                    viewModel = searchViewModel,
                    onNavigateToRepoDetail = { owner, repo ->
                        navController.navigate(Screen.RepoDetail.createRoute(owner, repo))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Settings Screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                    authRepository = authRepository,
                    onLogoutClick = {
                        authViewModel.resetState()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Modern Full-width Bottom Navigation Bar (Rendered at BottomCenter)
        if (isTopLevelRoute) {
            MonochromeBottomNav(
                currentRoute = currentRoute,
                onNavigate = { targetRoute ->
                    if (targetRoute != currentRoute) {
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showGlobalCreateRepoDialog) {
        CreateEditRepoBottomSheet(
            onDismissRequest = { showGlobalCreateRepoDialog = false },
            onConfirm = { name, desc, isPrivate ->
                showGlobalCreateRepoDialog = false
                repoViewModel.createRepository(name, desc, isPrivate) { created ->
                    val owner = created.owner?.login ?: ""
                    navController.navigate(Screen.RepoDetail.createRoute(owner, created.name))
                }
            }
        )
    }
}
