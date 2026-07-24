package ie.setu.questledger.data.rules

import ie.setu.questledger.data.compendium.AbilityType
import ie.setu.questledger.data.compendium.ArmourDefinition
import ie.setu.questledger.data.compendium.ClassDefinition
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.data.compendium.RaceDefinition
import ie.setu.questledger.data.compendium.SpellDefinition
import ie.setu.questledger.data.compendium.WeaponDefinition
import ie.setu.questledger.models.QuickSetupConfig
import ie.setu.questledger.models.characters.CharacterModel
import javax.inject.Inject
import javax.inject.Singleton

data class QuickSetupResult(
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
        val level = config.level.coerceIn(1, 20)

        val baseScores = generateBaseScores(clazz.quickBuildAbilityPriority)
        val scores = AbilityScoreRules.applyRaceBonuses(
            baseScores = baseScores,
            race = race,
            classPriority = clazz.quickBuildAbilityPriority
        )

        val starterWeapon = clazz.defaultWeaponId?.let(compendiumService::getWeaponById)
        val starterArmour = clazz.defaultArmourId?.let(compendiumService::getArmourById)
        val hasShield = clazz.startsWithShield
        val starterSpells = if (clazz.canCastAt(level)) {
            clazz.starterSpellIds
                .mapNotNull(compendiumService::getSpellById)
                .filter { clazz.id in it.classIds }
        } else {
            emptyList()
        }

        val inventory = StarterInventoryFactory.build(
            characterClass = clazz,
            starterWeapon = starterWeapon,
            starterArmour = starterArmour,
            hasShield = hasShield,
            starterSpells = starterSpells,
            strengthScore = scores.getValue(AbilityType.STRENGTH)
        )

        val character = CharacterModel(
            name = config.name.trim(),
            characterClass = clazz.id,
            race = race.id,
            level = level,
            strength = scores.getValue(AbilityType.STRENGTH),
            dexterity = scores.getValue(AbilityType.DEXTERITY),
            constitution = scores.getValue(AbilityType.CONSTITUTION),
            intelligence = scores.getValue(AbilityType.INTELLIGENCE),
            wisdom = scores.getValue(AbilityType.WISDOM),
            charisma = scores.getValue(AbilityType.CHARISMA),
            inventory = inventory,
            skillProficiencyIds = clazz.skillProficiencies.take(clazz.skillChoiceCount),
            knownSpellIds = starterSpells.map { it.id },
            preparedSpellIds = starterSpells.map { it.id }
        )
        val derived = CharacterStatEngine.build(character)
        val spellSlotsSummary = formatSpellSlots(derived.spellSlotsByLevel)

        val summaryLines = buildList {
            add("Quick Setup Build")
            add("Race: ${race.name}")
            add("Class: ${clazz.name}")
            add("Skills: ${character.skillProficiencyIds.joinToString().ifBlank { "None" }}")
            add("Starter Weapon: ${starterWeapon?.name ?: "None"}")
            add("Starter Armour: ${starterArmour?.name ?: "None"}")
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
            add("Spell Slots: $spellSlotsSummary")
            if (derived.unlockedFeatures.isNotEmpty()) {
                add("Unlocked Features: ${derived.unlockedFeatures.joinToString()}")
            }
        }

        return QuickSetupResult(
            character = character.copy(
                currentHp = derived.maxHp,
                notes = summaryLines.joinToString("\n")
            ),
            race = race,
            characterClass = clazz,
            starterWeapon = starterWeapon,
            starterArmour = starterArmour,
            hasShield = hasShield,
            starterSpells = starterSpells,
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
