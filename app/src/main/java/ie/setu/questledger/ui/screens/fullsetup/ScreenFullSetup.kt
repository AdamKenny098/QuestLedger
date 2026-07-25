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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ie.setu.questledger.data.compendium.CompendiumLookup
import ie.setu.questledger.models.FullSetupConfig
import ie.setu.questledger.ui.components.general.CompendiumDropdown
import ie.setu.questledger.ui.components.general.CompendiumOption
import ie.setu.questledger.ui.components.general.stats.CharacterDerivedStatsCard

private enum class FullSetupStep {
    CLASS, RACE, BACKGROUND, STATS, PROFICIENCIES, GEAR, SPELLS, REVIEW
}

@Composable
fun ScreenFullSetup(
    onDone: () -> Unit = {},
    onOpenManualSetup: () -> Unit = {},
    vm: FullSetupViewModel = hiltViewModel()
) {
    val classes = remember { vm.getClasses() }
    val races = remember { vm.getRaces() }
    val backgrounds = remember { vm.getBackgrounds() }
    val weapons = remember { vm.getWeapons() }
    val armours = remember { vm.getArmour() }
    val spells = remember { vm.getSpells() }

    var step by remember { mutableStateOf(FullSetupStep.CLASS) }

    var characterName by remember { mutableStateOf("") }
    var selectedClassId by remember { mutableStateOf(classes.firstOrNull()?.id.orEmpty()) }
    var selectedRaceId by remember { mutableStateOf(races.firstOrNull()?.id.orEmpty()) }
    var selectedRaceVariantId by remember {
        mutableStateOf(
            vm.getRaceVariantsForRace(races.firstOrNull()?.id.orEmpty())
                .firstOrNull()
                ?.id
                .orEmpty()
        )
    }
    var selectedBackgroundId by remember {
        mutableStateOf(backgrounds.firstOrNull()?.id.orEmpty())
    }

    val initialBackground = backgrounds.firstOrNull()
    var selectedPersonalityTrait1 by remember {
        mutableStateOf(initialBackground?.personalityTraits?.getOrNull(0).orEmpty())
    }
    var selectedPersonalityTrait2 by remember {
        mutableStateOf(initialBackground?.personalityTraits?.getOrNull(1).orEmpty())
    }
    var selectedIdeal by remember {
        mutableStateOf(initialBackground?.ideals?.firstOrNull().orEmpty())
    }
    var selectedBond by remember {
        mutableStateOf(initialBackground?.bonds?.firstOrNull().orEmpty())
    }
    var selectedFlaw by remember {
        mutableStateOf(initialBackground?.flaws?.firstOrNull().orEmpty())
    }

    var strength by remember { mutableIntStateOf(10) }
    var dexterity by remember { mutableIntStateOf(10) }
    var constitution by remember { mutableIntStateOf(10) }
    var intelligence by remember { mutableIntStateOf(10) }
    var wisdom by remember { mutableIntStateOf(10) }
    var charisma by remember { mutableIntStateOf(10) }

    val selectedProficiencyIds = remember { mutableStateListOf<String>() }
    val selectedFlexibleAbilityIds = remember { mutableStateListOf<String>() }
    val selectedRacialSkillIds = remember { mutableStateListOf<String>() }
    val selectedRacialLanguageIds = remember { mutableStateListOf<String>() }
    var selectedRacialSpellId by remember { mutableStateOf("") }
    var selectedWeaponId by remember { mutableStateOf<String?>(null) }
    var selectedArmourId by remember { mutableStateOf<String?>(null) }
    var hasShield by remember { mutableStateOf(false) }
    val selectedSpellIds = remember { mutableStateListOf<String>() }

    var localError by remember { mutableStateOf<String?>(null) }

    val selectedBackground = remember(selectedBackgroundId) {
        backgrounds.firstOrNull { it.id == selectedBackgroundId }
    }
    val selectedRace = remember(selectedRaceId) {
        races.firstOrNull { it.id == selectedRaceId }
    }
    val raceVariants = remember(selectedRaceId) {
        vm.getRaceVariantsForRace(selectedRaceId)
    }
    val selectedRaceVariant = remember(selectedRaceId, selectedRaceVariantId) {
        raceVariants.firstOrNull { it.id == selectedRaceVariantId }
    }
    val flexibleAbilityChoices = remember(selectedRaceId, selectedRaceVariantId) {
        vm.getFlexibleAbilityChoices(selectedRaceId, selectedRaceVariantId)
    }
    val flexibleAbilityChoiceCount = remember(selectedRaceId) {
        vm.getFlexibleAbilityChoiceCount(selectedRaceId)
    }
    val racialSkillChoiceCount = remember(selectedRaceId, selectedRaceVariantId) {
        vm.getRacialSkillChoiceCount(selectedRaceId, selectedRaceVariantId)
    }
    val racialSkillOptions = remember(
        selectedRaceId,
        selectedRaceVariantId,
        selectedBackgroundId
    ) {
        vm.getRacialSkillOptions(selectedRaceId, selectedRaceVariantId)
            .filterNot { it in selectedBackground?.skillProficiencyIds.orEmpty() }
    }
    val fixedRacialSkillIds = remember(selectedRaceId, selectedRaceVariantId) {
        vm.getFixedRacialSkillIds(selectedRaceId, selectedRaceVariantId)
    }
    val racialLanguageChoiceCount = remember(selectedRaceId, selectedRaceVariantId) {
        vm.getRacialLanguageChoiceCount(selectedRaceId, selectedRaceVariantId)
    }
    val racialLanguageOptions = remember(
        selectedRaceId,
        selectedRaceVariantId,
        selectedBackgroundId
    ) {
        vm.getRacialLanguageOptions(
            selectedRaceId,
            selectedRaceVariantId,
            selectedBackgroundId
        )
    }
    val racialCantripOptions = remember(selectedRaceVariantId) {
        vm.getRacialCantripOptions(selectedRaceVariantId)
    }
    val classWeaponIds = remember(selectedClassId) { vm.getSuggestedWeaponIdsForClass(selectedClassId) }
    val classArmourIds = remember(selectedClassId) { vm.getSuggestedArmourIdsForClass(selectedClassId) }
    val classSpellIds = remember(selectedClassId) { vm.getSuggestedSpellIdsForClass(selectedClassId) }
    val classProficiencies = remember(
        selectedClassId,
        selectedBackgroundId,
        selectedRaceId,
        selectedRaceVariantId,
        fixedRacialSkillIds,
        selectedRacialSkillIds.toList()
    ) {
        vm.getSuggestedProficienciesForClass(selectedClassId)
            .filterNot {
                it in selectedBackground?.skillProficiencyIds.orEmpty() ||
                    it in fixedRacialSkillIds ||
                    it in selectedRacialSkillIds
            }
    }
    val proficiencyChoiceCount = remember(selectedClassId) {
        vm.getProficiencyChoiceCount(selectedClassId)
    }
    val canStartWithShield = remember(selectedClassId) {
        vm.classCanStartWithShield(selectedClassId)
    }

    LaunchedEffect(selectedClassId) {
        val selectedClass = classes.firstOrNull { it.id == selectedClassId }
        selectedProficiencyIds.clear()
        selectedSpellIds.clear()
        selectedWeaponId = selectedClass?.defaultWeaponId
        selectedArmourId = selectedClass?.defaultArmourId
        hasShield = selectedClass?.startsWithShield == true
    }

    LaunchedEffect(selectedBackgroundId) {
        selectedPersonalityTrait1 =
            selectedBackground?.personalityTraits?.getOrNull(0).orEmpty()
        selectedPersonalityTrait2 =
            selectedBackground?.personalityTraits?.getOrNull(1).orEmpty()
        selectedIdeal = selectedBackground?.ideals?.firstOrNull().orEmpty()
        selectedBond = selectedBackground?.bonds?.firstOrNull().orEmpty()
        selectedFlaw = selectedBackground?.flaws?.firstOrNull().orEmpty()
        selectedProficiencyIds.removeAll(
            selectedBackground?.skillProficiencyIds.orEmpty().toSet()
        )
    }

    LaunchedEffect(
        selectedRaceId,
        selectedRaceVariantId,
        selectedClassId,
        selectedBackgroundId
    ) {
        val validVariant = raceVariants.firstOrNull { it.id == selectedRaceVariantId }
        if (raceVariants.isNotEmpty() && validVariant == null) {
            selectedRaceVariantId = raceVariants.first().id
            return@LaunchedEffect
        }
        if (raceVariants.isEmpty() && selectedRaceVariantId.isNotBlank()) {
            selectedRaceVariantId = ""
            return@LaunchedEffect
        }

        val classPriority = classes.firstOrNull { it.id == selectedClassId }
            ?.quickBuildAbilityPriority
            .orEmpty()
            .map { it.name }
        selectedFlexibleAbilityIds.clear()
        selectedFlexibleAbilityIds.addAll(
            (classPriority + flexibleAbilityChoices.map { it.name })
                .distinct()
                .filter { id -> flexibleAbilityChoices.any { it.name == id } }
                .take(flexibleAbilityChoiceCount)
        )

        selectedRacialSkillIds.clear()
        selectedRacialSkillIds.addAll(
            (
                vm.getSuggestedProficienciesForClass(selectedClassId) +
                    racialSkillOptions
                )
                .distinct()
                .filter { it in racialSkillOptions && it !in fixedRacialSkillIds }
                .take(racialSkillChoiceCount)
        )

        selectedRacialLanguageIds.clear()
        selectedRacialLanguageIds.addAll(
            racialLanguageOptions.take(racialLanguageChoiceCount)
        )
        selectedRacialSpellId = racialCantripOptions.firstOrNull()?.id.orEmpty()

        selectedProficiencyIds.removeAll(
            (fixedRacialSkillIds + selectedRacialSkillIds).toSet()
        )
    }

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
        selectedRaceVariantId,
        selectedBackgroundId,
        strength,
        dexterity,
        constitution,
        intelligence,
        wisdom,
        charisma,
        selectedProficiencyIds.toList(),
        selectedFlexibleAbilityIds.toList(),
        selectedRacialSkillIds.toList(),
        selectedRacialLanguageIds.toList(),
        selectedRacialSpellId,
        selectedWeaponId,
        selectedArmourId,
        hasShield,
        selectedSpellIds.toList(),
        selectedPersonalityTrait1,
        selectedPersonalityTrait2,
        selectedIdeal,
        selectedBond,
        selectedFlaw
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
            starterSpellIds = selectedSpellIds.toList(),
            backgroundId = selectedBackgroundId,
            raceVariantId = selectedRaceVariantId,
            selectedFlexibleAbilityIds = selectedFlexibleAbilityIds.toList(),
            selectedRacialSkillIds = selectedRacialSkillIds.toList(),
            selectedRacialLanguageIds = selectedRacialLanguageIds.toList(),
            selectedRacialSpellId = selectedRacialSpellId,
            personalityTraits = listOf(
                selectedPersonalityTrait1,
                selectedPersonalityTrait2
            ),
            ideal = selectedIdeal,
            bond = selectedBond,
            flaw = selectedFlaw
        )
    }

    val preview = remember(config) {
        if (
            config.name.isNotBlank() &&
            config.classId.isNotBlank() &&
            config.raceId.isNotBlank() &&
            config.backgroundId.isNotBlank()
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
                            summary = clazz.description,
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
                            subtitle = "${race.size} • Speed ${race.speed} ft.",
                            summary = race.description,
                            isSelected = race.id == selectedRaceId,
                            onClick = {
                                selectedRaceId = race.id
                                selectedRaceVariantId = vm.getRaceVariantsForRace(race.id)
                                    .firstOrNull()
                                    ?.id
                                    .orEmpty()
                            }
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    selectedRace?.let { race ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (race.id == "dragonborn") {
                                "Choose Draconic Ancestry"
                            } else if (raceVariants.isNotEmpty()) {
                                "Choose Subrace"
                            } else {
                                "Ancestry Details"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))

                        raceVariants.forEach { variant ->
                            ChoiceCard(
                                title = variant.name,
                                subtitle = CompendiumLookup.formatStatBonuses(race, variant),
                                summary = variant.description,
                                isSelected = variant.id == selectedRaceVariantId,
                                onClick = { selectedRaceVariantId = variant.id }
                            )
                            Spacer(Modifier.height(10.dp))
                        }

                        if (raceVariants.isEmpty()) {
                            Text(
                                "This race has no required subrace choice.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        selectedRaceVariant?.let { variant ->
                            Text(
                                "Traits: ${
                                    (race.passiveTraits + variant.passiveTraits)
                                        .distinct()
                                        .joinToString()
                                }",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Speed: ${variant.speedOverride ?: race.speed} ft.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (flexibleAbilityChoiceCount > 0) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Flexible Ability Bonuses",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "Choose $flexibleAbilityChoiceCount different abilities.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(8.dp))

                            repeat(flexibleAbilityChoiceCount) { index ->
                                val selectedId =
                                    selectedFlexibleAbilityIds.getOrNull(index).orEmpty()
                                val blocked = selectedFlexibleAbilityIds
                                    .filterIndexed { otherIndex, _ -> otherIndex != index }
                                    .toSet()
                                CompendiumDropdown(
                                    label = "Ability Bonus ${index + 1}",
                                    options = flexibleAbilityChoices
                                        .filterNot { it.name in blocked }
                                        .map {
                                            CompendiumOption(
                                                it.name,
                                                CompendiumLookup.abilityLabel(it)
                                            )
                                        },
                                    selectedId = selectedId,
                                    onSelected = { abilityId ->
                                        if (index < selectedFlexibleAbilityIds.size) {
                                            selectedFlexibleAbilityIds[index] = abilityId
                                        } else {
                                            selectedFlexibleAbilityIds.add(abilityId)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        if (racialSkillChoiceCount > 0) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Racial Skill Choices",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "Choose $racialSkillChoiceCount different skills.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(8.dp))

                            racialSkillOptions.forEach { skill ->
                                ToggleLine(
                                    label = skill,
                                    selected = skill in selectedRacialSkillIds,
                                    onClick = {
                                        if (skill in selectedRacialSkillIds) {
                                            selectedRacialSkillIds.remove(skill)
                                        } else if (
                                            selectedRacialSkillIds.size <
                                            racialSkillChoiceCount
                                        ) {
                                            selectedRacialSkillIds.add(skill)
                                        }
                                    }
                                )
                            }
                        }

                        if (racialLanguageChoiceCount > 0) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Extra Language",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(Modifier.height(8.dp))

                            repeat(racialLanguageChoiceCount) { index ->
                                val selectedId =
                                    selectedRacialLanguageIds.getOrNull(index).orEmpty()
                                val blocked = selectedRacialLanguageIds
                                    .filterIndexed { otherIndex, _ -> otherIndex != index }
                                    .toSet()
                                CompendiumDropdown(
                                    label = "Language ${index + 1}",
                                    options = racialLanguageOptions
                                        .filterNot { it in blocked }
                                        .map { CompendiumOption(it, it) },
                                    selectedId = selectedId,
                                    onSelected = { language ->
                                        if (index < selectedRacialLanguageIds.size) {
                                            selectedRacialLanguageIds[index] = language
                                        } else {
                                            selectedRacialLanguageIds.add(language)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        if (racialCantripOptions.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Racial Cantrip",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(Modifier.height(8.dp))
                            CompendiumDropdown(
                                label = "Cantrip",
                                options = racialCantripOptions.map {
                                    CompendiumOption(it.id, it.name)
                                },
                                selectedId = selectedRacialSpellId,
                                onSelected = { selectedRacialSpellId = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                FullSetupStep.BACKGROUND -> {
                    Text("Step 3: Choose Background", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    backgrounds.forEach { background ->
                        ChoiceCard(
                            title = background.name,
                            subtitle = "Skills: ${background.skillProficiencyIds.joinToString()}",
                            summary = background.description,
                            isSelected = background.id == selectedBackgroundId,
                            onClick = { selectedBackgroundId = background.id }
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    selectedBackground?.let { background ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Feature: ${background.featureName}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            background.featureDescription,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tools: ${background.toolProficiencies.joinToString().ifBlank { "None" }}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Languages: ${
                                background.suggestedLanguages.joinToString().ifBlank { "None" }
                            }",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Starting Gold: ${background.startingGoldGp} gp",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(Modifier.height(12.dp))
                        Text("Roleplay Prompts", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))

                        CompendiumDropdown(
                            label = "Personality Trait 1",
                            options = background.personalityTraits.map {
                                CompendiumOption(it, it)
                            },
                            selectedId = selectedPersonalityTrait1,
                            onSelected = { selected ->
                                selectedPersonalityTrait1 = selected
                                if (selectedPersonalityTrait2 == selected) {
                                    selectedPersonalityTrait2 =
                                        background.personalityTraits.firstOrNull { it != selected }
                                            .orEmpty()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))

                        CompendiumDropdown(
                            label = "Personality Trait 2",
                            options = background.personalityTraits
                                .filterNot { it == selectedPersonalityTrait1 }
                                .map { CompendiumOption(it, it) },
                            selectedId = selectedPersonalityTrait2,
                            onSelected = { selectedPersonalityTrait2 = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))

                        CompendiumDropdown(
                            label = "Ideal",
                            options = background.ideals.map { CompendiumOption(it, it) },
                            selectedId = selectedIdeal,
                            onSelected = { selectedIdeal = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))

                        CompendiumDropdown(
                            label = "Bond",
                            options = background.bonds.map { CompendiumOption(it, it) },
                            selectedId = selectedBond,
                            onSelected = { selectedBond = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))

                        CompendiumDropdown(
                            label = "Flaw",
                            options = background.flaws.map { CompendiumOption(it, it) },
                            selectedId = selectedFlaw,
                            onSelected = { selectedFlaw = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                FullSetupStep.STATS -> {
                    Text("Step 4: Allocate Stats", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Racial ability bonuses are applied automatically in the review.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))

                    StatRow("Strength", strength, { if (strength > 8) strength-- }, { if (strength < 18) strength++ })
                    StatRow("Dexterity", dexterity, { if (dexterity > 8) dexterity-- }, { if (dexterity < 18) dexterity++ })
                    StatRow("Constitution", constitution, { if (constitution > 8) constitution-- }, { if (constitution < 18) constitution++ })
                    StatRow("Intelligence", intelligence, { if (intelligence > 8) intelligence-- }, { if (intelligence < 18) intelligence++ })
                    StatRow("Wisdom", wisdom, { if (wisdom > 8) wisdom-- }, { if (wisdom < 18) wisdom++ })
                    StatRow("Charisma", charisma, { if (charisma > 8) charisma-- }, { if (charisma < 18) charisma++ })
                }

                FullSetupStep.PROFICIENCIES -> {
                    Text("Step 5: Choose Class Proficiencies", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Choose up to $proficiencyChoiceCount. Background skills are added automatically.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))

                    classProficiencies.forEach { prof ->
                        ToggleLine(
                            label = prof,
                            selected = selectedProficiencyIds.contains(prof),
                            onClick = {
                                if (selectedProficiencyIds.contains(prof)) {
                                    selectedProficiencyIds.remove(prof)
                                } else if (selectedProficiencyIds.size < proficiencyChoiceCount) {
                                    selectedProficiencyIds.add(prof)
                                }
                            }
                        )
                    }
                }

                FullSetupStep.GEAR -> {
                    Text("Step 6: Choose Gear", style = MaterialTheme.typography.titleMedium)
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

                    if (canStartWithShield) {
                        ToggleLine(
                            label = "Shield",
                            selected = hasShield,
                            onClick = { hasShield = !hasShield }
                        )
                    } else {
                        Text("This class does not start proficient with a shield.")
                    }
                }

                FullSetupStep.SPELLS -> {
                    Text("Step 7: Choose Spells", style = MaterialTheme.typography.titleMedium)
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
                    Text("Step 8: Review", style = MaterialTheme.typography.titleMedium)
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
                                FullSetupStep.BACKGROUND -> FullSetupStep.RACE
                                FullSetupStep.STATS -> FullSetupStep.BACKGROUND
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
                                    when {
                                        selectedRaceId.isBlank() ->
                                            localError = "Choose a race"
                                        raceVariants.isNotEmpty() &&
                                            selectedRaceVariantId.isBlank() ->
                                            localError = "Choose an ancestry"
                                        selectedFlexibleAbilityIds.size !=
                                            flexibleAbilityChoiceCount ->
                                            localError = "Choose every flexible ability bonus"
                                        selectedRacialSkillIds.size !=
                                            racialSkillChoiceCount ->
                                            localError = "Choose every racial skill"
                                        selectedRacialLanguageIds.size !=
                                            racialLanguageChoiceCount ->
                                            localError = "Choose every racial language"
                                        racialCantripOptions.isNotEmpty() &&
                                            selectedRacialSpellId.isBlank() ->
                                            localError = "Choose a racial cantrip"
                                        else -> {
                                            localError = null
                                            step = FullSetupStep.BACKGROUND
                                        }
                                    }
                                }

                                FullSetupStep.BACKGROUND -> {
                                    if (selectedBackgroundId.isBlank()) {
                                        localError = "Choose a background"
                                    } else {
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
                        },
                        enabled = !vm.isLoading.value
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
                                raceVariants.isNotEmpty() &&
                                    selectedRaceVariantId.isBlank() ->
                                    localError = "Choose an ancestry"
                                selectedBackgroundId.isBlank() ->
                                    localError = "Choose a background"
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
