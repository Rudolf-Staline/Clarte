package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.*
import com.example.viewmodel.JournalViewModel

import com.example.auth.AuthViewModel
import com.example.sync.SyncManager
import com.example.ui.auth.AccountScreen

object AppDestinations {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val CHOOSE_MODE = "choose_mode?mood={mood}"
    const val NEW_ENTRY = "new_entry/{writingMode}?mood={mood}"
    const val REFLECTION = "reflection/{entryId}"
    const val HISTORY = "history"
    const val DETAIL = "detail/{entryId}"
    const val TRENDS = "trends"
    const val SETTINGS = "settings"
    const val ACCOUNT = "account"
    const val PRIVACY_CENTER = "privacy_center"
    const val CALENDAR = "calendar"
    const val DAY_DETAIL = "day_detail/{dateStr}"

    fun chooseModeWithMood(mood: String?) = if (mood != null) "choose_mode?mood=$mood" else "choose_mode?mood="
    fun newEntryWithMode(writingMode: String, mood: String?) = "new_entry/$writingMode" + (if (!mood.isNullOrBlank()) "?mood=$mood" else "")
    fun reflection(entryId: Long) = "reflection/$entryId"
    fun detail(entryId: Long) = "detail/$entryId"
    fun dayDetail(dateStr: String) = "day_detail/$dateStr"
}

@Composable
fun AppNavigation(
    viewModel: JournalViewModel,
    authViewModel: AuthViewModel,
    syncManager: SyncManager
) {
    val navController = rememberNavController()
    val hasCompletedOnboarding = viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle().value

    // Reusable bottom tab navigation logic with state saving and singleTop launching
    val onNavigateTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (hasCompletedOnboarding) AppDestinations.HOME else AppDestinations.ONBOARDING
    ) {
        // ONBOARDING
        composable(AppDestinations.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    viewModel.setHasCompletedOnboarding(true)
                    navController.navigate(AppDestinations.HOME) {
                        popUpTo(AppDestinations.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // HOME SCREEN
        composable(AppDestinations.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToNewEntry = { mood ->
                    navController.navigate(AppDestinations.chooseModeWithMood(mood))
                },
                onNavigateToDetail = { entryId ->
                    navController.navigate(AppDestinations.detail(entryId))
                },
                onNavigateTab = onNavigateTab
            )
        }

        // CHOOSE MODE SCREEN
        composable(
            route = AppDestinations.CHOOSE_MODE,
            arguments = listOf(
                navArgument("mood") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val mood = backStackEntry.arguments?.getString("mood")?.takeIf { it.isNotBlank() }
            ChooseModeScreen(
                onNavigateToNewEntry = { writingMode ->
                    navController.navigate(AppDestinations.newEntryWithMode(writingMode, mood))
                },
                onNavigateTab = onNavigateTab
            )
        }

        // NEW ENTRY SCREEN
        composable(
            route = AppDestinations.NEW_ENTRY,
            arguments = listOf(
                navArgument("writingMode") {
                    type = NavType.StringType
                    defaultValue = "Journal libre"
                },
                navArgument("mood") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val writingMode = backStackEntry.arguments?.getString("writingMode") ?: "Journal libre"
            val mood = backStackEntry.arguments?.getString("mood")
            
            NewEntryScreen(
                viewModel = viewModel,
                writingMode = writingMode,
                initialMood = mood,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToReflection = { entryId ->
                    // Navigate to AI analyze screen and pop the writer so they can't resubmit
                    navController.navigate(AppDestinations.reflection(entryId)) {
                        popUpTo(AppDestinations.HOME)
                    }
                }
            )
        }

        // REFLECTION SCREEN
        composable(
            route = AppDestinations.REFLECTION,
            arguments = listOf(
                navArgument("entryId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
            ReflectionScreen(
                viewModel = viewModel,
                entryId = entryId,
                onNavigateHome = {
                    navController.navigate(AppDestinations.HOME) {
                        popUpTo(AppDestinations.HOME) { inclusive = true }
                    }
                }
            )
        }

        // HISTORY SCREEN
        composable(AppDestinations.HISTORY) {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateToDetail = { entryId ->
                    navController.navigate(AppDestinations.detail(entryId))
                },
                onNavigateTab = onNavigateTab
            )
        }

        // ENTRY DETAILS SCREEN
        composable(
            route = AppDestinations.DETAIL,
            arguments = listOf(
                navArgument("entryId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
            EntryDetailScreen(
                viewModel = viewModel,
                entryId = entryId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // TENDANCES (TRENDS) SCREEN
        composable(AppDestinations.TRENDS) {
            TrendsScreen(
                viewModel = viewModel,
                onNavigateTab = onNavigateTab,
                onNavigateToCalendar = { navController.navigate(AppDestinations.CALENDAR) }
            )
        }

        // CALENDAR SCREEN
        composable(AppDestinations.CALENDAR) {
            CalendarScreen(
                viewModel = viewModel,
                onNavigateTab = onNavigateTab,
                onNavigateToDay = { dateStr -> navController.navigate(AppDestinations.dayDetail(dateStr)) }
            )
        }

        // DAY DETAIL SCREEN
        composable(
            route = AppDestinations.DAY_DETAIL,
            arguments = listOf(
                navArgument("dateStr") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dateStr = backStackEntry.arguments?.getString("dateStr") ?: ""
            DayDetailScreen(
                viewModel = viewModel,
                dateStr = dateStr,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { entryId -> navController.navigate(AppDestinations.detail(entryId)) }
            )
        }

        // SETTINGS SCREEN
        composable(AppDestinations.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                authViewModel = authViewModel,
                syncManager = syncManager,
                onNavigateTab = onNavigateTab,
                onNavigateToAccount = { navController.navigate(AppDestinations.ACCOUNT) },
                onNavigateToPrivacyCenter = { navController.navigate(AppDestinations.PRIVACY_CENTER) }
            )
        }

        // PRIVACY CENTER SCREEN
        composable(AppDestinations.PRIVACY_CENTER) {
            PrivacyCenterScreen(
                onNavigateBack = { navController.popBackStack() },
                onReviewOnboarding = {
                    viewModel.setHasCompletedOnboarding(false)
                    navController.navigate(AppDestinations.ONBOARDING) {
                        popUpTo(0)
                    }
                }
            )
        }

        // ACCOUNT SCREEN
        composable(AppDestinations.ACCOUNT) {
            AccountScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { 
                    navController.navigate(AppDestinations.SETTINGS) {
                        popUpTo(AppDestinations.HOME)
                    }
                }
            )
        }
    }
}
