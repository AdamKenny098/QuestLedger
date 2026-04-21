package ie.setu.questledger.data.rules

import ie.setu.questledger.data.compendium.AbilityType
import ie.setu.questledger.data.compendium.ArmourDefinition
import ie.setu.questledger.data.compendium.ClassDefinition
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.data.compendium.RaceDefinition
import ie.setu.questledger.data.compendium.SpellDefinition
import ie.setu.questledger.data.compendium.WeaponDefinition
import ie.setu.questledger.models.CharacterModel
import ie.setu.questledger.models.FullSetupConfig
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

        validateScores(config)
        validateSpellSelection(config, clazz)

        val starterWeapon = config.starterWeaponId?.let { compendiumService.getWeaponById(it) }
        val starterArmour = config.starterArmourId?.let { compendiumService.getArmourById(it) }
        val starterSpells = config.starterSpellIds.mapNotNull { compendiumService.getSpellById(it) }

        val character = CharacterModel(
            name = config.name.trim(),
            characterClass = clazz.id,
            race = race.id,
            level = config.level.coerceIn(1, 20),
            notes = "",
            strength = config.strength,
            dexterity = config.dexterity,
            constitution = config.constitution,
            intelligence = config.intelligence,
            wisdom = config.wisdom,
            charisma = config.charisma,
            armourBonus = starterArmour?.let { armourBonusFromDefinition(it) } ?: 0,
            shieldBonus = if (config.hasShield) 2 else 0
        )

        val derived = CharacterStatEngine.build(character)

        val spellSlotsSummary = formatSpellSlots(clazz, config.level)
        val summaryLines = buildList {
            add("Full Setup Build")
            add("Race: ${race.name}")
            add("Class: ${clazz.name}")
            add(
                "Stats: STR ${config.strength}, DEX ${config.dexterity}, CON ${config.constitution}, " +
                        "INT ${config.intelligence}, WIS ${config.wisdom}, CHA ${config.charisma}"
            )

            if (config.selectedProficiencyIds.isNotEmpty()) {
                add("Proficiencies: ${config.selectedProficiencyIds.joinToString()}")
            }

            add("Starter Weapon: ${starterWeapon?.name ?: "None"}")
            add("Starter Armour: ${starterArmour?.name ?: "None"}")
            add("Shield: ${if (config.hasShield) "Yes" else "No"}")

            if (starterSpells.isNotEmpty()) {
                add("Starter Spells: ${starterSpells.joinToString { it.name }}")
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
        val scores = listOf(
            config.strength,
            config.dexterity,
            config.constitution,
            config.intelligence,
            config.wisdom,
            config.charisma
        )

        require(scores.all { it in 8..18 }) {
            "Ability scores must stay between 8 and 18"
        }
    }

    private fun validateSpellSelection(
        config: FullSetupConfig,
        clazz: ClassDefinition
    ) {
        val usesSpells = clazz.spellcastingAbility != AbilityType.NONE

        if (!usesSpells && config.starterSpellIds.isNotEmpty()) {
            throw IllegalArgumentException("This class should not start with spells")
        }
    }

    private fun formatSpellSlots(clazz: ClassDefinition, level: Int): String {
        val slots = clazz.spellSlotProgression[level.coerceIn(1, 20)] ?: emptyList()
        if (slots.isEmpty()) return "None"

        return slots.mapIndexedNotNull { index, count ->
            if (count > 0) "L${index + 1} x$count" else null
        }.joinToString(", ")
    }

    private fun armourBonusFromDefinition(armour: ArmourDefinition): Int {
        return (armour.baseAc - 10).coerceAtLeast(0)
    }
}