package ie.setu.questledger.data.rules

import ie.setu.questledger.data.compendium.AbilityType
import ie.setu.questledger.data.compendium.SeedCompendiumData
import ie.setu.questledger.models.characters.CharacterModel
import ie.setu.questledger.models.inventory.InventoryItemModel
import kotlin.math.floor

object CharacterStatEngine {

    fun build(character: CharacterModel): CharacterDerivedStats {
        val inventory = character.inventory
        val equippedWeapon = InventoryEngine.findEquippedWeapon(inventory)
        val equippedArmour = InventoryEngine.findEquippedArmour(inventory)
        val equippedOffhand = InventoryEngine.findEquippedOffhand(inventory)
        val clazz = SeedCompendiumData.classes.firstOrNull {
            it.id.equals(character.characterClass, ignoreCase = true)
        }
        val race = SeedCompendiumData.races.firstOrNull {
            it.id.equals(character.race, ignoreCase = true)
        }
        val raceVariant = SeedCompendiumData.raceVariants.firstOrNull {
            it.id.equals(character.raceVariant, ignoreCase = true) &&
                it.raceId.equals(character.race, ignoreCase = true)
        }

        val progression = CharacterProgressionRules.build(
            classId = character.characterClass,
            level = character.level
        )

        val modifiers = mapOf(
            AbilityType.STRENGTH to abilityModifier(character.strength),
            AbilityType.DEXTERITY to abilityModifier(character.dexterity),
            AbilityType.CONSTITUTION to abilityModifier(character.constitution),
            AbilityType.INTELLIGENCE to abilityModifier(character.intelligence),
            AbilityType.WISDOM to abilityModifier(character.wisdom),
            AbilityType.CHARISMA to abilityModifier(character.charisma)
        )
        val strMod = modifiers.getValue(AbilityType.STRENGTH)
        val dexMod = modifiers.getValue(AbilityType.DEXTERITY)
        val conMod = modifiers.getValue(AbilityType.CONSTITUTION)
        val intMod = modifiers.getValue(AbilityType.INTELLIGENCE)
        val wisMod = modifiers.getValue(AbilityType.WISDOM)
        val chaMod = modifiers.getValue(AbilityType.CHARISMA)

        val maxHp = calculateMaxHp(
            level = character.level,
            hitDie = progression.hitDie,
            conMod = conMod,
            racialHitPointsPerLevelBonus = raceVariant?.hitPointsPerLevelBonus ?: 0
        )

        val shieldBonus = if (inventory.items.isEmpty()) {
            character.shieldBonus
        } else {
            equippedOffhand?.shieldBonus ?: 0
        }
        val armourClass = calculateArmourClass(
            equippedArmour = equippedArmour,
            legacyArmourBonus = character.armourBonus,
            inventoryHasItems = inventory.items.isNotEmpty(),
            dexMod = dexMod,
            unarmouredDefenseMod = clazz?.unarmouredDefenseAbility?.let(modifiers::get),
            shieldBonus = shieldBonus
        )

        val weaponAttackBonus = equippedWeapon?.attackBonus ?: 0
        val meleeAttackBonus = progression.proficiencyBonus + strMod + weaponAttackBonus
        val rangedAttackBonus = progression.proficiencyBonus + dexMod + weaponAttackBonus

        val castingAbility = clazz
            ?.takeIf { it.canCastAt(character.level) }
            ?.spellcastingAbility
        val spellcastingMod = castingAbility?.let(modifiers::get)
        val spellcastingBlocked = equippedArmour?.spellcastingBlocked == true
        val canCast = spellcastingMod != null && !spellcastingBlocked
        val spellAttackBonus = if (canCast) {
            progression.proficiencyBonus + requireNotNull(spellcastingMod)
        } else {
            0
        }
        val spellSaveDc = if (canCast) {
            8 + progression.proficiencyBonus + requireNotNull(spellcastingMod)
        } else {
            0
        }

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
            proficiencyBonus = progression.proficiencyBonus,
            maxHp = maxHp,
            armourClass = armourClass,
            meleeAttackBonus = meleeAttackBonus,
            rangedAttackBonus = rangedAttackBonus,
            spellAttackBonus = spellAttackBonus,
            spellSaveDc = spellSaveDc,
            initiativeBonus = dexMod,
            passivePerception = 10 + wisMod,
            carryCapacity = (character.strength * 15).coerceAtLeast(0),
            inventoryCapacity = inventory.capacitySlots,
            speed = (
                (raceVariant?.speedOverride ?: race?.speed ?: 30) -
                    (equippedArmour?.movementPenalty ?: 0)
                ).coerceAtLeast(0),
            hitDie = progression.hitDie,
            weaponName = equippedWeapon?.name,
            damageRoll = equippedWeapon?.damageDice ?: "1",
            spellcastingBlocked = spellcastingBlocked,
            spellSlotsByLevel = progression.spellSlotsByLevel,
            unlockedFeatures = progression.unlockedFeatures,
            spellcastingAbilityLabel = castingAbility?.toDisplayName()
        )
    }

    private fun abilityModifier(score: Int): Int {
        return floor((score - 10) / 2.0).toInt()
    }

    private fun calculateMaxHp(
        level: Int,
        hitDie: Int,
        conMod: Int,
        racialHitPointsPerLevelBonus: Int
    ): Int {
        val clampedLevel = level.coerceAtLeast(1)
        val firstLevelHp = hitDie + conMod
        val laterLevelGain = ((hitDie / 2) + 1) + conMod
        val classHitPoints = if (clampedLevel == 1) {
            firstLevelHp.coerceAtLeast(1)
        } else {
            (firstLevelHp + ((clampedLevel - 1) * laterLevelGain)).coerceAtLeast(1)
        }
        return classHitPoints + (clampedLevel * racialHitPointsPerLevelBonus)
    }

    private fun calculateArmourClass(
        equippedArmour: InventoryItemModel?,
        legacyArmourBonus: Int,
        inventoryHasItems: Boolean,
        dexMod: Int,
        unarmouredDefenseMod: Int?,
        shieldBonus: Int
    ): Int {
        val baseWithDex = when {
            equippedArmour?.baseArmourClass != null -> {
                val dexContribution = when (equippedArmour.maxDexBonus) {
                    null -> dexMod
                    0 -> 0
                    else -> dexMod.coerceAtMost(equippedArmour.maxDexBonus)
                }
                equippedArmour.baseArmourClass + dexContribution
            }

            equippedArmour != null -> 10 + equippedArmour.armourBonus + dexMod
            !inventoryHasItems && legacyArmourBonus != 0 -> 10 + legacyArmourBonus + dexMod
            else -> 10 + dexMod + (unarmouredDefenseMod ?: 0)
        }
        return baseWithDex + shieldBonus
    }

    private fun AbilityType.toDisplayName(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }
}
