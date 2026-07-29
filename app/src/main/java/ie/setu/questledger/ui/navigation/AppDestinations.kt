package ie.setu.questledger.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

interface AppDestination {
    val icon: ImageVector
    val label: String
    val route: String
}

object Roster : AppDestination {
    override val icon = Icons.Filled.Groups
    override val label = "Characters"
    override val route = NavRoutes.ROSTER
}

object Create : AppDestination {
    override val icon = Icons.Filled.AddCircle
    override val label = "Create"
    override val route = NavRoutes.CREATE
}

object Dice : AppDestination {
    override val icon = Icons.Filled.Casino
    override val label = "Dice"
    override val route = "dice"
}

object DMWorkspace : AppDestination {
    override val icon = Icons.Filled.AutoStories
    override val label = "DM"
    override val route = "dm_workspace"
}

object Profile : AppDestination {
    override val icon = Icons.Filled.Person
    override val label = "Profile"
    override val route = NavRoutes.PROFILE
}

object About : AppDestination {
    override val icon = Icons.Filled.Info
    override val label = "About"
    override val route = NavRoutes.ABOUT
}

object CampaignMap : AppDestination {
    override val icon = Icons.Filled.Map
    override val label = "Map"
    override val route = "campaign_map"
}

object QuickSetupCharacter : AppDestination {
    override val icon = Icons.Filled.Add
    override val label = "Quick Setup"
    override val route = "quick_setup_character"
}

object PremadeCharacters : AppDestination {
    override val icon = Icons.Filled.Add
    override val label = "Premade"
    override val route = "premade_characters"
}

object FullSetupCharacter : AppDestination {
    override val icon = Icons.Filled.Add
    override val label = "Full Setup"
    override val route = "full_setup_character"
}

object CharacterSpellbook : AppDestination {
    override val icon = Icons.Filled.MenuBook
    override val label = "Spellbook"
    override val route = "character_spellbook/{id}"

    fun createRoute(id: String): String = "character_spellbook/$id"
}

object CharacterEdit : AppDestination {
    override val icon = Icons.Filled.Edit
    override val label = "Edit"
    override val route = "character_edit/{id}"

    fun createRoute(id: String): String = "character_edit/$id"
}

object CharacterDetails : AppDestination {
    override val icon = Icons.Filled.Info
    override val label = "Details"
    override val route = "details/{id}"

    fun createRoute(id: String): String = "details/$id"
}

object DMCampaignEditor : AppDestination {
    override val icon = Icons.Filled.AutoStories
    override val label = "Campaign Editor"
    override val route = "dm_campaign_editor?campaignId={campaignId}"

    fun createRoute(campaignId: String? = null): String {
        return if (campaignId.isNullOrBlank()) {
            "dm_campaign_editor"
        } else {
            "dm_campaign_editor?campaignId=$campaignId"
        }
    }
}

object DMQuestEditor : AppDestination {
    override val icon = Icons.Filled.AutoStories
    override val label = "Quest Editor"
    override val route = "dm_quest_editor?questId={questId}"

    fun createRoute(questId: String? = null): String {
        return if (questId.isNullOrBlank()) {
            "dm_quest_editor"
        } else {
            "dm_quest_editor?questId=$questId"
        }
    }
}

object DMNpcEditor : AppDestination {
    override val icon = Icons.Filled.AutoStories
    override val label = "NPC Editor"
    override val route = "dm_npc_editor?npcId={npcId}"

    fun createRoute(npcId: String? = null): String {
        return if (npcId.isNullOrBlank()) {
            "dm_npc_editor"
        } else {
            "dm_npc_editor?npcId=$npcId"
        }
    }
}

object DMPlaceEditor : AppDestination {
    override val icon = Icons.Filled.AutoStories
    override val label = "Place Editor"
    override val route = "dm_place_editor?placeId={placeId}"

    fun createRoute(placeId: String? = null): String {
        return if (placeId.isNullOrBlank()) {
            "dm_place_editor"
        } else {
            "dm_place_editor?placeId=$placeId"
        }
    }
}

val bottomNavDestinations = listOf(Roster, Create, Dice, DMWorkspace, Profile)
