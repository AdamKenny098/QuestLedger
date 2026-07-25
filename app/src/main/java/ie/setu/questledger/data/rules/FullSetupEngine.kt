package ie.setu.questledger.data.rules

import ie.setu.questledger.data.compendium.AbilityType
import ie.setu.questledger.data.compendium.ArmourDefinition
import ie.setu.questledger.data.compendium.ArmourType
import ie.setu.questledger.data.compendium.BackgroundDefinition
import ie.setu.questledger.data.compendium.ClassDefinition
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.data.compendium.RaceDefinition
import ie.setu.questledger.data.compendium.RaceVariantDefinition
import ie.setu.questledger.data.compendium.SpellDefinition
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
    val background: BackgroundDefinition,
    val starterWeapon: WeaponDefinition?,
    val starterArmour: ArmourDefinition?,
    val hasShield: Boolean,
    val starterSpells: List<SpellDefinition>,
    val racialSpells: List<SpellDefinition>,
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
            racialSkillIds = raceSelection.allRacialSkillIds
        )
        validateGear(config, clazz)
        validateSpellSelection(config, clazz, level)
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
        val allKnownSpells = (starterSpells + racialSpells).distinctBy { it.id }

        val finalScores = AbilityScoreRules.applyRaceBonuses(
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

        val inventory = StarterInventoryFactory.build(
            characterClass = clazz,
            background = background,
            starterWeapon = starterWeapon,
            starterArmour = starterArmour,
            hasShield = config.hasShield,
            starterSpells = allKnownSpells,
            strengthScore = finalScores.getValue(AbilityType.STRENGTH)
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
                config.selectedProficiencyIds
            ).distinct()

        val character = CharacterModel(
            name = config.name.trim(),
            characterClass = clazz.id,
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
            preparedSpellIds = starterSpells.map { it.id }
        )

        val derived = CharacterStatEngine.build(character)
        val spellSlotsSummary = formatSpellSlots(derived.spellSlotsByLevel)

        val summaryLines = buildList {
            add("Full Setup Build")
            add("Race: ${race.name}")
            raceSelection.variant?.let { add("Ancestry: ${it.name}") }
            add("Class: ${clazz.name}")
            add("Background: ${background.name}")
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

        return FullSetupResult(
            character = character.copy(
                currentHp = derived.maxHp,
                notes = summaryLines.joinToString("\n")
            ),
            race = race,
            raceVariant = raceSelection.variant,
            characterClass = clazz,
            background = background,
            starterWeapon = starterWeapon,
            starterArmour = starterArmour,
            hasShield = config.hasShield,
            starterSpells = starterSpells,
            racialSpells = racialSpells,
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
        require(!config.hasShield || ArmourType.SHIELD in clazz.armourProficiencies) {
            "${clazz.name} is not proficient with shields"
        }
    }

    private fun validateSpellSelection(
        config: FullSetupConfig,
        clazz: ClassDefinition,
        level: Int
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
        require(spells.all { clazz.id in it.classIds }) {
            "One or more selected spells are not available to ${clazz.name}"
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
