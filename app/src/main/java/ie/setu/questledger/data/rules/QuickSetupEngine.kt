package ie.setu.questledger.data.rules

import ie.setu.questledger.data.compendium.AbilityType
import ie.setu.questledger.data.compendium.ArmourDefinition
import ie.setu.questledger.data.compendium.BackgroundDefinition
import ie.setu.questledger.data.compendium.ClassDefinition
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.data.compendium.EquipmentDefinition
import ie.setu.questledger.data.compendium.EquipmentPackDefinition
import ie.setu.questledger.data.compendium.RaceDefinition
import ie.setu.questledger.data.compendium.RaceVariantDefinition
import ie.setu.questledger.data.compendium.SpellDefinition
import ie.setu.questledger.data.compendium.SubclassDefinition
import ie.setu.questledger.data.compendium.WeaponDefinition
import ie.setu.questledger.models.QuickSetupConfig
import ie.setu.questledger.models.characters.CharacterModel
import javax.inject.Inject
import javax.inject.Singleton

data class QuickSetupResult(
    val character: CharacterModel,
    val race: RaceDefinition,
    val raceVariant: RaceVariantDefinition?,
    val characterClass: ClassDefinition,
    val subclass: SubclassDefinition?,
    val background: BackgroundDefinition,
    val starterWeapon: WeaponDefinition?,
    val starterArmour: ArmourDefinition?,
    val starterPack: EquipmentPackDefinition?,
    val spellFocus: EquipmentDefinition?,
    val hasShield: Boolean,
    val starterSpells: List<SpellDefinition>,
    val racialSpells: List<SpellDefinition>,
    val subclassSpells: List<SpellDefinition>,
    val spellSlotsSummary: String,
    val summaryLines: List<String>
)

