package ie.setu.questledger.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ie.setu.questledger.data.compendium.CompendiumLookup
import ie.setu.questledger.data.rules.CharacterStatEngine
import ie.setu.questledger.models.characters.CharacterModel
import ie.setu.questledger.ui.components.general.ShowError
import ie.setu.questledger.ui.components.ledger.LedgerBadge
import ie.setu.questledger.ui.components.ledger.LedgerEmptyState
import ie.setu.questledger.ui.components.ledger.LedgerPanel
import ie.setu.questledger.ui.components.ledger.LedgerScreenIntro
import ie.setu.questledger.ui.components.ledger.LedgerStatTile
import ie.setu.questledger.ui.screens.roster.RosterSort
import ie.setu.questledger.ui.screens.roster.RosterViewModel

@Composable
fun ScreenRoster(
    onOpenDetails: (String) -> Unit,
    onOpenEdit: (String) -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onCreateCharacter: () -> Unit = {}
) {
    val vm: RosterViewModel = hiltViewModel()
    val characters by vm.uiCharacters.collectAsState()
    val query by vm.query.collectAsState()
    val error by vm.error.collectAsState()

    var sortExpanded by remember { mutableStateOf(false) }
    var actionsExpanded by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Delete every character?") },
            text = {
                Text(
                    "This removes the entire roster. This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAllDialog = false
                        vm.deleteAll()
                    }
                ) {
                    Text(
                        text = "Delete all",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (error != null) {
            Column(modifier = Modifier.padding(16.dp)) {
                ShowError(
                    headline = "Could not load your roster",
                    subtitle = error.orEmpty(),
                    onClick = vm::getCharacters
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 18.dp,
                    end = 16.dp,
                    bottom = 28.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    LedgerScreenIntro(
                        eyebrow = "Your adventuring party",
                        title = "Characters",
                        body = "Open a sheet, check the essentials, or prepare a new hero."
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = vm::onQueryChange,
                            placeholder = { Text("Search characters") },
                            leadingIcon = {
                                Icon(Icons.Filled.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (query.isNotBlank()) {
                                    IconButton(onClick = { vm.onQueryChange("") }) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Clear search"
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.weight(1f)
                        )

                        Box {
                            FilledTonalIconButton(
                                onClick = { sortExpanded = true }
                            ) {
                                Icon(Icons.Filled.Sort, contentDescription = "Sort")
                            }

                            DropdownMenu(
                                expanded = sortExpanded,
                                onDismissRequest = { sortExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Name: A–Z") },
                                    onClick = {
                                        sortExpanded = false
                                        vm.onSortChange(RosterSort.NAME_ASC)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Highest level") },
                                    onClick = {
                                        sortExpanded = false
                                        vm.onSortChange(RosterSort.LEVEL_DESC)
                                    }
                                )
                            }
                        }

                        Box {
                            IconButton(onClick = { actionsExpanded = true }) {
                                Icon(
                                    Icons.Filled.MoreVert,
                                    contentDescription = "Roster actions"
                                )
                            }

                            DropdownMenu(
                                expanded = actionsExpanded,
                                onDismissRequest = { actionsExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("About QuestLedger") },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Info, contentDescription = null)
                                    },
                                    onClick = {
                                        actionsExpanded = false
                                        onOpenAbout()
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Delete all characters",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.DeleteOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        actionsExpanded = false
                                        showDeleteAllDialog = true
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = when {
                            query.isNotBlank() -> "${characters.size} matching characters"
                            characters.size == 1 -> "1 character"
                            else -> "${characters.size} characters"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (characters.isEmpty()) {
                    item {
                        LedgerEmptyState(
                            title = if (query.isBlank()) {
                                "Your ledger is empty"
                            } else {
                                "No matching heroes"
                            },
                            body = if (query.isBlank()) {
                                "Create your first character and their essential stats will appear here."
                            } else {
                                "Try a different name, ancestry, class, or subclass."
                            },
                            actionLabel = if (query.isBlank()) {
                                "Create a character"
                            } else {
                                "Clear search"
                            },
                            onAction = {
                                if (query.isBlank()) onCreateCharacter()
                                else vm.onQueryChange("")
                            }
                        )
                    }
                } else {
                    items(
                        items = characters,
                        key = { it.id }
                    ) { character ->
                        CharacterLedgerCard(
                            character = character,
                            onDelete = { vm.deleteCharacter(character) },
                            onOpenDetails = onOpenDetails,
                            onOpenEdit = onOpenEdit
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterLedgerCard(
    character: CharacterModel,
    onDelete: () -> Unit,
    onOpenDetails: (String) -> Unit,
    onOpenEdit: (String) -> Unit
) {
    val derived = remember(character) { CharacterStatEngine.build(character) }
    val maxHp = derived.maxHp.coerceAtLeast(1)
    val currentHp = when {
        character.currentHp > 0 -> character.currentHp.coerceAtMost(maxHp)
        character.sessionStateVersion > 0 -> 0
        else -> maxHp
    }
    val ancestry = remember(character.race, character.raceVariant) {
        CompendiumLookup.characterRaceDisplayName(
            character.race,
            character.raceVariant
        )
    }
    val className = remember(character.characterClass) {
        CompendiumLookup.classDisplayName(character.characterClass)
    }
    val subclassName = remember(character.subclass) {
        character.subclass
            .takeIf { it.isNotBlank() }
            ?.let(CompendiumLookup::subclassDisplayName)
    }

    var cardMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ${character.name}?") },
            text = { Text("This character will be permanently removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LedgerPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetails(character.id) },
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.82f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CharacterPortrait(character = character)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = character.name.ifBlank { "Unnamed Hero" },
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    LedgerBadge(text = "LV ${character.level}")
                }

                Text(
                    text = listOfNotNull(subclassName, className)
                        .joinToString(" ")
                        .ifBlank { "Class not chosen" },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = ancestry.ifBlank { "Ancestry not chosen" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box {
                IconButton(onClick = { cardMenuExpanded = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "Actions for ${character.name}"
                    )
                }
                DropdownMenu(
                    expanded = cardMenuExpanded,
                    onDismissRequest = { cardMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit character") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            cardMenuExpanded = false
                            onOpenEdit(character.id)
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Delete character",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            cardMenuExpanded = false
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LedgerStatTile(
                label = "HP",
                value = buildString {
                    append("$currentHp/$maxHp")
                    if (character.temporaryHp > 0) append(" +${character.temporaryHp}")
                },
                modifier = Modifier.weight(1f),
                accent = hpAccent(currentHp, maxHp)
            )
            LedgerStatTile(
                label = "AC",
                value = derived.armourClass.toString(),
                modifier = Modifier.weight(1f),
                accent = MaterialTheme.colorScheme.tertiary
            )
            LedgerStatTile(
                label = "Initiative",
                value = formatModifier(derived.initiativeBonus),
                modifier = Modifier.weight(1f),
                accent = MaterialTheme.colorScheme.secondary
            )
        }

        if (character.notes.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = character.notes.lineSequence().first(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CharacterPortrait(character: CharacterModel) {
    Surface(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        if (character.imageUri.isBlank()) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(34.dp)
                )
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(character.imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "${character.name} portrait",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun hpAccent(currentHp: Int, maxHp: Int): Color {
    val ratio = currentHp.toFloat() / maxHp.coerceAtLeast(1)
    return when {
        ratio <= 0.25f -> MaterialTheme.colorScheme.error
        ratio <= 0.5f -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.tertiary
    }
}

private fun formatModifier(value: Int): String {
    return if (value >= 0) "+$value" else value.toString()
}
