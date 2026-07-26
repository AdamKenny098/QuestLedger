package ie.setu.questledger.data.rules

import ie.setu.questledger.data.compendium.AbilityType
import ie.setu.questledger.data.compendium.ArmourType
import ie.setu.questledger.data.compendium.SeedCompendiumData
import ie.setu.questledger.data.compendium.WeaponCategory
import ie.setu.questledger.models.characters.CharacterModel
import ie.setu.questledger.models.inventory.InventoryItemModel
import kotlin.math.floor

object CharacterStatEngine {

    fun build(character: CharacterModel): CharacterDerivedStats {
        val inventory = character.inventory
        val equippedWeapon = InventoryEngine.findEquippedWeapon(inventory)
        val equippedArmour = InventoryEngine.findEquippedArmour(inventory)
        val equippedOffhand = InventoryEngine.findEquippedOffhand(inventory)
        val equippedWeaponDefinition = SeedCompendiumData.weapons.firstOrNull {
            it.id == equippedWeapon?.catalogueId || it.id == equippedWeapon?.id
        }
        val equippedArmourDefinition = SeedCompendiumData.armour.firstOrNull {
            it.id == equippedArmour?.catalogueId || it.id == equippedArmour?.id
        }
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
        val subclass = CharacterSubclassRules.effectiveSubclass(character)
        val advancementEffects = CharacterAdvancementRules.passiveEffects(
            selections = character.advancementSelections,
            feats = SeedCompendiumData.feats
        )

        val progression = CharacterProgressionRules.build(
            classId = character.characterClass,
            level = character.level,
            subclassId = subclass?.id.orEmpty()
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
            racialHitPointsPerLevelBonus =
                (raceVariant?.hitPointsPerLevelBonus ?: 0) +
                    (subclass?.hitPointsPerLevelBonus ?: 0)
        ) + (character.level.coerceAtLeast(1) * advancementEffects.hitPointsPerLevelBonus)

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
            unarmouredBaseAc = subclass?.unarmouredBaseAc,
            shieldBonus = shieldBonus
        )

        val weaponAttackBonus = equippedWeapon?.attackBonus ?: 0
        val isWeaponProficient = when {
            equippedWeaponDefinition == null -> true
            equippedWeaponDefinition.id in character.racialWeaponProficiencyIds -> true
            clazz == null -> false
            equippedWeaponDefinition.weaponCategory == WeaponCategory.SIMPLE &&
                "Simple Weapons" in clazz.weaponProficiencies -> true
            equippedWeaponDefinition.weaponCategory == WeaponCategory.MARTIAL &&
                "Martial Weapons" in clazz.weaponProficiencies -> true
            else -> clazz.weaponProficiencies.any { proficiency ->
                val normalized = proficiency
                    .lowercase()
                    .replace(Regex("[^a-z]"), "")
                    .removeSuffix("s")
                val weaponName = equippedWeaponDefinition.name
                    .lowercase()
                    .replace(Regex("[^a-z]"), "")
                    .removeSuffix("s")
                normalized == weaponName
            }
        }
        val weaponProficiencyBonus = if (isWeaponProficient) {
            progression.proficiencyBonus
        } else {
            0
        }
        val finesseMeleeModifier = if (
            equippedWeaponDefinition?.propertyTags?.contains("Finesse") == true
        ) {
            maxOf(strMod, dexMod)
        } else {
            strMod
        }
        val meleeAttackBonus =
            weaponProficiencyBonus + finesseMeleeModifier + weaponAttackBonus
        val rangedAttackBonus =
            weaponProficiencyBonus + dexMod + weaponAttackBonus

        val castingAbility = clazz
            ?.takeIf { it.canCastAt(character.level) }
            ?.spellcastingAbility
        val spellcastingMod = castingAbility?.let(modifiers::get)
        val isArmourProficient = when {
            equippedArmourDefinition == null -> true
            equippedArmourDefinition.armourType in clazz?.armourProficiencies.orEmpty() -> true
            equippedArmourDefinition.armourType in
                subclass?.armourProficiencies.orEmpty() -> true
            equippedArmourDefinition.armourType == ArmourType.LIGHT &&
                character.racialArmourProficiencyIds.any {
                    it.equals("Light armour", ignoreCase = true)
                } -> true
            equippedArmourDefinition.armourType == ArmourType.MEDIUM &&
                character.racialArmourProficiencyIds.any {
                    it.equals("Medium armour", ignoreCase = true)
                } -> true
            else -> false
        }
        val spellcastingBlocked =
            equippedArmour?.spellcastingBlocked == true || !isArmourProficient
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
        val armourMovementPenalty = when {
            equippedArmour?.minimumStrength != null &&
                character.strength < equippedArmour.minimumStrength -> 10
            else -> equippedArmour?.movementPenalty ?: 0
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
            initiativeBonus = dexMod + advancementEffects.initiativeBonus,
            passivePerception = 10 + wisMod + advancementEffects.passivePerceptionBonus,
            carryCapacity = (character.strength * 15).coerceAtLeast(0),
            inventoryCapacity = inventory.capacitySlots,
            speed = (
                (raceVariant?.speedOverride ?: race?.speed ?: 30) -
                    armourMovementPenalty +
                    advancementEffects.speedBonus
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
        unarmouredBaseAc: Int?,
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
            else -> maxOf(
                10 + dexMod + (unarmouredDefenseMod ?: 0),
                (unarmouredBaseAc ?: 10) + dexMod
            )
        }
        return baseWithDex + shieldBonus
    }

    private fun AbilityType.toDisplayName(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }
}
