package ie.setu.questledger.data.rules

import ie.setu.questledger.models.CharacterModel
import kotlin.math.floor

object CharacterStatEngine {

    fun build(character: CharacterModel): CharacterDerivedStats {
        val strMod = abilityModifier(character.strength)
        val dexMod = abilityModifier(character.dexterity)
        val conMod = abilityModifier(character.constitution)
        val intMod = abilityModifier(character.intelligence)
        val wisMod = abilityModifier(character.wisdom)
        val chaMod = abilityModifier(character.charisma)

        val proficiencyBonus = proficiencyBonusForLevel(character.level)
        val hitDie = hitDieForClass(character.characterClass)

        val maxHp = calculateMaxHp(
            level = character.level,
            hitDie = hitDie,
            conMod = conMod
        )

        val armourClass = calculateArmourClass(
            armourBonus = character.armourBonus,
            shieldBonus = character.shieldBonus,
            dexMod = dexMod
        )

        val meleeAttackBonus = proficiencyBonus + strMod
        val rangedAttackBonus = proficiencyBonus + dexMod

        val spellcastingMod = spellcastingModifierForClass(
            classId = character.characterClass,
            intMod = intMod,
            wisMod = wisMod,
            chaMod = chaMod
        )

        val spellAttackBonus = if (spellcastingMod == null) 0 else proficiencyBonus + spellcastingMod
        val spellSaveDc = if (spellcastingMod == null) 0 else 8 + proficiencyBonus + spellcastingMod

        val initiativeBonus = dexMod
        val passivePerception = 10 + wisMod
        val carryCapacity = (character.strength * 15).coerceAtLeast(0)
        val inventoryCapacity = calculateInventoryCapacity(character.strength, strMod)
        val speed = speedForRace(character.race)

        return CharacterDerivedStats(
            strengthScore = character.strength,
            dexterityScore = character.dexterity,
            constitutionScore = character.constitution,
            intelligenceScore = character.intelligence,
            wisdomScore = character.wisdom,
            charismaScore = character.charisma,

            strMod = strMod,
            dexMod = dexMod,
            conMod = conMod,
            intMod = intMod,
            wisMod = wisMod,
            chaMod = chaMod,

            proficiencyBonus = proficiencyBonus,

            maxHp = maxHp,
            armourClass = armourClass,

            meleeAttackBonus = meleeAttackBonus,
            rangedAttackBonus = rangedAttackBonus,
            spellAttackBonus = spellAttackBonus,
            spellSaveDc = spellSaveDc,

            initiativeBonus = initiativeBonus,
            passivePerception = passivePerception,
            carryCapacity = carryCapacity,
            inventoryCapacity = inventoryCapacity,

            speed = speed,
            hitDie = hitDie
        )
    }

    private fun abilityModifier(score: Int): Int {
        return floor((score - 10) / 2.0).toInt()
    }

    private fun proficiencyBonusForLevel(level: Int): Int {
        return when (level.coerceIn(1, 20)) {
            in 1..4 -> 2
            in 5..8 -> 3
            in 9..12 -> 4
            in 13..16 -> 5
            else -> 6
        }
    }

    private fun hitDieForClass(classId: String): Int {
        return when (classId.lowercase()) {
            "fighter", "paladin", "ranger" -> 10
            "cleric", "druid", "rogue", "bard", "warlock", "monk" -> 8
            "wizard", "sorcerer" -> 6
            "barbarian" -> 12
            else -> 8
        }
    }

    private fun calculateMaxHp(
        level: Int,
        hitDie: Int,
        conMod: Int
    ): Int {
        val clampedLevel = level.coerceAtLeast(1)

        val firstLevelHp = hitDie + conMod
        val laterLevelGain = ((hitDie / 2) + 1) + conMod

        return if (clampedLevel == 1) {
            firstLevelHp.coerceAtLeast(1)
        } else {
            (firstLevelHp + ((clampedLevel - 1) * laterLevelGain)).coerceAtLeast(1)
        }
    }

    private fun calculateArmourClass(
        armourBonus: Int,
        shieldBonus: Int,
        dexMod: Int
    ): Int {
        return 10 + armourBonus + shieldBonus + dexMod
    }

    private fun spellcastingModifierForClass(
        classId: String,
        intMod: Int,
        wisMod: Int,
        chaMod: Int
    ): Int? {
        return when (classId.lowercase()) {
            "wizard", "artificer" -> intMod
            "cleric", "druid", "ranger" -> wisMod
            "bard", "paladin", "sorcerer", "warlock" -> chaMod
            else -> null
        }
    }

    private fun calculateInventoryCapacity(
        strength: Int,
        strMod: Int
    ): Int {
        return (10 + strength + strMod.coerceAtLeast(0)).coerceAtLeast(1)
    }

    private fun speedForRace(raceId: String): Int {
        return when (raceId.lowercase()) {
            "dwarf", "halfling", "gnome" -> 25
            else -> 30
        }
    }
}