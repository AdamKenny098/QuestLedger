package ie.setu.questledger.ui.screens.fullsetup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ie.setu.questledger.models.FullSetupConfig
import ie.setu.questledger.ui.components.general.CharacterDerivedStatsCard

private enum class FullSetupStep {
    CLASS, RACE, STATS, PROFICIENCIES, GEAR, SPELLS, REVIEW
}

@Composable
fun ScreenFullSetup(
    onDone: () -> Unit = {},
    onOpenManualSetup: () -> Unit = {},
    vm: FullSetupViewModel = hiltViewModel()
) {
    val classes = remember { vm.getClasses() }
    val races = remember { vm.getRaces() }
    val weapons = remember { vm.getWeapons() }
    val armours = remember { vm.getArmour() }
    val spells = remember { vm.getSpells() }

    var step by remember { mutableStateOf(FullSetupStep.CLASS) }

    var characterName by remember { mutableStateOf("") }
    var selectedClassId by remember { mutableStateOf(classes.firstOrNull()?.id.orEmpty()) }
    var selectedRaceId by remember { mutableStateOf(races.firstOrNull()?.id.orEmpty()) }

    var strength by remember { mutableIntStateOf(10) }
    var dexterity by remember { mutableIntStateOf(10) }
    var constitution by remember { mutableIntStateOf(10) }
    var intelligence by remember { mutableIntStateOf(10) }
    var wisdom by remember { mutableIntStateOf(10) }
    var charisma by remember { mutableIntStateOf(10) }

    val selectedProficiencyIds = remember { mutableStateListOf<String>() }
    var selectedWeaponId by remember { mutableStateOf<String?>(null) }
    var selectedArmourId by remember { mutableStateOf<String?>(null) }
    var hasShield by remember { mutableStateOf(false) }
    val selectedSpellIds = remember { mutableStateListOf<String>() }

    var localError by remember { mutableStateOf<String?>(null) }

    val classWeaponIds = remember(selectedClassId) { vm.getSuggestedWeaponIdsForClass(selectedClassId) }
    val classArmourIds = remember(selectedClassId) { vm.getSuggestedArmourIdsForClass(selectedClassId) }
    val classSpellIds = remember(selectedClassId) { vm.getSuggestedSpellIdsForClass(selectedClassId) }
    val classProficiencies = remember(selectedClassId) { vm.getSuggestedProficienciesForClass(selectedClassId) }

    val filteredWeapons = remember(classWeaponIds, weapons) {
        weapons.filter { it.id in classWeaponIds }
    }

    val filteredArmours = remember(classArmourIds, armours) {
        armours.filter { it.id in classArmourIds }
    }

    val filteredSpells = remember(classSpellIds, spells) {
        spells.filter { it.id in classSpellIds }
    }

    val config = remember(
        characterName,
        selectedClassId,
        selectedRaceId,
        strength,
        dexterity,
        constitution,
        intelligence,
        wisdom,
        charisma,
        selectedProficiencyIds.toList(),
        selectedWeaponId,
        selectedArmourId,
        hasShield,
        selectedSpellIds.toList()
    ) {
        FullSetupConfig(
            name = characterName.trim(),
            raceId = selectedRaceId,
            classId = selectedClassId,
            level = 1,
            strength = strength,
            dexterity = dexterity,
            constitution = constitution,
            intelligence = intelligence,
            wisdom = wisdom,
            charisma = charisma,
            selectedProficiencyIds = selectedProficiencyIds.toList(),
            starterWeaponId = selectedWeaponId,
            starterArmourId = selectedArmourId,
            hasShield = hasShield,
            starterSpellIds = selectedSpellIds.toList()
        )
    }

    val preview = remember(config) {
        if (
            config.name.isNotBlank() &&
            config.classId.isNotBlank() &&
            config.raceId.isNotBlank()
        ) {
            vm.buildPreview(config)
        } else {
            null
        }
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
            Text("Full Setup", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            Text(
                "Step-by-step guided builder for players who want more control over their character.",
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
                value = characterName,
                onValueChange = { characterName = it },
                label = { Text("Character Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            when (step) {
                FullSetupStep.CLASS -> {
                    Text("Step 1: Choose Class", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    classes.forEach { clazz ->
                        ChoiceCard(
                            title = clazz.name,
                            subtitle = "Hit Die: d${clazz.hitDie}",
                            summary = clazz.primaryStats.toString(),
                            isSelected = clazz.id == selectedClassId,
                            onClick = { selectedClassId = clazz.id }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }

                FullSetupStep.RACE -> {
                    Text("Step 2: Choose Race", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    races.forEach { race ->
                        ChoiceCard(
                            title = race.name,
                            subtitle = "Race Option",
                            summary = race.description,
                            isSelected = race.id == selectedRaceId,
                            onClick = { selectedRaceId = race.id }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }

                FullSetupStep.STATS -> {
                    Text("Step 3: Allocate Stats", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    StatRow("Strength", strength, { if (strength > 8) strength-- }, { if (strength < 18) strength++ })
                    StatRow("Dexterity", dexterity, { if (dexterity > 8) dexterity-- }, { if (dexterity < 18) dexterity++ })
                    StatRow("Constitution", constitution, { if (constitution > 8) constitution-- }, { if (constitution < 18) constitution++ })
                    StatRow("Intelligence", intelligence, { if (intelligence > 8) intelligence-- }, { if (intelligence < 18) intelligence++ })
                    StatRow("Wisdom", wisdom, { if (wisdom > 8) wisdom-- }, { if (wisdom < 18) wisdom++ })
                    StatRow("Charisma", charisma, { if (charisma > 8) charisma-- }, { if (charisma < 18) charisma++ })
                }

                FullSetupStep.PROFICIENCIES -> {
                    Text("Step 4: Choose Proficiencies", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    classProficiencies.forEach { prof ->
                        ToggleLine(
                            label = prof,
                            selected = selectedProficiencyIds.contains(prof),
                            onClick = {
                                if (selectedProficiencyIds.contains(prof)) {
                                    selectedProficiencyIds.remove(prof)
                                } else if (selectedProficiencyIds.size < 2) {
                                    selectedProficiencyIds.add(prof)
                                }
                            }
                        )
                    }
                }

                FullSetupStep.GEAR -> {
                    Text("Step 5: Choose Gear", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    Text("Weapon", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))

                    filteredWeapons.forEach { weapon ->
                        ToggleLine(
                            label = weapon.name,
                            selected = selectedWeaponId == weapon.id,
                            onClick = { selectedWeaponId = weapon.id }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text("Armour", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))

                    if (filteredArmours.isEmpty()) {
                        Text("This class has no default armour choices.")
                    } else {
                        filteredArmours.forEach { armour ->
                            ToggleLine(
                                label = armour.name,
                                selected = selectedArmourId == armour.id,
                                onClick = { selectedArmourId = armour.id }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    ToggleLine(
                        label = "Shield",
                        selected = hasShield,
                        onClick = { hasShield = !hasShield }
                    )
                }

                FullSetupStep.SPELLS -> {
                    Text("Step 6: Choose Spells", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    if (!vm.classUsesSpells(selectedClassId)) {
                        Text("This class does not start with spell selection.")
                    } else {
                        filteredSpells.forEach { spell ->
                            ToggleLine(
                                label = spell.name,
                                selected = selectedSpellIds.contains(spell.id),
                                onClick = {
                                    if (selectedSpellIds.contains(spell.id)) {
                                        selectedSpellIds.remove(spell.id)
                                    } else if (selectedSpellIds.size < 3) {
                                        selectedSpellIds.add(spell.id)
                                    }
                                }
                            )
                        }
                    }
                }

                FullSetupStep.REVIEW -> {
                    Text("Step 7: Review", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))

                    preview?.let { result ->
                        CharacterDerivedStatsCard(character = result.character)
                        Spacer(Modifier.height(12.dp))

                        result.summaryLines.forEach { line ->
                            Text(line, style = MaterialTheme.typography.bodyMedium)
                        }
                    } ?: Text("Complete the earlier steps to generate a preview.")
                }
            }

            Spacer(Modifier.height(16.dp))

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step != FullSetupStep.CLASS) {
                    TextButton(
                        onClick = {
                            localError = null
                            step = when (step) {
                                FullSetupStep.CLASS -> FullSetupStep.CLASS
                                FullSetupStep.RACE -> FullSetupStep.CLASS
                                FullSetupStep.STATS -> FullSetupStep.RACE
                                FullSetupStep.PROFICIENCIES -> FullSetupStep.STATS
                                FullSetupStep.GEAR -> FullSetupStep.PROFICIENCIES
                                FullSetupStep.SPELLS -> FullSetupStep.GEAR
                                FullSetupStep.REVIEW -> FullSetupStep.SPELLS
                            }
                        }
                    ) {
                        Text("Back")
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                if (step != FullSetupStep.REVIEW) {
                    Button(
                        onClick = {
                            when (step) {
                                FullSetupStep.CLASS -> {
                                    if (selectedClassId.isBlank()) localError = "Choose a class"
                                    else {
                                        localError = null
                                        step = FullSetupStep.RACE
                                    }
                                }

                                FullSetupStep.RACE -> {
                                    if (selectedRaceId.isBlank()) localError = "Choose a race"
                                    else {
                                        localError = null
                                        step = FullSetupStep.STATS
                                    }
                                }

                                FullSetupStep.STATS -> {
                                    localError = null
                                    step = FullSetupStep.PROFICIENCIES
                                }

                                FullSetupStep.PROFICIENCIES -> {
                                    localError = null
                                    step = FullSetupStep.GEAR
                                }

                                FullSetupStep.GEAR -> {
                                    localError = null
                                    step = FullSetupStep.SPELLS
                                }

                                FullSetupStep.SPELLS -> {
                                    if (characterName.isBlank()) localError = "Enter a character name"
                                    else {
                                        localError = null
                                        step = FullSetupStep.REVIEW
                                    }
                                }

                                FullSetupStep.REVIEW -> Unit
                            }
                        }
                    ) {
                        Text("Next")
                    }
                } else {
                    Button(
                        onClick = {
                            when {
                                characterName.isBlank() -> localError = "Enter a character name"
                                selectedClassId.isBlank() -> localError = "Choose a class"
                                selectedRaceId.isBlank() -> localError = "Choose a race"
                                else -> {
                                    localError = null
                                    vm.saveCharacter(config) {
                                        onDone()
                                    }
                                }
                            }
                        },
                        enabled = !vm.isLoading.value
                    ) {
                        Text("Create Character")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ChoiceCard(
    title: String,
    subtitle: String,
    summary: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        tonalElevation = if (isSelected) 4.dp else 1.dp,
        color = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        },
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Text(summary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.weight(1f))
        TextButton(onClick = onMinus) { Text("-") }
        Text(value.toString())
        TextButton(onClick = onPlus) { Text("+") }
    }
}

@Composable
private fun ToggleLine(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        tonalElevation = if (selected) 2.dp else 0.dp,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label)
            Text(if (selected) "Selected" else "Choose")
        }
    }
    Spacer(Modifier.height(6.dp))
}