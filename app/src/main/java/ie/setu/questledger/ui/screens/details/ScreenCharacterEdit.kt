package ie.setu.questledger.ui.screens.details

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import ie.setu.questledger.data.compendium.CompendiumLookup
import ie.setu.questledger.data.rules.CharacterStatEngine
import ie.setu.questledger.models.characters.CharacterModel
import ie.setu.questledger.ui.components.general.CompendiumDropdown
import ie.setu.questledger.ui.components.general.CompendiumOption
import ie.setu.questledger.ui.components.general.ShowPhotoPicker
import ie.setu.questledger.ui.components.general.equipment.CharacterEquipmentEditorCard
import ie.setu.questledger.ui.components.general.inventory.CharacterInventoryEditorCard
import ie.setu.questledger.ui.components.general.stats.AbilityScoreField

@Composable
fun ScreenCharacterEdit(
    onDone: () -> Unit,
    onOpenSpellbook: (String) -> Unit = {},
    vm: CharacterDetailsViewModel = hiltViewModel()
) {
    val c = vm.character.value

    val races = remember { vm.getRaces() }
    val classes = remember { vm.getClasses() }
    val backgrounds = remember { vm.getBackgrounds() }

    var name by remember(c.id) { mutableStateOf(c.name) }
    var selectedClassId by remember(c.id) {
        mutableStateOf(CompendiumLookup.findClass(c.characterClass)?.id ?: c.characterClass)
    }
    var selectedRaceId by remember(c.id) {
        mutableStateOf(CompendiumLookup.findRace(c.race)?.id ?: c.race)
    }
    var selectedRaceVariantId by remember(c.id) {
        mutableStateOf(
            CompendiumLookup.findRaceVariant(c.raceVariant)?.id
                ?: c.raceVariant
        )
    }
    val selectedFlexibleAbilityIds = remember(c.id) {
        mutableStateListOf<String>().apply {
            addAll(c.selectedRacialAbilityBonusIds)
        }
    }
    val selectedRacialSkillIds = remember(c.id) {
        mutableStateListOf<String>().apply {
            addAll(c.selectedRacialSkillIds)
        }
    }
    val selectedRacialLanguageIds = remember(c.id) {
        mutableStateListOf<String>().apply {
            addAll(c.selectedRacialLanguageIds)
        }
    }
    var selectedRacialSpellId by remember(c.id) {
        mutableStateOf(c.selectedRacialSpellId)
    }
    var selectedBackgroundId by remember(c.id) {
        mutableStateOf(
            CompendiumLookup.findBackground(c.background)?.id
                ?: c.background.ifBlank { backgrounds.firstOrNull()?.id.orEmpty() }
        )
    }
    var levelText by remember(c.id) { mutableStateOf(c.level.toString()) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember(c.id) { mutableStateOf<Uri?>(null) }

    var strengthText by remember(c.id) { mutableStateOf(c.strength.toString()) }
    var dexterityText by remember(c.id) { mutableStateOf(c.dexterity.toString()) }
    var constitutionText by remember(c.id) { mutableStateOf(c.constitution.toString()) }
    var intelligenceText by remember(c.id) { mutableStateOf(c.intelligence.toString()) }
    var wisdomText by remember(c.id) { mutableStateOf(c.wisdom.toString()) }
    var charismaText by remember(c.id) { mutableStateOf(c.charisma.toString()) }

    val errorEmptyNotes = "Notes cannot be empty..."
    val errorShortNotes = "Notes must be at least 2 characters"

    var text by rememberSaveable { mutableStateOf("") }
    var isEmptyError by rememberSaveable { mutableStateOf(false) }
    var isShortError by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(c.id) {
        text = c.notes
        isEmptyError = false
        isShortError = false
        selectedImageUri = null
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

    val selectedClass = remember(selectedClassId) {
        classes.firstOrNull { it.id == selectedClassId }
    }

    val selectedBackground = remember(selectedBackgroundId) {
        backgrounds.firstOrNull { it.id == selectedBackgroundId }
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

    LaunchedEffect(
        selectedRaceId,
        selectedRaceVariantId,
        selectedClassId,
        selectedBackgroundId,
        c.id
    ) {
        if (raceVariants.isNotEmpty() && selectedRaceVariant == null) {
            selectedRaceVariantId = raceVariants.first().id
            return@LaunchedEffect
        }
        if (raceVariants.isEmpty() && selectedRaceVariantId.isNotBlank()) {
            selectedRaceVariantId = ""
            return@LaunchedEffect
        }

        val abilityOptions = flexibleAbilityChoices.map { it.name }
        if (
            selectedFlexibleAbilityIds.size != flexibleAbilityChoiceCount ||
            selectedFlexibleAbilityIds.any { it !in abilityOptions } ||
            selectedFlexibleAbilityIds.distinct().size !=
            selectedFlexibleAbilityIds.size
        ) {
            val classPriority = selectedClass?.quickBuildAbilityPriority
                .orEmpty()
                .map { it.name }
            selectedFlexibleAbilityIds.clear()
            selectedFlexibleAbilityIds.addAll(
                (classPriority + abilityOptions)
                    .distinct()
                    .filter { it in abilityOptions }
                    .take(flexibleAbilityChoiceCount)
            )
        }

        if (
            selectedRacialSkillIds.size != racialSkillChoiceCount ||
            selectedRacialSkillIds.any { it !in racialSkillOptions } ||
            selectedRacialSkillIds.distinct().size != selectedRacialSkillIds.size
        ) {
            selectedRacialSkillIds.clear()
            selectedRacialSkillIds.addAll(
                racialSkillOptions.take(racialSkillChoiceCount)
            )
        }

        if (
            selectedRacialLanguageIds.size != racialLanguageChoiceCount ||
            selectedRacialLanguageIds.any { it !in racialLanguageOptions } ||
            selectedRacialLanguageIds.distinct().size !=
            selectedRacialLanguageIds.size
        ) {
            selectedRacialLanguageIds.clear()
            selectedRacialLanguageIds.addAll(
                racialLanguageOptions.take(racialLanguageChoiceCount)
            )
        }

        if (
            racialCantripOptions.isNotEmpty() &&
            racialCantripOptions.none { it.id == selectedRacialSpellId }
        ) {
            selectedRacialSpellId = racialCantripOptions.first().id
        } else if (racialCantripOptions.isEmpty()) {
            selectedRacialSpellId = ""
        }
    }

    val previewCharacter = CharacterModel(
        id = c.id,
        email = c.email,
        name = name,
        characterClass = selectedClassId,
        race = selectedRaceId,
        raceVariant = selectedRaceVariantId,
        background = selectedBackgroundId,
        level = levelText.toIntOrNull() ?: 1,
        notes = text,
        imageUri = c.imageUri,
        strength = strengthText.toIntOrNull() ?: 10,
        dexterity = dexterityText.toIntOrNull() ?: 10,
        constitution = constitutionText.toIntOrNull() ?: 10,
        intelligence = intelligenceText.toIntOrNull() ?: 10,
        wisdom = wisdomText.toIntOrNull() ?: 10,
        charisma = charismaText.toIntOrNull() ?: 10,
        currentHp = vm.character.value.currentHp,
        armourBonus = vm.character.value.armourBonus,
        shieldBonus = vm.character.value.shieldBonus,
        inventory = vm.character.value.inventory,
        skillProficiencyIds = vm.character.value.skillProficiencyIds,
        racialSkillProficiencyIds = selectedRacialSkillIds.toList(),
        selectedRacialSkillIds = selectedRacialSkillIds.toList(),
        racialWeaponProficiencyIds = vm.character.value.racialWeaponProficiencyIds,
        racialArmourProficiencyIds = vm.character.value.racialArmourProficiencyIds,
        racialToolProficiencyIds = vm.character.value.racialToolProficiencyIds,
        racialTraitNames = vm.character.value.racialTraitNames,
        selectedRacialAbilityBonusIds = selectedFlexibleAbilityIds.toList(),
        selectedRacialLanguageIds = selectedRacialLanguageIds.toList(),
        racialSpellIds = vm.character.value.racialSpellIds,
        selectedRacialSpellId = selectedRacialSpellId,
        damageResistanceType = selectedRaceVariant?.damageResistanceType.orEmpty(),
        breathWeaponDamageType = selectedRaceVariant?.breathWeaponDamageType.orEmpty(),
        breathWeaponShape = selectedRaceVariant?.breathWeaponShape.orEmpty(),
        breathWeaponSaveAbility =
            selectedRaceVariant?.breathWeaponSaveAbility?.name.orEmpty(),
        toolProficiencyIds = vm.character.value.toolProficiencyIds,
        languages = vm.character.value.languages,
        goldPieces = vm.character.value.goldPieces,
        backgroundFeatureName = vm.character.value.backgroundFeatureName,
        backgroundFeatureDescription = vm.character.value.backgroundFeatureDescription,
        personalityTraits = vm.character.value.personalityTraits,
        ideal = vm.character.value.ideal,
        bond = vm.character.value.bond,
        flaw = vm.character.value.flaw,
        knownSpellIds = vm.character.value.knownSpellIds,
        preparedSpellIds = vm.character.value.preparedSpellIds
    )

    val isSpellcaster = CharacterStatEngine.build(previewCharacter).spellcastingAbilityLabel != null

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Edit Character", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            if (selectedImageUri != null || c.imageUri.isNotBlank()) {
                AsyncImage(
                    model = selectedImageUri ?: c.imageUri,
                    contentDescription = "Character Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.height(12.dp))
            }

            ShowPhotoPicker(
                onPhotoUriChanged = { selectedImageUri = it }
            )

            Spacer(Modifier.height(12.dp))

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

            selectedClass?.let { classDef ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Hit Die: d${classDef.hitDie} • Primary Stats: ${CompendiumLookup.formatAbilityList(classDef.primaryStats)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Spellcasting: ${classDef.spellcastingAbility?.let { CompendiumLookup.abilityLabel(it) } ?: "None"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (isSpellcaster) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onOpenSpellbook(c.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Spellbook")
                }
            }

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
                    selectedFlexibleAbilityIds.clear()
                    selectedRacialSkillIds.clear()
                    selectedRacialLanguageIds.clear()
                    selectedRacialSpellId = ""
                }
            )

            selectedRace?.let { raceDef ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = raceDef.description,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Bonuses: ${
                        CompendiumLookup.formatStatBonuses(
                            raceDef,
                            selectedRaceVariant
                        )
                    } • Speed: ${selectedRaceVariant?.speedOverride ?: raceDef.speed}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (raceVariants.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                CompendiumDropdown(
                    label = if (selectedRaceId == "dragonborn") {
                        "Draconic Ancestry"
                    } else {
                        "Subrace"
                    },
                    options = raceVariants.map {
                        CompendiumOption(it.id, it.name)
                    },
                    selectedId = selectedRaceVariantId,
                    onSelected = { variantId ->
                        selectedRaceVariantId = variantId
                        selectedFlexibleAbilityIds.clear()
                        selectedRacialSkillIds.clear()
                        selectedRacialLanguageIds.clear()
                        selectedRacialSpellId = ""
                    }
                )

                selectedRaceVariant?.let { variant ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        variant.description,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Traits: ${variant.passiveTraits.joinToString().ifBlank { "None" }}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (flexibleAbilityChoiceCount > 0) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Flexible Racial Ability Bonuses",
                    style = MaterialTheme.typography.titleSmall
                )
                repeat(flexibleAbilityChoiceCount) { index ->
                    Spacer(Modifier.height(6.dp))
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
                        }
                    )
                }
            }

            if (racialSkillChoiceCount > 0) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Racial Skill Choices",
                    style = MaterialTheme.typography.titleSmall
                )
                repeat(racialSkillChoiceCount) { index ->
                    Spacer(Modifier.height(6.dp))
                    val selectedId = selectedRacialSkillIds.getOrNull(index).orEmpty()
                    val blocked = selectedRacialSkillIds
                        .filterIndexed { otherIndex, _ -> otherIndex != index }
                        .toSet()
                    CompendiumDropdown(
                        label = "Racial Skill ${index + 1}",
                        options = racialSkillOptions
                            .filterNot { it in blocked }
                            .map { CompendiumOption(it, it) },
                        selectedId = selectedId,
                        onSelected = { skill ->
                            if (index < selectedRacialSkillIds.size) {
                                selectedRacialSkillIds[index] = skill
                            } else {
                                selectedRacialSkillIds.add(skill)
                            }
                        }
                    )
                }
            }

            if (racialLanguageChoiceCount > 0) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Racial Language Choices",
                    style = MaterialTheme.typography.titleSmall
                )
                repeat(racialLanguageChoiceCount) { index ->
                    Spacer(Modifier.height(6.dp))
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
                        }
                    )
                }
            }

            if (racialCantripOptions.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                CompendiumDropdown(
                    label = "Racial Cantrip",
                    options = racialCantripOptions.map {
                        CompendiumOption(it.id, it.name)
                    },
                    selectedId = selectedRacialSpellId,
                    onSelected = { selectedRacialSpellId = it }
                )
            }

            Spacer(Modifier.height(10.dp))

            CompendiumDropdown(
                label = "Background",
                options = backgrounds.map { CompendiumOption(it.id, it.name) },
                selectedId = selectedBackgroundId,
                onSelected = { selectedBackgroundId = it }
            )

            selectedBackground?.let { backgroundDef ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = backgroundDef.description,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Skills: ${backgroundDef.skillProficiencyIds.joinToString()} • Feature: ${backgroundDef.featureName}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = levelText,
                onValueChange = { levelText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Level (1–20)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Text("Ability Scores", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))

            AbilityScoreField("Strength", strengthText, { strengthText = it })
            Spacer(Modifier.height(8.dp))
            AbilityScoreField("Dexterity", dexterityText, { dexterityText = it })
            Spacer(Modifier.height(8.dp))
            AbilityScoreField("Constitution", constitutionText, { constitutionText = it })
            Spacer(Modifier.height(8.dp))
            AbilityScoreField("Intelligence", intelligenceText, { intelligenceText = it })
            Spacer(Modifier.height(8.dp))
            AbilityScoreField("Wisdom", wisdomText, { wisdomText = it })
            Spacer(Modifier.height(8.dp))
            AbilityScoreField("Charisma", charismaText, { charismaText = it })

            Spacer(Modifier.height(16.dp))

            CharacterEquipmentEditorCard(
                inventory = vm.character.value.inventory,
                onEquipItem = vm::equipItem,
                onUnequipWeapon = vm::unequipWeapon,
                onUnequipArmour = vm::unequipArmour,
                onUnequipOffhand = vm::unequipOffhand,
                onUnequipSpellFocus = vm::unequipSpellFocus
            )

            Spacer(Modifier.height(12.dp))

            CharacterInventoryEditorCard(
                inventory = vm.character.value.inventory,
                onRemoveItem = vm::removeItem,
                onAddTestPotion = vm::addTestPotion,
                onAddTestTool = vm::addTestTool,
                onAddTestShield = vm::addTestShield
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    val trimmed = it.trim()
                    isEmptyError = trimmed.isEmpty()
                    isShortError = trimmed.isNotEmpty() && trimmed.length < 2
                },
                label = { Text("Notes") },
                isError = isEmptyError || isShortError,
                modifier = Modifier.fillMaxWidth()
            )

            if (isEmptyError) {
                Text(errorEmptyNotes, color = MaterialTheme.colorScheme.error)
            } else if (isShortError) {
                Text(errorShortNotes, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))

            if (vm.isLoading.value) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            if (vm.isErr.value) {
                Text(vm.error.value.message ?: "Update failed", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    val trimmedNotes = text.trim()
                    isEmptyError = trimmedNotes.isEmpty()
                    isShortError = trimmedNotes.isNotEmpty() && trimmedNotes.length < 2
                    if (isEmptyError || isShortError) return@Button

                    val level = levelText.toIntOrNull()
                    val str = strengthText.toIntOrNull()
                    val dex = dexterityText.toIntOrNull()
                    val con = constitutionText.toIntOrNull()
                    val intScore = intelligenceText.toIntOrNull()
                    val wis = wisdomText.toIntOrNull()
                    val cha = charismaText.toIntOrNull()

                    when {
                        name.isBlank() -> error = "Name is required"
                        selectedClassId.isBlank() -> error = "Class is required"
                        selectedRaceId.isBlank() -> error = "Race is required"
                        raceVariants.isNotEmpty() && selectedRaceVariantId.isBlank() ->
                            error = "Ancestry is required"
                        selectedFlexibleAbilityIds.size != flexibleAbilityChoiceCount ->
                            error = "Choose every flexible racial ability bonus"
                        selectedRacialSkillIds.size != racialSkillChoiceCount ->
                            error = "Choose every racial skill"
                        selectedRacialLanguageIds.size != racialLanguageChoiceCount ->
                            error = "Choose every racial language"
                        racialCantripOptions.isNotEmpty() &&
                            selectedRacialSpellId.isBlank() ->
                            error = "Choose a racial cantrip"
                        selectedBackgroundId.isBlank() -> error = "Background is required"
                        level == null || level !in 1..20 -> error = "Level must be 1–20"
                        listOf(str, dex, con, intScore, wis, cha).any { it == null || it !in 1..20 } ->
                            error = "All ability scores must be between 1 and 20"
                        else -> {
                            error = null
                            vm.updateCharacter(
                                name = name.trim(),
                                characterClass = selectedClassId.trim(),
                                race = selectedRaceId.trim(),
                                raceVariant = selectedRaceVariantId.trim(),
                                background = selectedBackgroundId.trim(),
                                level = level,
                                notes = trimmedNotes,
                                strength = str!!,
                                dexterity = dex!!,
                                constitution = con!!,
                                intelligence = intScore!!,
                                wisdom = wis!!,
                                charisma = cha!!,
                                selectedRacialAbilityBonusIds =
                                    selectedFlexibleAbilityIds.toList(),
                                selectedRacialSkillIds =
                                    selectedRacialSkillIds.toList(),
                                selectedRacialLanguageIds =
                                    selectedRacialLanguageIds.toList(),
                                selectedRacialSpellId = selectedRacialSpellId,
                                imageUri = selectedImageUri,
                                onSuccess = onDone
                            )
                        }
                    }
                },
                enabled = !vm.isLoading.value,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}
