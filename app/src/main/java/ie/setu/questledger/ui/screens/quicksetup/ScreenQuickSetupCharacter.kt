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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.setu.questledger.models.QuickSetupConfig
import ie.setu.questledger.ui.components.general.stats.CharacterDerivedStatsCard
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
    val backgrounds = remember { vm.getBackgrounds() }

    var name by remember { mutableStateOf("") }
    var selectedRaceId by remember { mutableStateOf(races.firstOrNull()?.id.orEmpty()) }
    var selectedRaceVariantId by remember {
        mutableStateOf(
            vm.getRaceVariantsForRace(races.firstOrNull()?.id.orEmpty())
                .firstOrNull()
                ?.id
                .orEmpty()
        )
    }
    var selectedClassId by remember { mutableStateOf(classes.firstOrNull()?.id.orEmpty()) }
    var selectedSubclassId by remember { mutableStateOf("") }
    val selectedSubclassChoiceIds = remember { mutableStateListOf<String>() }
    var selectedBackgroundId by remember {
        mutableStateOf(backgrounds.firstOrNull()?.id.orEmpty())
    }
    var level by remember { mutableStateOf(1) }
    var localError by remember { mutableStateOf<String?>(null) }
    val raceVariants = remember(selectedRaceId) {
        vm.getRaceVariantsForRace(selectedRaceId)
    }
    val classSubclasses = remember(selectedClassId) {
        vm.getSubclassesForClass(selectedClassId)
    }
    val availableSubclasses = remember(selectedClassId, level) {
        vm.getSubclassesForClass(selectedClassId)
            .filter { it.selectionLevel <= level }
    }
    val selectedSubclass = remember(selectedSubclassId, availableSubclasses) {
        availableSubclasses.firstOrNull { it.id == selectedSubclassId }
    }
    val subclassChoiceGroups = remember(
        selectedSubclassId,
        level,
        selectedSubclassChoiceIds.toList()
    ) {
        selectedSubclass
            ?.choiceGroups
            .orEmpty()
            .filter { it.minimumLevel <= level }
    }

    LaunchedEffect(selectedClassId, level, selectedSubclassId) {
        if (availableSubclasses.isEmpty()) {
            selectedSubclassId = ""
            selectedSubclassChoiceIds.clear()
            return@LaunchedEffect
        }
        val validSubclass = availableSubclasses.firstOrNull {
            it.id == selectedSubclassId
        }
        if (validSubclass == null) {
            selectedSubclassId = availableSubclasses.first().id
            return@LaunchedEffect
        }
        val normalisedChoices = validSubclass.choiceGroups
            .filter { it.minimumLevel <= level }
            .flatMap { group ->
                val optionIds = group.options.map { it.id }
                (
                    selectedSubclassChoiceIds.filter { it in optionIds } +
                        optionIds
                    )
                    .distinct()
                    .take(group.selectionCount)
            }
        if (normalisedChoices != selectedSubclassChoiceIds.toList()) {
            selectedSubclassChoiceIds.clear()
            selectedSubclassChoiceIds.addAll(normalisedChoices)
        }
    }

    val config = QuickSetupConfig(
        name = name,
        raceId = selectedRaceId,
        classId = selectedClassId,
        subclassId = selectedSubclassId,
        level = level,
        backgroundId = selectedBackgroundId,
        raceVariantId = selectedRaceVariantId,
        selectedSubclassChoiceIds = selectedSubclassChoiceIds.toList()
    )

    val preview = remember(
        name,
        selectedRaceId,
        selectedRaceVariantId,
        selectedClassId,
        selectedSubclassId,
        selectedSubclassChoiceIds.toList(),
        selectedBackgroundId,
        level
    ) {
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
                "Fast character creation for new players. Pick a class, race, background, and level, and QuestLedger will build a starter character for you.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onOpenManualSetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Setup Menu")
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
                onSelected = { raceId ->
                    selectedRaceId = raceId
                    selectedRaceVariantId = vm.getRaceVariantsForRace(raceId)
                        .firstOrNull()
                        ?.id
                        .orEmpty()
                }
            )

            Spacer(Modifier.height(10.dp))

            if (raceVariants.isNotEmpty()) {
                CompendiumDropdown(
                    label = if (selectedRaceId == "dragonborn") {
                        "Draconic Ancestry"
                    } else {
                        "Subrace"
                    },
                    options = raceVariants.map { CompendiumOption(it.id, it.name) },
                    selectedId = selectedRaceVariantId,
                    onSelected = { selectedRaceVariantId = it }
                )

                Spacer(Modifier.height(10.dp))
            }

            CompendiumDropdown(
                label = "Background",
                options = backgrounds.map { CompendiumOption(it.id, it.name) },
                selectedId = selectedBackgroundId,
                onSelected = { selectedBackgroundId = it }
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

            if (availableSubclasses.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                CompendiumDropdown(
                    label = "Subclass",
                    options = availableSubclasses.map {
                        CompendiumOption(it.id, it.name)
                    },
                    selectedId = selectedSubclassId,
                    onSelected = {
                        selectedSubclassId = it
                        selectedSubclassChoiceIds.clear()
                    }
                )
                selectedSubclass?.let { subclass ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        subclass.description,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                subclassChoiceGroups.forEach { group ->
                    repeat(group.selectionCount) { index ->
                        val optionIds = group.options.map { it.id }.toSet()
                        val selectedForGroup =
                            selectedSubclassChoiceIds.filter { it in optionIds }
                        val selectedId = selectedForGroup.getOrNull(index).orEmpty()
                        val blocked = selectedForGroup
                            .filterIndexed { otherIndex, _ -> otherIndex != index }
                            .toSet()
                        Spacer(Modifier.height(8.dp))
                        CompendiumDropdown(
                            label = if (group.selectionCount == 1) {
                                group.name
                            } else {
                                "${group.name} ${index + 1}"
                            },
                            options = group.options
                                .filterNot { it.id in blocked }
                                .map { CompendiumOption(it.id, it.name) },
                            selectedId = selectedId,
                            onSelected = { optionId ->
                                val oldId = selectedForGroup.getOrNull(index)
                                if (oldId != null) {
                                    val listIndex =
                                        selectedSubclassChoiceIds.indexOf(oldId)
                                    selectedSubclassChoiceIds[listIndex] = optionId
                                } else {
                                    selectedSubclassChoiceIds.add(optionId)
                                }
                            }
                        )
                    }
                }
            } else {
                classSubclasses.firstOrNull()?.let { futureSubclass ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Subclass unlocks at level ${futureSubclass.selectionLevel}.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

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

            if (vm.isLoading.value) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
            }

            localError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
            }

            if (vm.isErr.value && vm.error.value.isNotBlank()) {
                Text(
                    text = vm.error.value,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    when {
                        name.isBlank() -> localError = "Name is required"
                        selectedClassId.isBlank() -> localError = "Class is required"
                        selectedRaceId.isBlank() -> localError = "Race is required"
                        raceVariants.isNotEmpty() && selectedRaceVariantId.isBlank() ->
                            localError = "Ancestry is required"
                        selectedBackgroundId.isBlank() -> localError = "Background is required"
                        level !in 1..20 -> localError = "Level must be between 1 and 20"
                        availableSubclasses.isNotEmpty() &&
                            selectedSubclassId.isBlank() ->
                            localError = "Subclass is required at this level"
                        else -> {
                            localError = null
                            vm.saveQuickSetup(config) {
                                onDone()
                            }
                        }
                    }
                },
                enabled = !vm.isLoading.value,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Quick Character")
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}
