package ie.setu.questledger.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.ui.graphics.vector.ImageVector

interface AppDestination {
    val icon: ImageVector
    val label: String
    val route: String
}

object Create : AppDestination {
    override val icon = Icons.Filled.PersonAdd
    override val label = "Create"
    override val route = NavRoutes.CREATE
}

object Roster : AppDestination {
    override val icon = Icons.Filled.List
    override val label = "Roster"
    override val route = NavRoutes.ROSTER
}

object About : AppDestination {
    override val icon = Icons.Filled.Info
    override val label = "About"
    override val route = NavRoutes.ABOUT
}

val bottomNavDestinations = listOf(Create, Roster, About)
