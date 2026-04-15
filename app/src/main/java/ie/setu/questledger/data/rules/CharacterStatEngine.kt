package ie.setu.questledger.data.rules

import ie.setu.questledger.models.CharacterModel
import kotlin.math.floor

data class CharacterDerivedStats(
    val strMod: Int,
    val dexMod: Int,
    val conMod: Int,
    val intMod: Int,
    val wisMod: Int,
    val chaMod: Int,
    val proficiencyBonus: Int,
    val hitDie: Int,
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

    fun hitDieForClass(characterClass: String): Int {
        return when (characterClass.trim().lowercase()) {
            "barbarian" -> 12
            "fighter", "paladin", "ranger" -> 10
            "bard", "cleric", "druid", "monk", "rogue", "warlock" -> 8
            "sorcerer", "wizard" -> 6
            else -> 8
        }
    }

    private fun spellcastingModifier(character: CharacterModel): Int {
        return when (character.characterClass.trim().lowercase()) {
            "wizard" -> abilityModifier(character.intelligence)
            "cleric", "druid", "ranger" -> abilityModifier(character.wisdom)
            "bard", "paladin", "sorcerer", "warlock" -> abilityModifier(character.charisma)
            else -> 0
        }
    }

    fun build(character: CharacterModel): CharacterDerivedStats {
        val level = character.level.coerceAtLeast(1)

        val strMod = abilityModifier(character.strength)
        val dexMod = abilityModifier(character.dexterity)
        val conMod = abilityModifier(character.constitution)
        val intMod = abilityModifier(character.intelligence)
        val wisMod = abilityModifier(character.wisdom)
        val chaMod = abilityModifier(character.charisma)

        val proficiency = proficiencyBonus(level)
        val hitDie = hitDieForClass(character.characterClass)

        val firstLevelHp = (hitDie + conMod).coerceAtLeast(1)
        val averagePerLevel = (hitDie / 2 + 1 + conMod).coerceAtLeast(1)
        val maxHp = firstLevelHp + ((level - 1) * averagePerLevel)

        val armourClass = 10 + dexMod + character.armourBonus + character.shieldBonus
        val initiativeBonus = dexMod
        val carryCapacity = character.strength * 15

        val spellMod = spellcastingModifier(character)
        val spellAttackBonus =
            if (spellMod == 0) 0 else spellMod + proficiency
        val spellSaveDc =
            if (spellMod == 0) 0 else 8 + proficiency + spellMod

        return CharacterDerivedStats(
            strMod = strMod,
            dexMod = dexMod,
            conMod = conMod,
            intMod = intMod,
            wisMod = wisMod,
            chaMod = chaMod,
            proficiencyBonus = proficiency,
            hitDie = hitDie,
            maxHp = maxHp,
            armourClass = armourClass,
            initiativeBonus = initiativeBonus,
            carryCapacity = carryCapacity,
            spellAttackBonus = spellAttackBonus,
            spellSaveDc = spellSaveDc
        )
    }
}