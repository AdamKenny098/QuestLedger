package ie.setu.questledger.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import ie.setu.questledger.ui.screens.fullsetup.ScreenFullSetup
import ie.setu.questledger.ui.screens.map.CampaignMapScreen
import ie.setu.questledger.ui.screens.profile.ScreenProfile
import ie.setu.questledger.ui.screens.quicksetup.ScreenQuickSetupCharacter
import ie.setu.questledger.ui.screens.premade.ScreenPremadeCharacters
import ie.setu.questledger.ui.screens.ScreenDiceRoller
import ie.setu.questledger.ui.screens.spellbook.ScreenCharacterSpellbook
import ie.setu.questledger.ui.screens.details.ScreenCharacterEdit
import ie.setu.questledger.ui.screens.dm.ScreenDMWorkspace
import ie.setu.questledger.ui.screens.dm.ScreenDMCampaignEditor
import ie.setu.questledger.ui.screens.dm.ScreenDMQuestEditor
import ie.setu.questledger.ui.screens.dm.ScreenDMNpcEditor
import ie.setu.questledger.ui.screens.dm.ScreenDMPlaceEditor

@Composable
fun QuestLedgerNavHost(
    authService: AuthService
) {
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val startDestination = if (authService.hasUser) {
        NavRoutes.ROSTER
    } else {
        NavRoutes.LOGIN
    }

    val isAuthScreen = currentRoute == NavRoutes.LOGIN || currentRoute == NavRoutes.REGISTER
    val topLevelRoutes = bottomNavDestinations.map { it.route }.toSet()
    val showBottomBar = currentRoute in topLevelRoutes

    val currentLabel = when (currentRoute) {
        NavRoutes.ROSTER -> "Characters"
        NavRoutes.CREATE -> "Create Character"
        NavRoutes.ABOUT -> "About"
        NavRoutes.PROFILE -> "Profile"
        CharacterDetails.route -> "Character Sheet"
        CharacterEdit.route -> "Edit Character"
        CharacterSpellbook.route -> "Spellbook"
        QuickSetupCharacter.route -> "Quick Setup"
        PremadeCharacters.route -> "Premade Heroes"
        FullSetupCharacter.route -> "Full Setup"
        Dice.route -> "Dice Roller"
        DMWorkspace.route -> "Dungeon Master"
        CampaignMap.route -> "Campaign Map"
        DMCampaignEditor.route -> "Campaign Editor"
        DMQuestEditor.route -> "Quest Editor"
        DMNpcEditor.route -> "NPC Editor"
        DMPlaceEditor.route -> "Place Editor"
        else -> "QuestLedger"
    }

    val canNavigateBack =
        currentRoute != null &&
                currentRoute !in topLevelRoutes &&
                !isAuthScreen

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (!isAuthScreen) {
                TopAppBarProvider(
                    currentScreenLabel = currentLabel,
                    canNavigateBack = canNavigateBack,
                    navigateUp = { navController.popBackStack() },
                    onHelp = { navController.navigate(NavRoutes.ABOUT) }
                )
            }
        },
        bottomBar = {
            if (!isAuthScreen && showBottomBar) {
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
                        navController.navigate(CharacterDetails.createRoute(id))
                    },
                    onOpenEdit = { id ->
                        navController.navigate(CharacterEdit.createRoute(id))
                    },
                    onOpenAbout = {
                        navController.navigate((About.route))
                    },
                    onCreateCharacter = {
                        navController.navigate(NavRoutes.CREATE)
                    }
                )
            }

            composable(NavRoutes.CREATE) {
                ScreenCharacterCreate(
                    onOpenQuickSetup = {
                        navController.navigate(QuickSetupCharacter.route)
                    },
                    onOpenPremade = {
                        navController.navigate(PremadeCharacters.route)
                    },
                    onOpenFullSetup = {
                        navController.navigate(FullSetupCharacter.route)
                    }
                )

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

            composable(route = CampaignMap.route) {
                CampaignMapScreen()
            }

            composable(route = QuickSetupCharacter.route) {
                ScreenQuickSetupCharacter(
                    onDone = { navController.popBackStack() },
                    onOpenManualSetup = { navController.popBackStack() }
                )
            }

            composable(route = PremadeCharacters.route) {
                ScreenPremadeCharacters(
                    onDone = { navController.popBackStack() },
                    onOpenManualSetup = { navController.popBackStack() }
                )
            }

            composable(route = FullSetupCharacter.route) {
                ScreenFullSetup(
                    onDone = { navController.popBackStack() },
                    onOpenManualSetup = { navController.popBackStack() }
                )
            }

            composable(
                route = CharacterDetails.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) {
                ScreenCharacterDetails(
                    onOpenSpellbook = { id ->
                        navController.navigate(CharacterSpellbook.createRoute(id))
                    }
                )
            }

            composable(
                route = CharacterEdit.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) {
                ScreenCharacterEdit(
                    onDone = { navController.popBackStack() },
                    onOpenSpellbook = { id ->
                        navController.navigate(CharacterSpellbook.createRoute(id))
                    }
                )
            }

            composable(route = Dice.route) {
                ScreenDiceRoller()
            }

            composable(
                route = CharacterSpellbook.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) {
                ScreenCharacterSpellbook(
                    onDone = { navController.popBackStack() }
                )
            }

            composable(route = DMWorkspace.route) {
                ScreenDMWorkspace(
                    onOpenCampaignEditor = { campaignId ->
                        navController.navigate(DMCampaignEditor.createRoute(campaignId))
                    },
                    onOpenQuestEditor = { questId ->
                        navController.navigate(DMQuestEditor.createRoute(questId))
                    },
                    onOpenNpcEditor = { npcId ->
                        navController.navigate(DMNpcEditor.createRoute(npcId))
                    },
                    onOpenPlaceEditor = { placeId ->
                        navController.navigate(DMPlaceEditor.createRoute(placeId))
                    },
                    onOpenMap = {
                        navController.navigate(CampaignMap.route)
                    }
                )
            }

            composable(
                route = DMCampaignEditor.route,
                arguments = listOf(
                    navArgument("campaignId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                ScreenDMCampaignEditor(
                    onDone = { navController.popBackStack() }
                )
            }

            composable(
                route = DMQuestEditor.route,
                arguments = listOf(
                    navArgument("questId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                ScreenDMQuestEditor(
                    onDone = { navController.popBackStack() }
                )
            }

            composable(
                route = DMNpcEditor.route,
                arguments = listOf(
                    navArgument("npcId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                ScreenDMNpcEditor(
                    onDone = { navController.popBackStack() }
                )
            }

            composable(
                route = DMPlaceEditor.route,
                arguments = listOf(
                    navArgument("placeId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                ScreenDMPlaceEditor(
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}
