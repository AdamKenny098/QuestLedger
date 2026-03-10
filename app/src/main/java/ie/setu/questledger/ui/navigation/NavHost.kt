package ie.setu.questledger.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ie.setu.questledger.ui.screens.ScreenAbout
import ie.setu.questledger.ui.screens.ScreenCharacterCreate
import ie.setu.questledger.ui.screens.ScreenRoster
import androidx.hilt.navigation.compose.hiltViewModel
import ie.setu.questledger.ui.screens.roster.RosterViewModel
import androidx.navigation.navArgument
import androidx.navigation.NavType
import ie.setu.questledger.ui.screens.details.ScreenCharacterDetails



@Composable
fun QuestLedgerNavHost() {
    val navController = rememberNavController()

    val rosterVM: RosterViewModel = hiltViewModel()



    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val canNavigateBack = currentRoute != null && currentRoute != NavRoutes.ROSTER

    val currentLabel = when (currentRoute) {
        NavRoutes.CREATE -> "Create"
        NavRoutes.ABOUT -> "About"
        else -> "Roster"
    }

    val showDeleteAll = currentRoute == NavRoutes.ROSTER

    Scaffold(
        topBar = {
            TopAppBarProvider(
                currentScreenLabel = currentLabel,
                canNavigateBack = canNavigateBack,
                navigateUp = {
                    navController.navigate(NavRoutes.ROSTER) {
                        launchSingleTop = true
                        popUpTo(NavRoutes.ROSTER) { inclusive = false }
                    }
                },
                showDeleteAll = showDeleteAll,
                onHelp = { navController.navigate(NavRoutes.ABOUT) },
                onDeleteAll = { rosterVM.deleteAll() }
            )
        },
        bottomBar = { QuestLedgerBottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.ROSTER,
            modifier = Modifier.padding(padding)
        ) {
            composable(NavRoutes.ROSTER) {
                ScreenRoster(
                    onOpenDetails = { id -> navController.navigate("details/$id") }
                )
            }
            composable(NavRoutes.CREATE) { ScreenCharacterCreate() }
            composable(NavRoutes.ABOUT) { ScreenAbout() }
            composable(
                route = "details/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) {
                ScreenCharacterDetails(
                    onDone = { navController.popBackStack() }
                )
            }


        }
    }
}
