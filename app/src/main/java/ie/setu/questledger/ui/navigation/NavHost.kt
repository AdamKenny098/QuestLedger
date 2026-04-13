package ie.setu.questledger.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.ui.screens.ScreenAbout
import ie.setu.questledger.ui.screens.ScreenCharacterCreate
import ie.setu.questledger.ui.screens.ScreenRoster
import ie.setu.questledger.ui.screens.auth.ScreenLogin
import ie.setu.questledger.ui.screens.auth.ScreenRegister
import ie.setu.questledger.ui.screens.details.ScreenCharacterDetails
import ie.setu.questledger.ui.screens.profile.ScreenProfile
import ie.setu.questledger.ui.screens.roster.RosterViewModel

@Composable
fun QuestLedgerNavHost(
    authService: AuthService
) {
    val navController = rememberNavController()
    val rosterVM: RosterViewModel = hiltViewModel()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val startDestination = if (authService.hasUser) {
        NavRoutes.ROSTER
    } else {
        NavRoutes.LOGIN
    }

    val isAuthScreen = currentRoute == NavRoutes.LOGIN || currentRoute == NavRoutes.REGISTER
    val isDetailScreen = currentRoute == NavRoutes.DETAILS_ROUTE

    val currentLabel = when (currentRoute) {
        NavRoutes.CREATE -> "Create"
        NavRoutes.ABOUT -> "About"
        NavRoutes.PROFILE -> "Profile"
        NavRoutes.DETAILS_ROUTE -> "Details"
        else -> "Roster"
    }

    val canNavigateBack =
        currentRoute != null &&
                currentRoute != NavRoutes.ROSTER &&
                currentRoute != NavRoutes.LOGIN &&
                currentRoute != NavRoutes.REGISTER

    val showDeleteAll = currentRoute == NavRoutes.ROSTER

    Scaffold(
        topBar = {
            if (!isAuthScreen) {
                TopAppBarProvider(
                    currentScreenLabel = currentLabel,
                    canNavigateBack = canNavigateBack,
                    navigateUp = { navController.popBackStack() },
                    showDeleteAll = showDeleteAll,
                    onHelp = { navController.navigate(NavRoutes.ABOUT) },
                    onDeleteAll = { rosterVM.deleteAll() }
                )
            }
        },
        bottomBar = {
            if (!isAuthScreen && !isDetailScreen) {
                QuestLedgerBottomBar(navController)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(NavRoutes.LOGIN) {
                ScreenLogin(
                    onLoginSuccess = {
                        navController.navigate(NavRoutes.ROSTER) {
                            popUpTo(NavRoutes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(NavRoutes.REGISTER)
                    }
                )
            }

            composable(NavRoutes.REGISTER) {
                ScreenRegister(
                    onRegisterSuccess = {
                        navController.navigate(NavRoutes.ROSTER) {
                            popUpTo(NavRoutes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable(NavRoutes.ROSTER) {
                ScreenRoster(
                    onOpenDetails = { id ->
                        navController.navigate("${NavRoutes.DETAILS}/$id")
                    }
                )
            }

            composable(NavRoutes.CREATE) {
                ScreenCharacterCreate()
            }

            composable(NavRoutes.ABOUT) {
                ScreenAbout()
            }

            composable(NavRoutes.PROFILE) {
                ScreenProfile(
                    onSignOut = {
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = NavRoutes.DETAILS_ROUTE,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) {
                ScreenCharacterDetails(
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}