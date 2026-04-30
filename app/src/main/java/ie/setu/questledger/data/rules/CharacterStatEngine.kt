package ie.setu.questledger.data.rules

import ie.setu.questledger.models.characters.CharacterModel
import kotlin.math.floor

object CharacterStatEngine {

    fun build(character: CharacterModel): CharacterDerivedStats {
        val inventory = character.inventory
        val equippedWeapon = InventoryEngine.findEquippedWeapon(inventory)
        val equippedArmour = InventoryEngine.findEquippedArmour(inventory)
        val equippedOffhand = InventoryEngine.findEquippedOffhand(inventory)

        val progression = CharacterProgressionRules.build(
            classId = character.characterClass,
            level = character.level
        )

        val strMod = abilityModifier(character.strength)
        val dexMod = abilityModifier(character.dexterity)
        val conMod = abilityModifier(character.constitution)
        val intMod = abilityModifier(character.intelligence)
        val wisMod = abilityModifier(character.wisdom)
        val chaMod = abilityModifier(character.charisma)

        val proficiencyBonus = progression.proficiencyBonus
        val hitDie = progression.hitDie

        val maxHp = calculateMaxHp(
            level = character.level,
            hitDie = hitDie,
            conMod = conMod
        )

        val hasInventoryItems = inventory.items.isNotEmpty()

        val baseArmourBonus = if (hasInventoryItems) 0 else character.armourBonus
        val baseShieldBonus = if (hasInventoryItems) 0 else character.shieldBonus

        val equippedArmourBonus = equippedArmour?.armourBonus ?: 0
        val equippedShieldBonus = equippedOffhand?.shieldBonus ?: 0

        val raceBonus = raceAcBonus(character.race)
        val classBonus = classAcBonus(character.characterClass)

        val armourBonus = baseArmourBonus + equippedArmourBonus + raceBonus + classBonus
        val shieldBonus = baseShieldBonus + equippedShieldBonus

        val armourClass = calculateArmourClass(
            armourBonus = armourBonus,
            shieldBonus = shieldBonus,
            dexMod = dexMod
        )

        val weaponAttackBonus = equippedWeapon?.attackBonus ?: 0
        val meleeAttackBonus = proficiencyBonus + strMod + weaponAttackBonus
        val rangedAttackBonus = proficiencyBonus + dexMod + weaponAttackBonus

        val spellcastingMod = spellcastingModifierForClass(
            classId = character.characterClass,
            intMod = intMod,
            wisMod = wisMod,
            chaMod = chaMod
        )

        val spellcastingAbilityLabel = spellcastingAbilityLabelForClass(character.characterClass)

        val spellcastingBlocked = equippedArmour?.spellcastingBlocked == true
        val canCast = !spellcastingBlocked

        val spellAttackBonus =
            if (spellcastingMod == null || !canCast) 0 else proficiencyBonus + spellcastingMod

        val spellSaveDc =
            if (spellcastingMod == null || !canCast) 0 else 8 + proficiencyBonus + spellcastingMod

        val initiativeBonus = dexMod
        val passivePerception = 10 + wisMod
        val carryCapacity = (character.strength * 15).coerceAtLeast(0)
        val inventoryCapacity = inventory.capacitySlots
        val speed = (
                speedForRace(character.race) -
                        (equippedArmour?.movementPenalty ?: 0)
                ).coerceAtLeast(0)

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
            hitDie = hitDie,

            weaponName = equippedWeapon?.name,
            damageRoll = equippedWeapon?.damageDice ?: "1",
            spellcastingBlocked = spellcastingBlocked,

            spellSlotsByLevel = progression.spellSlotsByLevel,
            unlockedFeatures = progression.unlockedFeatures,
            spellcastingAbilityLabel = spellcastingAbilityLabel
        )
    }

    private fun abilityModifier(score: Int): Int {
        return floor((score - 10) / 2.0).toInt()
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

    private fun spellcastingAbilityLabelForClass(classId: String): String? {
        return when (classId.lowercase()) {
            "wizard", "artificer" -> "Intelligence"
            "cleric", "druid", "ranger" -> "Wisdom"
            "bard", "paladin", "sorcerer", "warlock" -> "Charisma"
            else -> null
        }
    }

    private fun speedForRace(raceId: String): Int {
        return when (raceId.lowercase()) {
            "dwarf", "halfling", "gnome" -> 25
            else -> 30
        }
    }

    private fun raceAcBonus(raceId: String): Int {
        return when (raceId.lowercase()) {
            else -> 0
        }
    }

    private fun classAcBonus(classId: String): Int {
        return when (classId.lowercase()) {
            else -> 0
        }
    }
}