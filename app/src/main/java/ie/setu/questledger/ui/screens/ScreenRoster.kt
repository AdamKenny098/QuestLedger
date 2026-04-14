package ie.setu.questledger.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ie.setu.questledger.models.CharacterModel
import ie.setu.questledger.ui.components.general.ShowError
import ie.setu.questledger.ui.screens.roster.RosterSort
import ie.setu.questledger.ui.screens.roster.RosterViewModel

@Composable
fun ScreenRoster(onOpenDetails: (String) -> Unit) {
    val vm: RosterViewModel = hiltViewModel()

    val characters = vm.uiCharacters.collectAsState().value
    val query = vm.query.collectAsState().value
    val error = vm.error.collectAsState().value

    var sortExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (error != null) {
                ShowError(
                    headline = "Load Error",
                    subtitle = error,
                    onClick = { vm.getCharacters() }
                )
                return@Column
            }

            Text("Character Roster", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { vm.onQueryChange(it) },
                    label = { Text("Search (name, race, class)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { sortExpanded = true }) {
                    Icon(Icons.Filled.Sort, contentDescription = "Sort")
                }

                DropdownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Name (A–Z)") },
                        onClick = {
                            sortExpanded = false
                            vm.onSortChange(RosterSort.NAME_ASC)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Level (High–Low)") },
                        onClick = {
                            sortExpanded = false
                            vm.onSortChange(RosterSort.LEVEL_DESC)
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (characters.isEmpty()) {
                val msg =
                    if (query.isBlank()) "No characters yet. Create one."
                    else "No results for \"$query\""

                Text(msg, style = MaterialTheme.typography.bodyLarge)
                return@Column
            }

            LazyColumn {
                items(
                    items = characters,
                    key = { it.id }
                ) { c ->
                    CharacterCard(
                        c = c,
                        onDelete = { vm.deleteCharacter(c) },
                        onOpenDetails = onOpenDetails
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            FilledTonalButton(
                onClick = { vm.deleteAll() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete All")
            }
        }
    }
}

@Composable
private fun CharacterCard(
    c: CharacterModel,
    onDelete: () -> Unit,
    onOpenDetails: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        if (c.imageUri.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(c.imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Character Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.height(8.dp))
        }

        Text(
            text = "${c.name} — ${c.race} ${c.characterClass} (Lv ${c.level})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenDetails(c.id) }
                .padding(vertical = 4.dp)
        )

        if (expanded) {
            if (c.notes.isNotBlank()) {
                Text(
                    text = "Notes: ${c.notes}",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                Spacer(Modifier.height(16.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilledTonalButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Show Less" else "Show More")
            }

            if (expanded) {
                FilledTonalIconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Character")
                }
            }
        }
    }
}