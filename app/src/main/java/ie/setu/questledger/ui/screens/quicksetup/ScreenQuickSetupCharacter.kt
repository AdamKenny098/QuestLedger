package ie.setu.questledger.ui.screens.quicksetup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.setu.questledger.models.QuickSetupConfig
import ie.setu.questledger.ui.components.general.CharacterDerivedStatsCard
import ie.setu.questledger.ui.components.general.CompendiumDropdown
import ie.setu.questledger.ui.components.general.CompendiumOption

@Composable
fun ScreenQuickSetupCharacter(
    onDone: () -> Unit = {},
    onOpenManualSetup: () -> Unit = {},
    vm: QuickSetupViewModel = hiltViewModel()
) {
    val races = remember { vm.getRaces() }
    val classes = remember { vm.getClasses() }

    var name by remember { mutableStateOf("") }
    var selectedRaceId by remember { mutableStateOf(races.firstOrNull()?.id.orEmpty()) }
    var selectedClassId by remember { mutableStateOf(classes.firstOrNull()?.id.orEmpty()) }
    var level by remember { mutableStateOf(1) }
    var error by remember { mutableStateOf<String?>(null) }

    val config = QuickSetupConfig(
        name = name,
        raceId = selectedRaceId,
        classId = selectedClassId,
        level = level
    )

    val preview = remember(name, selectedRaceId, selectedClassId, level) {
        vm.buildPreview(config)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Quick Setup", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            Text(
                "Fast character creation for new players. Pick a class, race, and level, and QuestLedger will build a starter character for you.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onOpenManualSetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use Manual Setup Instead")
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            CompendiumDropdown(
                label = "Class",
                options = classes.map { CompendiumOption(it.id, it.name) },
                selectedId = selectedClassId,
                onSelected = { selectedClassId = it }
            )

            Spacer(Modifier.height(10.dp))

            CompendiumDropdown(
                label = "Race",
                options = races.map { CompendiumOption(it.id, it.name) },
                selectedId = selectedRaceId,
                onSelected = { selectedRaceId = it }
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = level.toString(),
                onValueChange = { input ->
                    val parsed = input.filter { it.isDigit() }.toIntOrNull()
                    if (parsed != null) {
                        level = parsed.coerceIn(1, 20)
                    } else if (input.isBlank()) {
                        level = 1
                    }
                },
                label = { Text("Level (1–20)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            preview?.let { result ->
                Text("Preview", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))

                CharacterDerivedStatsCard(character = result.character)

                Spacer(Modifier.height(12.dp))

                Text("Starter Loadout", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                result.summaryLines.forEach { line ->
                    Text(line, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(12.dp))

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    when {
                        name.isBlank() -> error = "Name is required"
                        selectedClassId.isBlank() -> error = "Class is required"
                        selectedRaceId.isBlank() -> error = "Race is required"
                        level !in 1..20 -> error = "Level must be between 1 and 20"
                        else -> {
                            error = null
                            vm.saveQuickSetup(config) {
                                onDone()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Quick Character")
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}