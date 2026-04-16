package ie.setu.questledger.data.rules

import ie.setu.questledger.data.compendium.AbilityType
import ie.setu.questledger.data.compendium.CompendiumLookup
import ie.setu.questledger.models.CharacterModel
import kotlin.math.floor

data class CharacterDerivedStats(
    val strengthScore: Int,
    val dexterityScore: Int,
    val constitutionScore: Int,
    val intelligenceScore: Int,
    val wisdomScore: Int,
    val charismaScore: Int,
    val strMod: Int,
    val dexMod: Int,
    val conMod: Int,
    val intMod: Int,
    val wisMod: Int,
    val chaMod: Int,
    val proficiencyBonus: Int,
    val hitDie: Int,
    val speed: Int,
    val maxHp: Int,
    val armourClass: Int,
    val initiativeBonus: Int,
    val carryCapacity: Int,
    val spellAttackBonus: Int,
    val spellSaveDc: Int
)

object CharacterStatEngine {

    fun abilityModifier(score: Int): Int {
        return floor((score - 10) / 2.0).toInt()
    }

    fun proficiencyBonus(level: Int): Int {
        return when {
            level <= 4 -> 2
            level <= 8 -> 3
            level <= 12 -> 4
            level <= 16 -> 5
            else -> 6
        }
    }

    fun build(character: CharacterModel): CharacterDerivedStats {
        val level = character.level.coerceAtLeast(1)

        val raceDefinition = CompendiumLookup.findRace(character.race)
        val classDefinition = CompendiumLookup.findClass(character.characterClass)

        fun raceBonus(ability: AbilityType): Int {
            return raceDefinition?.statBonuses?.get(ability) ?: 0
        }

        val strengthScore = (character.strength + raceBonus(AbilityType.STRENGTH)).coerceAtLeast(1)
        val dexterityScore = (character.dexterity + raceBonus(AbilityType.DEXTERITY)).coerceAtLeast(1)
        val constitutionScore = (character.constitution + raceBonus(AbilityType.CONSTITUTION)).coerceAtLeast(1)
        val intelligenceScore = (character.intelligence + raceBonus(AbilityType.INTELLIGENCE)).coerceAtLeast(1)
        val wisdomScore = (character.wisdom + raceBonus(AbilityType.WISDOM)).coerceAtLeast(1)
        val charismaScore = (character.charisma + raceBonus(AbilityType.CHARISMA)).coerceAtLeast(1)

        val strMod = abilityModifier(strengthScore)
        val dexMod = abilityModifier(dexterityScore)
        val conMod = abilityModifier(constitutionScore)
        val intMod = abilityModifier(intelligenceScore)
        val wisMod = abilityModifier(wisdomScore)
        val chaMod = abilityModifier(charismaScore)

        val proficiency = proficiencyBonus(level)
        val hitDie = classDefinition?.hitDie ?: 8
        val speed = raceDefinition?.speed ?: 30

        val firstLevelHp = (hitDie + conMod).coerceAtLeast(1)
        val averagePerLevel = (hitDie / 2 + 1 + conMod).coerceAtLeast(1)
        val maxHp = firstLevelHp + ((level - 1) * averagePerLevel)

        val armourClass = 10 + dexMod + character.armourBonus + character.shieldBonus
        val initiativeBonus = dexMod
        val carryCapacity = strengthScore * 15

        val spellcastingAbility = classDefinition?.spellcastingAbility
        val spellcastingModifier = when (spellcastingAbility) {
            AbilityType.STRENGTH -> strMod
            AbilityType.DEXTERITY -> dexMod
            AbilityType.CONSTITUTION -> conMod
            AbilityType.INTELLIGENCE -> intMod
            AbilityType.WISDOM -> wisMod
            AbilityType.CHARISMA -> chaMod
            null -> 0
        }

        val spellAttackBonus =
            if (spellcastingAbility == null) 0 else spellcastingModifier + proficiency

        val spellSaveDc =
            if (spellcastingAbility == null) 0 else 8 + proficiency + spellcastingModifier

        return CharacterDerivedStats(
            strengthScore = strengthScore,
            dexterityScore = dexterityScore,
            constitutionScore = constitutionScore,
            intelligenceScore = intelligenceScore,
            wisdomScore = wisdomScore,
            charismaScore = charismaScore,
            strMod = strMod,
            dexMod = dexMod,
            conMod = conMod,
            intMod = intMod,
            wisMod = wisMod,
            chaMod = chaMod,
            proficiencyBonus = proficiency,
            hitDie = hitDie,
            speed = speed,
            maxHp = maxHp,
            armourClass = armourClass,
            initiativeBonus = initiativeBonus,
            carryCapacity = carryCapacity,
            spellAttackBonus = spellAttackBonus,
            spellSaveDc = spellSaveDc
        )
    }
}