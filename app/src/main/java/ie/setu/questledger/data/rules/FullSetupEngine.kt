package ie.setu.questledger.data.rules

import ie.setu.questledger.data.compendium.AbilityType
import ie.setu.questledger.data.compendium.ArmourDefinition
import ie.setu.questledger.data.compendium.ClassDefinition
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.data.compendium.RaceDefinition
import ie.setu.questledger.data.compendium.SpellDefinition
import ie.setu.questledger.data.compendium.WeaponDefinition
import ie.setu.questledger.models.CharacterInventory
import ie.setu.questledger.models.CharacterModel
import ie.setu.questledger.models.EquipmentLoadout
import ie.setu.questledger.models.FullSetupConfig
import ie.setu.questledger.models.InventoryItemModel
import ie.setu.questledger.models.InventoryItemType
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

        val inventory = buildInventory(
            characterClassId = clazz.id,
            starterWeapon = starterWeapon,
            starterArmour = starterArmour,
            hasShield = config.hasShield,
            starterSpells = starterSpells,
            strengthScore = config.strength
        )

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

        val spellSlotsSummary = if (derived.spellSlotsByLevel.isEmpty()) {
            "None"
        } else {
            derived.spellSlotsByLevel.mapIndexed { index, count ->
                "L${index + 1} x$count"
            }.joinToString(", ")
        }

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

            if (derived.spellAttackBonus != 0 || derived.spellSaveDc != 0) {
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

    private fun buildInventory(
        characterClassId: String,
        starterWeapon: WeaponDefinition?,
        starterArmour: ArmourDefinition?,
        hasShield: Boolean,
        starterSpells: List<SpellDefinition>,
        strengthScore: Int
    ): CharacterInventory {
        val items = mutableListOf<InventoryItemModel>()

        val weaponItem = starterWeapon?.toInventoryWeapon()
        if (weaponItem != null) items += weaponItem

        val armourItem = starterArmour?.toInventoryArmour()
        if (armourItem != null) items += armourItem

        val shieldItem = if (hasShield) {
            InventoryItemModel(
                id = "shield_basic",
                name = "Shield",
                type = InventoryItemType.SHIELD,
                slotCost = 2,
                quantity = 1,
                shieldBonus = 2
            )
        } else null
        if (shieldItem != null) items += shieldItem

        starterSpells.forEach { spell ->
            items += InventoryItemModel(
                id = "spell_${spell.id}",
                name = spell.name,
                type = InventoryItemType.BACKPACK_ITEM,
                slotCost = 0,
                quantity = 1
            )
        }

        val focusItem = when (characterClassId.lowercase()) {
            "wizard", "cleric" -> InventoryItemModel(
                id = "${characterClassId}_focus",
                name = if (characterClassId.equals("wizard", true)) "Arcane Focus" else "Holy Symbol",
                type = InventoryItemType.SPELL_FOCUS,
                slotCost = 1,
                quantity = 1
            )
            else -> null
        }
        if (focusItem != null) items += focusItem

        items += InventoryItemModel(
            id = "backpack_basic",
            name = "Backpack",
            type = InventoryItemType.BACKPACK_ITEM,
            slotCost = 1,
            quantity = 1
        )

        return CharacterInventory(
            capacitySlots = 10 + strengthScore,
            items = items,
            equipped = EquipmentLoadout(
                weaponId = weaponItem?.id,
                armourId = armourItem?.id,
                offhandId = shieldItem?.id,
                spellFocusId = focusItem?.id
            )
        )
    }

    private fun WeaponDefinition.toInventoryWeapon(): InventoryItemModel {
        return InventoryItemModel(
            id = id,
            name = name,
            type = InventoryItemType.WEAPON,
            slotCost = 2,
            quantity = 1,
            attackBonus = 0,
            damageDice = damageDice
        )
    }

    private fun ArmourDefinition.toInventoryArmour(): InventoryItemModel {
        return InventoryItemModel(
            id = id,
            name = name,
            type = InventoryItemType.ARMOUR,
            slotCost = 3,
            quantity = 1,
            armourBonus = (baseAc - 10).coerceAtLeast(0)
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

    private fun formatSigned(value: Int): String {
        return if (value >= 0) "+$value" else value.toString()
    }
}