@Singleton
class QuickSetupEngine @Inject constructor(
    private val compendiumService: CompendiumService
) {

    fun build(config: QuickSetupConfig): QuickSetupResult {
        val race = requireNotNull(compendiumService.getRaceById(config.raceId)) {
            "Unknown race: ${config.raceId}"
        }
        val clazz = requireNotNull(compendiumService.getClassById(config.classId)) {
            "Unknown class: ${config.classId}"
        }
        val background = requireNotNull(compendiumService.getBackgroundById(config.backgroundId)) {
            "Unknown background: ${config.backgroundId}"
        }
        val level = config.level.coerceIn(1, 20)
        var subclassSelection = CharacterSubclassRules.resolve(
            characterClass = clazz,
            level = level,
            requestedSubclassId = config.subclassId,
            selectedChoiceIds = config.selectedSubclassChoiceIds,
            subclasses = compendiumService.getSubclasses(),
            spells = compendiumService.getSpells()
        )
        val raceSelection = CharacterRaceRules.resolve(
            race = race,
            requestedVariantId = config.raceVariantId,
            variantsForRace = compendiumService.getRaceVariantsForRace(race.id),
            characterClass = clazz,
            background = background,
            level = level,
            spells = compendiumService.getSpells(),
            selectedFlexibleAbilityIds = config.selectedFlexibleAbilityIds,
            selectedRacialSkillIds = config.selectedRacialSkillIds,
            selectedRacialLanguageIds = config.selectedRacialLanguageIds,
            selectedRacialSpellId = config.selectedRacialSpellId
        )
        if (config.selectedSubclassChoiceIds.isEmpty()) {
            val excludedSkillIds = (
                background.skillProficiencyIds +
                    raceSelection.allRacialSkillIds
                ).toMutableSet()
            val automaticChoiceIds = buildList {
                subclassSelection.subclass
                    ?.choiceGroups
                    .orEmpty()
                    .filter { it.minimumLevel <= level }
                    .forEach { group ->
                        group.options
                            .sortedBy { option ->
                                option.grantedSkillProficiencyId
                                    ?.let { it in excludedSkillIds } == true
                            }
                            .take(group.selectionCount)
                            .forEach { option ->
                                add(option.id)
                                option.grantedSkillProficiencyId?.let(excludedSkillIds::add)
                            }
                    }
            }
            if (automaticChoiceIds != subclassSelection.selectedChoiceIds) {
                subclassSelection = CharacterSubclassRules.resolve(
                    characterClass = clazz,
                    level = level,
                    requestedSubclassId = subclassSelection.subclass?.id.orEmpty(),
                    selectedChoiceIds = automaticChoiceIds,
                    subclasses = compendiumService.getSubclasses(),
                    spells = compendiumService.getSpells()
                )
            }
        }

        val baseScores = generateBaseScores(clazz.quickBuildAbilityPriority)
        val racialScores = AbilityScoreRules.applyRaceBonuses(
            baseScores = baseScores,
            race = race,
            raceVariant = raceSelection.variant,
            classPriority = clazz.quickBuildAbilityPriority,
            selectedFlexibleAbilities = raceSelection.flexibleAbilityIds.map(AbilityType::valueOf)
        )
        val advancementSelections = CharacterAdvancementRules.defaultSelections(
            characterClass = clazz,
            level = level,
            baseScores = racialScores,
            feats = compendiumService.getFeats(),
            raceVariant = raceSelection.variant
        )
        CharacterAdvancementRules.validateSelections(
            selections = advancementSelections,
            characterClass = clazz,
            raceVariant = raceSelection.variant,
            level = level,
            baseScores = racialScores,
            feats = compendiumService.getFeats(),
            racialArmourProficiencies = raceSelection.armourProficiencyIds
        )
        val scores = CharacterAdvancementRules.applyToScores(
            baseScores = racialScores,
            selections = advancementSelections,
            feats = compendiumService.getFeats()
        )

        val starterWeapon = clazz.defaultWeaponId?.let(compendiumService::getWeaponById)
        val starterArmour = clazz.defaultArmourId?.let(compendiumService::getArmourById)
        val starterPack = clazz.defaultPackId?.let(compendiumService::getEquipmentPackById)
        val spellFocus = clazz.defaultSpellFocusId?.let(compendiumService::getEquipmentById)
        val hasShield = clazz.startsWithShield
        val availableClassSpellIds = SpellRules.availableSpells(
            characterClass = clazz,
            characterLevel = level,
            spells = compendiumService.getSpells()
        ).mapTo(mutableSetOf(), SpellDefinition::id)
        val starterSpells = if (clazz.canCastAt(level)) {
            clazz.starterSpellIds
                .mapNotNull(compendiumService::getSpellById)
                .filter { it.id in availableClassSpellIds }
        } else {
            emptyList()
        }
        val racialSpells = raceSelection.racialSpellIds
            .mapNotNull(compendiumService::getSpellById)
        val subclassSpells = subclassSelection.alwaysPreparedSpellIds
            .mapNotNull(compendiumService::getSpellById)
        val allStarterSpells = (starterSpells + racialSpells + subclassSpells)
            .distinctBy { it.id }

        val inventory = StarterInventoryFactory.build(
            characterClass = clazz,
            background = background,
            starterWeapon = starterWeapon,
            starterArmour = starterArmour,
            hasShield = hasShield,
            starterSpells = allStarterSpells,
            strengthScore = scores.getValue(AbilityType.STRENGTH),
            starterPack = starterPack,
            spellFocus = spellFocus
        )

        val fixedCreationSkills = (
            background.skillProficiencyIds +
                raceSelection.allRacialSkillIds +
                subclassSelection.skillProficiencyIds
            ).distinct()
        val classSkills = clazz.skillProficiencies
            .filterNot { it in fixedCreationSkills }
            .take(clazz.skillChoiceCount)
        val selectedSkills = (fixedCreationSkills + classSkills).distinct()

        val characterDraft = CharacterModel(
            name = config.name.trim(),
            characterClass = clazz.id,
            subclass = subclassSelection.subclass?.id.orEmpty(),
            race = race.id,
            raceVariant = raceSelection.variant?.id.orEmpty(),
            background = background.id,
            level = level,
            strength = scores.getValue(AbilityType.STRENGTH),
            dexterity = scores.getValue(AbilityType.DEXTERITY),
            constitution = scores.getValue(AbilityType.CONSTITUTION),
            intelligence = scores.getValue(AbilityType.INTELLIGENCE),
            wisdom = scores.getValue(AbilityType.WISDOM),
            charisma = scores.getValue(AbilityType.CHARISMA),
            inventory = inventory,
            skillProficiencyIds = selectedSkills,
            racialSkillProficiencyIds = raceSelection.allRacialSkillIds,
            selectedRacialSkillIds = raceSelection.selectedSkillIds,
            racialWeaponProficiencyIds = raceSelection.weaponProficiencyIds,
            racialArmourProficiencyIds = raceSelection.armourProficiencyIds,
            racialToolProficiencyIds = raceSelection.toolProficiencyIds,
            racialTraitNames = raceSelection.traitNames,
            selectedRacialAbilityBonusIds = raceSelection.flexibleAbilityIds,
            selectedRacialLanguageIds = raceSelection.selectedLanguageIds,
            racialSpellIds = raceSelection.racialSpellIds,
            selectedRacialSpellId = raceSelection.selectedSpellId,
            damageResistanceType = raceSelection.variant?.damageResistanceType.orEmpty(),
            breathWeaponDamageType = raceSelection.variant?.breathWeaponDamageType.orEmpty(),
            breathWeaponShape = raceSelection.variant?.breathWeaponShape.orEmpty(),
            breathWeaponSaveAbility =
                raceSelection.variant?.breathWeaponSaveAbility?.name.orEmpty(),
            toolProficiencyIds = (
                background.toolProficiencies +
                    raceSelection.toolProficiencyIds
                ).distinct(),
            languages = (
                raceSelection.allRaceLanguages +
                    background.suggestedLanguages
                ).distinct(),
            goldPieces = background.startingGoldGp,
            backgroundFeatureName = background.featureName,
            backgroundFeatureDescription = background.featureDescription,
            personalityTraits = background.personalityTraits.take(2),
            ideal = background.ideals.first(),
            bond = background.bonds.first(),
            flaw = background.flaws.first(),
            knownSpellIds = allStarterSpells.map { it.id },
            preparedSpellIds = emptyList(),
            subclassSpellIds = subclassSelection.alwaysPreparedSpellIds,
            subclassSkillProficiencyIds = subclassSelection.skillProficiencyIds,
            selectedSubclassChoiceIds = subclassSelection.selectedChoiceIds,
            advancementSelections = advancementSelections
        )
        val character = characterDraft.copy(
            preparedSpellIds = SpellRules.initialPreparedSpellIds(
                character = characterDraft,
                characterClass = clazz,
                spells = starterSpells,
                alwaysPreparedSpellIds = subclassSelection.alwaysPreparedSpellIds
            )
        )
        val derived = CharacterStatEngine.build(character)
        val spellSlotsSummary = formatSpellSlots(derived.spellSlotsByLevel)

        val summaryLines = buildList {
            add("Quick Setup Build")
            add("Race: ${race.name}")
            raceSelection.variant?.let { add("Ancestry: ${it.name}") }
            add("Class: ${clazz.name}")
            subclassSelection.subclass?.let { add("Subclass: ${it.name}") }
            if (subclassSelection.selectedChoiceIds.isNotEmpty()) {
                add(
                    "Subclass Choices: ${
                        CharacterSubclassRules.choiceDisplayNames(character).joinToString()
                    }"
                )
            }
            add("Background: ${background.name}")
            if (advancementSelections.isNotEmpty()) {
                add(
                    "Advancement: ${
                        advancementSelections.joinToString { selection ->
                            if (selection.isFeat) {
                                compendiumService.getFeatById(selection.featId)?.name
                                    ?: selection.featId
                            } else {
                                selection.abilityIncreases.entries.joinToString(
                                    prefix = "ASI ",
                                    separator = "/"
                                ) { (ability, amount) -> "$ability +$amount" }
                            }
                        }
                    }"
                )
            }
            add("Skills: ${character.skillProficiencyIds.joinToString().ifBlank { "None" }}")
            add("Tools: ${character.toolProficiencyIds.joinToString().ifBlank { "None" }}")
            add("Languages: ${character.languages.joinToString().ifBlank { "None" }}")
            if (character.racialTraitNames.isNotEmpty()) {
                add("Racial Traits: ${character.racialTraitNames.joinToString()}")
            }
            if (character.racialWeaponProficiencyIds.isNotEmpty()) {
                add(
                    "Racial Weapon Training: ${
                        character.racialWeaponProficiencyIds.joinToString()
                    }"
                )
            }
            if (character.racialArmourProficiencyIds.isNotEmpty()) {
                add(
                    "Racial Armour Training: ${
                        character.racialArmourProficiencyIds.joinToString()
                    }"
                )
            }
            if (character.damageResistanceType.isNotBlank()) {
                add("Damage Resistance: ${character.damageResistanceType}")
            }
            if (character.breathWeaponDamageType.isNotBlank()) {
                add(
                    "Breath Weapon: ${character.breathWeaponDamageType} • " +
                        "${character.breathWeaponShape} • " +
                        "${character.breathWeaponSaveAbility} save"
                )
            }
            add("Background Feature: ${background.featureName}")
            add("Starting Gold: ${character.goldPieces} gp")
            add("Starter Weapon: ${starterWeapon?.name ?: "None"}")
            add("Starter Armour: ${starterArmour?.name ?: "None"}")
            add("Equipment Pack: ${starterPack?.name ?: "None"}")
            add("Spell Focus: ${spellFocus?.name ?: "None"}")
            add("Shield: ${if (hasShield) "Yes" else "No"}")
            add("HP Max: ${derived.maxHp}")
            add("AC: ${derived.armourClass}")
            add("Proficiency Bonus: +${derived.proficiencyBonus}")
            add(
                "Initiative: ${
                    if (derived.initiativeBonus >= 0) "+${derived.initiativeBonus}"
                    else derived.initiativeBonus
                }"
            )
            add("Passive Perception: ${derived.passivePerception}")
            if (starterSpells.isNotEmpty()) {
                add("Starter Spells: ${starterSpells.joinToString { it.name }}")
            }
            if (racialSpells.isNotEmpty()) {
                add("Racial Spells: ${racialSpells.joinToString { it.name }}")
            }
            if (subclassSpells.isNotEmpty()) {
                add("Subclass Spells: ${subclassSpells.joinToString { it.name }}")
            }
            add("Spell Slots: $spellSlotsSummary")
            if (derived.unlockedFeatures.isNotEmpty()) {
                add("Unlocked Features: ${derived.unlockedFeatures.joinToString()}")
            }
            add("Personality: ${character.personalityTraits.joinToString(" • ")}")
            add("Ideal: ${character.ideal}")
            add("Bond: ${character.bond}")
            add("Flaw: ${character.flaw}")
        }

        val playableCharacter = CharacterSessionRules.initialise(
            character.copy(notes = summaryLines.joinToString("\n"))
        )

        return QuickSetupResult(
            character = playableCharacter,
            race = race,
            raceVariant = raceSelection.variant,
            characterClass = clazz,
            subclass = subclassSelection.subclass,
            background = background,
            starterWeapon = starterWeapon,
            starterArmour = starterArmour,
            starterPack = starterPack,
            spellFocus = spellFocus,
            hasShield = hasShield,
            starterSpells = starterSpells,
            racialSpells = racialSpells,
            subclassSpells = subclassSpells,
            spellSlotsSummary = spellSlotsSummary,
            summaryLines = summaryLines
        )
    }

    private fun generateBaseScores(priority: List<AbilityType>): Map<AbilityType, Int> {
        require(priority.size == 6 && priority.distinct().size == 6) {
            "Quick-build ability priority must contain every ability exactly once"
        }
        val scores = listOf(15, 14, 13, 12, 10, 8)
        return priority.mapIndexed { index, ability -> ability to scores[index] }.toMap()
    }

    private fun formatSpellSlots(slots: List<Int>): String {
        return slots.mapIndexedNotNull { index, count ->
            if (count > 0) "L${index + 1} x$count" else null
        }.joinToString(", ").ifBlank { "None" }
    }
}
