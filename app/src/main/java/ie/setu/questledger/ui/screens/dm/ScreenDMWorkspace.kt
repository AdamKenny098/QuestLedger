package ie.setu.questledger.ui.screens.dm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ScreenDMWorkspace(
    onOpenCampaignEditor: () -> Unit,
    onOpenQuestEditor: () -> Unit,
    onOpenNpcEditor: () -> Unit,
    onOpenPlaceEditor: () -> Unit,
    vm: DMWorkspaceViewModel = hiltViewModel()
) {
    val campaigns by vm.campaigns.collectAsState()
    val quests by vm.quests.collectAsState()
    val npcs by vm.npcs.collectAsState()
    val places by vm.places.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.padding(16.dp)
        ) {
            item {
                Text("DM Workspace", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Campaign planning and world management tools.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onOpenCampaignEditor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Campaign")
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onOpenQuestEditor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Quest")
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onOpenNpcEditor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add NPC")
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onOpenPlaceEditor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Place")
                }

                Spacer(Modifier.height(20.dp))

                Text("Campaigns", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            items(campaigns, key = { it.id }) { campaign ->
                DMItemCard(
                    title = campaign.title,
                    subtitle = "${campaign.setting} • Sessions ${campaign.sessionCount}",
                    body = campaign.summary,
                    onDelete = { vm.deleteCampaign(campaign.id) }
                )
                Spacer(Modifier.height(12.dp))
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text("Quests", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            items(quests, key = { it.id }) { quest ->
                DMItemCard(
                    title = quest.title,
                    subtitle = quest.status,
                    body = quest.summary,
                    onDelete = { vm.deleteQuest(quest.id) }
                )
                Spacer(Modifier.height(12.dp))
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text("NPCs", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            items(npcs, key = { it.id }) { npc ->
                DMItemCard(
                    title = npc.name,
                    subtitle = "${npc.role} • ${npc.faction}",
                    body = npc.notes,
                    onDelete = { vm.deleteNpc(npc.id) }
                )
                Spacer(Modifier.height(12.dp))
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text("Places", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            items(places, key = { it.id }) { place ->
                DMItemCard(
                    title = place.name,
                    subtitle = place.region,
                    body = place.description,
                    onDelete = { vm.deletePlace(place.id) }
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun DMItemCard(
    title: String,
    subtitle: String,
    body: String,
    onDelete: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (body.isBlank()) "No details." else body,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        }
    }
}