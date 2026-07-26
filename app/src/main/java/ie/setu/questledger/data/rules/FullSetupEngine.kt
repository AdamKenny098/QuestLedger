package ie.setu.questledger.data.rules

import ie.setu.questledger.data.compendium.AbilityType
import ie.setu.questledger.data.compendium.ArmourDefinition
import ie.setu.questledger.data.compendium.ArmourType
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
import ie.setu.questledger.models.FullSetupConfig
import ie.setu.questledger.models.characters.CharacterModel
import javax.inject.Inject
import javax.inject.Singleton

data class FullSetupResult(
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
class FullSetupEngine @Inject constructor(
    private val compendiumService: CompendiumService
) {

    fun build(config: FullSetupConfig): FullSetupResult {
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
        val subclassSelection = CharacterSubclassRules.resolve(
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

        validateScores(config)
        validateProficiencies(
            config = config,
            clazz = clazz,
            background = background,
            racialSkillIds = (
                raceSelection.allRacialSkillIds +
                    subclassSelection.skillProficiencyIds
                ).distinct()
        )
        validateGear(config, clazz)
        validateSpellSelection(
            config,
            clazz,
            level,
            subclassSelection.expandedSpellIds
        )
        validateBackgroundChoices(config, background)

        val starterWeapon = config.starterWeaponId?.let { weaponId ->
            requireNotNull(compendiumService.getWeaponById(weaponId)) {
                "Unknown starter weapon: $weaponId"
            }
        }
        val starterArmour = config.starterArmourId?.let { armourId ->
            requireNotNull(compendiumService.getArmourById(armourId)) {
                "Unknown starter armour: $armourId"
            }
        }
        val starterPack = config.starterPackId?.let { packId ->
            requireNotNull(compendiumService.getEquipmentPackById(packId)) {
                "Unknown starter equipment pack: $packId"
            }
        }
        val spellFocus = clazz.defaultSpellFocusId?.let { focusId ->
            requireNotNull(compendiumService.getEquipmentById(focusId)) {
                "Unknown spell focus: $focusId"
            }
        }
        val starterSpells = config.starterSpellIds.map { spellId ->
            requireNotNull(compendiumService.getSpellById(spellId)) {
                "Unknown starter spell: $spellId"
            }
        }
        val racialSpells = raceSelection.racialSpellIds.map { spellId ->
            requireNotNull(compendiumService.getSpellById(spellId)) {
                "Unknown racial spell: $spellId"
            }
        }
        val subclassSpells = subclassSelection.alwaysPreparedSpellIds.map { spellId ->
            requireNotNull(compendiumService.getSpellById(spellId)) {
                "Unknown subclass spell: $spellId"
            }
        }
        val allKnownSpells = (starterSpells + racialSpells + subclassSpells)
            .distinctBy { it.id }

        val racialScores = AbilityScoreRules.applyRaceBonuses(
            baseScores = mapOf(
                AbilityType.STRENGTH to config.strength,
                AbilityType.DEXTERITY to config.dexterity,
                AbilityType.CONSTITUTION to config.constitution,
                AbilityType.INTELLIGENCE to config.intelligence,
                AbilityType.WISDOM to config.wisdom,
                AbilityType.CHARISMA to config.charisma
            ),
            race = race,
            raceVariant = raceSelection.variant,
            classPriority = clazz.quickBuildAbilityPriority,
            selectedFlexibleAbilities = raceSelection.flexibleAbilityIds.map {
                AbilityType.valueOf(it)
            }
        )
        CharacterAdvancementRules.validateSelections(
            selections = config.advancementSelections,
            characterClass = clazz,
            raceVariant = raceSelection.variant,
            level = level,
            baseScores = racialScores,
            feats = compendiumService.getFeats(),
            racialArmourProficiencies = raceSelection.armourProficiencyIds
        )
        val finalScores = CharacterAdvancementRules.applyToScores(
            baseScores = racialScores,
            selections = config.advancementSelections,
            feats = compendiumService.getFeats()
        )

        val inventory = StarterInventoryFactory.build(
            characterClass = clazz,
            background = background,
            starterWeapon = starterWeapon,
            starterArmour = starterArmour,
            hasShield = config.hasShield,
            starterSpells = allKnownSpells,
            strengthScore = finalScores.getValue(AbilityType.STRENGTH),
            starterPack = starterPack,
            spellFocus = spellFocus
        )

        val personalityTraits = config.personalityTraits.ifEmpty {
            background.personalityTraits.take(2)
        }
        val ideal = config.ideal.ifBlank { background.ideals.first() }
        val bond = config.bond.ifBlank { background.bonds.first() }
        val flaw = config.flaw.ifBlank { background.flaws.first() }
        val selectedSkills = (
            background.skillProficiencyIds +
                raceSelection.allRacialSkillIds +
                subclassSelection.skillProficiencyIds +
                config.selectedProficiencyIds
            ).distinct()

        val characterDraft = CharacterModel(
            name = config.name.trim(),
            characterClass = clazz.id,
            subclass = subclassSelection.subclass?.id.orEmpty(),
            race = race.id,
            raceVariant = raceSelection.variant?.id.orEmpty(),
            background = background.id,
            level = level,
            strength = finalScores.getValue(AbilityType.STRENGTH),
            dexterity = finalScores.getValue(AbilityType.DEXTERITY),
            constitution = finalScores.getValue(AbilityType.CONSTITUTION),
            intelligence = finalScores.getValue(AbilityType.INTELLIGENCE),
            wisdom = finalScores.getValue(AbilityType.WISDOM),
            charisma = finalScores.getValue(AbilityType.CHARISMA),
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
            personalityTraits = personalityTraits,
            ideal = ideal,
            bond = bond,
            flaw = flaw,
            knownSpellIds = allKnownSpells.map { it.id },
            preparedSpellIds = emptyList(),
            subclassSpellIds = subclassSelection.alwaysPreparedSpellIds,
            subclassSkillProficiencyIds = subclassSelection.skillProficiencyIds,
            selectedSubclassChoiceIds = subclassSelection.selectedChoiceIds,
            advancementSelections = config.advancementSelections
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
            add("Full Setup Build")
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
            if (character.advancementSelections.isNotEmpty()) {
                add(
                    "Advancement: ${
                        character.advancementSelections.joinToString { selection ->
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
            add(
                "Final Stats: STR ${character.strength}, DEX ${character.dexterity}, " +
                    "CON ${character.constitution}, INT ${character.intelligence}, " +
                    "WIS ${character.wisdom}, CHA ${character.charisma}"
            )
            add("Skills: ${character.skillProficiencyIds.joinToString().ifBlank { "None" }}")
            add("Tools: ${character.toolProficiencyIds.joinToString().ifBlank { "None" }}")
            add("Languages: ${character.languages.joinToString().ifBlank { "None" }}")
            if (character.racialTraitNames.isNotEmpty()) {
                add("Racial Traits: ${character.racialTraitNames.joinToString()}")
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
            add("Shield: ${if (config.hasShield) "Yes" else "No"}")
            add("HP Max: ${derived.maxHp}")
            add("AC: ${derived.armourClass}")
            add("Melee Attack: ${formatSigned(derived.meleeAttackBonus)}")
            add("Ranged Attack: ${formatSigned(derived.rangedAttackBonus)}")
            add("Initiative: ${formatSigned(derived.initiativeBonus)}")
            add("Passive Perception: ${derived.passivePerception}")
            add("Carry Capacity: ${derived.carryCapacity}")
            add("Inventory Capacity: ${derived.inventoryCapacity}")
            add("Speed: ${derived.speed}")
            add("Hit Die: d${derived.hitDie}")
            add("Proficiency Bonus: +${derived.proficiencyBonus}")
            if (starterSpells.isNotEmpty()) {
                add("Starter Spells: ${starterSpells.joinToString { it.name }}")
            }
            if (racialSpells.isNotEmpty()) {
                add("Racial Spells: ${racialSpells.joinToString { it.name }}")
            }
            if (subclassSpells.isNotEmpty()) {
                add("Subclass Spells: ${subclassSpells.joinToString { it.name }}")
            }
            if (derived.spellcastingAbilityLabel != null) {
                add("Spell Attack: ${formatSigned(derived.spellAttackBonus)}")
                add("Spell Save DC: ${derived.spellSaveDc}")
            }
            if (derived.unlockedFeatures.isNotEmpty()) {
                add("Unlocked Features: ${derived.unlockedFeatures.joinToString()}")
            }
            add("Spell Slots: $spellSlotsSummary")
            add("Personality: ${character.personalityTraits.joinToString(" • ")}")
            add("Ideal: ${character.ideal}")
            add("Bond: ${character.bond}")
            add("Flaw: ${character.flaw}")
        }

        val playableCharacter = CharacterSessionRules.initialise(
            character.copy(notes = summaryLines.joinToString("\n"))
        )

        return FullSetupResult(
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
            hasShield = config.hasShield,
            starterSpells = starterSpells,
            racialSpells = racialSpells,
            subclassSpells = subclassSpells,
            spellSlotsSummary = spellSlotsSummary,
            summaryLines = summaryLines
        )
    }

    private fun validateScores(config: FullSetupConfig) {
        require(
            listOf(
                config.strength,
                config.dexterity,
                config.constitution,
                config.intelligence,
                config.wisdom,
                config.charisma
            ).all { it in 8..18 }
        ) {
            "Ability scores must stay between 8 and 18 before racial bonuses"
        }
    }

    private fun validateProficiencies(
        config: FullSetupConfig,
        clazz: ClassDefinition,
        background: BackgroundDefinition,
        racialSkillIds: List<String>
    ) {
        require(config.selectedProficiencyIds.distinct().size == config.selectedProficiencyIds.size) {
            "A skill proficiency cannot be selected twice"
        }
        require(config.selectedProficiencyIds.size <= clazz.skillChoiceCount) {
            "${clazz.name} can choose ${clazz.skillChoiceCount} skill proficiencies"
        }
        require(config.selectedProficiencyIds.all { it in clazz.skillProficiencies }) {
            "One or more selected skills are not available to ${clazz.name}"
        }
        require(
            config.selectedProficiencyIds.none {
                it in background.skillProficiencyIds || it in racialSkillIds
            }
        ) {
            "Choose a different class skill when the background or race already grants it"
        }
    }

    private fun validateBackgroundChoices(
        config: FullSetupConfig,
        background: BackgroundDefinition
    ) {
        val personalityTraits = config.personalityTraits.ifEmpty {
            background.personalityTraits.take(2)
        }
        val ideal = config.ideal.ifBlank { background.ideals.first() }
        val bond = config.bond.ifBlank { background.bonds.first() }
        val flaw = config.flaw.ifBlank { background.flaws.first() }

        require(personalityTraits.size == 2 && personalityTraits.distinct().size == 2) {
            "Choose two different personality traits"
        }
        require(personalityTraits.all { it in background.personalityTraits }) {
            "One or more personality traits do not belong to ${background.name}"
        }
        require(ideal in background.ideals) {
            "Selected ideal does not belong to ${background.name}"
        }
        require(bond in background.bonds) {
            "Selected bond does not belong to ${background.name}"
        }
        require(flaw in background.flaws) {
            "Selected flaw does not belong to ${background.name}"
        }
    }

    private fun validateGear(config: FullSetupConfig, clazz: ClassDefinition) {
        require(config.starterWeaponId == null || config.starterWeaponId in clazz.starterWeaponIds) {
            "Selected weapon is not a ${clazz.name} starter option"
        }
        require(config.starterArmourId == null || config.starterArmourId in clazz.starterArmourIds) {
            "Selected armour is not a ${clazz.name} starter option"
        }
        require(config.starterPackId == null || config.starterPackId in clazz.starterPackIds) {
            "Selected equipment pack is not a ${clazz.name} starter option"
        }
        require(!config.hasShield || ArmourType.SHIELD in clazz.armourProficiencies) {
            "${clazz.name} is not proficient with shields"
        }
    }

    private fun validateSpellSelection(
        config: FullSetupConfig,
        clazz: ClassDefinition,
        level: Int,
        expandedSubclassSpellIds: List<String>
    ) {
        if (!clazz.canCastAt(level)) {
            require(config.starterSpellIds.isEmpty()) {
                "${clazz.name} does not cast spells at level $level"
            }
            return
        }

        val spells = config.starterSpellIds.mapNotNull(compendiumService::getSpellById)
        require(spells.size == config.starterSpellIds.size) {
            "One or more selected spells do not exist"
        }
        require(spells.map(SpellDefinition::id).distinct().size == spells.size) {
            "A spell cannot be selected twice"
        }
        val availableSpellIds = SpellRules.availableSpells(
            characterClass = clazz,
            characterLevel = level,
            spells = compendiumService.getSpells()
        ).mapTo(mutableSetOf(), SpellDefinition::id)
        availableSpellIds += expandedSubclassSpellIds
        require(spells.all { it.id in availableSpellIds }) {
            "One or more selected spells are not available to ${clazz.name} at level $level"
        }
        val limits = SpellRules.startingLimits(clazz)
        require(spells.count(SpellDefinition::isCantrip) <= limits.cantrips) {
            "${clazz.name} can choose ${limits.cantrips} starting cantrips"
        }
        require(spells.count { !it.isCantrip } <= limits.levelledSpells) {
            "${clazz.name} can choose ${limits.levelledSpells} starting levelled spells"
        }
    }

    private fun formatSpellSlots(slots: List<Int>): String {
        return slots.mapIndexedNotNull { index, count ->
            if (count > 0) "L${index + 1} x$count" else null
        }.joinToString(", ").ifBlank { "None" }
    }

    private fun formatSigned(value: Int): String {
        return if (value >= 0) "+$value" else value.toString()
    }
}
