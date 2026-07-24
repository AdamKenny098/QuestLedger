package ie.setu.questledger.data.rules

import ie.setu.questledger.data.compendium.AbilityType
import ie.setu.questledger.data.compendium.ArmourDefinition
import ie.setu.questledger.data.compendium.ArmourType
import ie.setu.questledger.data.compendium.ClassDefinition
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.data.compendium.RaceDefinition
import ie.setu.questledger.data.compendium.SpellDefinition
import ie.setu.questledger.data.compendium.WeaponDefinition
import ie.setu.questledger.models.FullSetupConfig
import ie.setu.questledger.models.characters.CharacterModel
import javax.inject.Inject
import javax.inject.Singleton

data class FullSetupResult(
    val character: CharacterModel,
    val race: RaceDefinition,
    val characterClass: ClassDefinition,
    val starterWeapon: WeaponDefinition?,
    val starterArmour: ArmourDefinition?,
    val hasShield: Boolean,
    val starterSpells: List<SpellDefinition>,
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
        val level = config.level.coerceIn(1, 20)

        validateScores(config)
        validateProficiencies(config, clazz)
        validateGear(config, clazz)
        validateSpellSelection(config, clazz, level)

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
            classPriority = clazz.quickBuildAbilityPriority
        )

        val inventory = StarterInventoryFactory.build(
            characterClass = clazz,
            starterWeapon = starterWeapon,
            starterArmour = starterArmour,
            hasShield = config.hasShield,
            starterSpells = starterSpells,
            strengthScore = finalScores.getValue(AbilityType.STRENGTH)
        )

        val character = CharacterModel(
            name = config.name.trim(),
            characterClass = clazz.id,
            race = race.id,
            level = level,
            strength = finalScores.getValue(AbilityType.STRENGTH),
            dexterity = finalScores.getValue(AbilityType.DEXTERITY),
            constitution = finalScores.getValue(AbilityType.CONSTITUTION),
            intelligence = finalScores.getValue(AbilityType.INTELLIGENCE),
            wisdom = finalScores.getValue(AbilityType.WISDOM),
            charisma = finalScores.getValue(AbilityType.CHARISMA),
            inventory = inventory,
            skillProficiencyIds = config.selectedProficiencyIds,
            knownSpellIds = starterSpells.map { it.id },
            preparedSpellIds = starterSpells.map { it.id }
        )

        val derived = CharacterStatEngine.build(character)
        val spellSlotsSummary = formatSpellSlots(derived.spellSlotsByLevel)

        val summaryLines = buildList {
            add("Full Setup Build")
            add("Race: ${race.name}")
            add("Class: ${clazz.name}")
            add(
                "Final Stats: STR ${character.strength}, DEX ${character.dexterity}, " +
                    "CON ${character.constitution}, INT ${character.intelligence}, " +
                    "WIS ${character.wisdom}, CHA ${character.charisma}"
            )
            if (config.selectedProficiencyIds.isNotEmpty()) {
                add("Proficiencies: ${config.selectedProficiencyIds.joinToString()}")
            }
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
            if (derived.spellcastingAbilityLabel != null) {
                add("Spell Attack: ${formatSigned(derived.spellAttackBonus)}")
                add("Spell Save DC: ${derived.spellSaveDc}")
            }
            if (derived.unlockedFeatures.isNotEmpty()) {
                add("Unlocked Features: ${derived.unlockedFeatures.joinToString()}")
            }
            add("Spell Slots: $spellSlotsSummary")
        }

        return FullSetupResult(
            character = character.copy(
                currentHp = derived.maxHp,
                notes = summaryLines.joinToString("\n")
            ),
            race = race,
            characterClass = clazz,
            starterWeapon = starterWeapon,
            starterArmour = starterArmour,
            hasShield = config.hasShield,
            starterSpells = starterSpells,
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

    private fun validateProficiencies(config: FullSetupConfig, clazz: ClassDefinition) {
        require(config.selectedProficiencyIds.distinct().size == config.selectedProficiencyIds.size) {
            "A skill proficiency cannot be selected twice"
        }
        require(config.selectedProficiencyIds.size <= clazz.skillChoiceCount) {
            "${clazz.name} can choose ${clazz.skillChoiceCount} skill proficiencies"
        }
        require(config.selectedProficiencyIds.all { it in clazz.skillProficiencies }) {
            "One or more selected skills are not available to ${clazz.name}"
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
