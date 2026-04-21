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
import ie.setu.questledger.models.InventoryItemModel
import ie.setu.questledger.models.InventoryItemType
import ie.setu.questledger.models.QuickSetupConfig
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

        val scores = generateBaseScores(clazz.id)

        val starterWeapon = compendiumService.getWeaponById(starterWeaponIdFor(clazz.id))
        val starterArmour = starterArmourIdFor(clazz.id)?.let { compendiumService.getArmourById(it) }
        val hasShield = hasShieldFor(clazz.id)
        val starterSpells = starterSpellIdsFor(clazz.id).mapNotNull { compendiumService.getSpellById(it) }

        val inventory = buildStarterInventory(
            characterClassId = clazz.id,
            starterWeapon = starterWeapon,
            starterArmour = starterArmour,
            hasShield = hasShield,
            starterSpells = starterSpells,
            strengthScore = scores[AbilityType.STRENGTH] ?: 10
        )

        val character = CharacterModel(
            name = config.name.trim(),
            characterClass = clazz.id,
            race = race.id,
            level = config.level.coerceIn(1, 20),
            notes = "",
            strength = scores[AbilityType.STRENGTH] ?: 10,
            dexterity = scores[AbilityType.DEXTERITY] ?: 10,
            constitution = scores[AbilityType.CONSTITUTION] ?: 10,
            intelligence = scores[AbilityType.INTELLIGENCE] ?: 10,
            wisdom = scores[AbilityType.WISDOM] ?: 10,
            charisma = scores[AbilityType.CHARISMA] ?: 10,
            armourBonus = 0,
            shieldBonus = 0,
            inventory = inventory
        )

        val derived = CharacterStatEngine.build(character)

        val spellSlotsSummary = formatSpellSlots(clazz, config.level)
        val summaryLines = buildList {
            add("Quick Setup Build")
            add("Race: ${race.name}")
            add("Class: ${clazz.name}")
            add("Starter Weapon: ${starterWeapon?.name ?: "None"}")
            add("Starter Armour: ${starterArmour?.name ?: "None"}")
            add("Shield: ${if (hasShield) "Yes" else "No"}")
            add("HP Max: ${derived.maxHp}")
            add("AC: ${derived.armourClass}")
            add("Initiative: ${if (derived.initiativeBonus >= 0) "+${derived.initiativeBonus}" else derived.initiativeBonus}")
            add("Passive Perception: ${derived.passivePerception}")

            if (starterSpells.isNotEmpty()) {
                add("Starter Spells: ${starterSpells.joinToString { it.name }}")
            }

            add("Spell Slots: $spellSlotsSummary")
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

    private fun buildStarterInventory(
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

        val backpackItem = InventoryItemModel(
            id = "backpack_basic",
            name = "Backpack",
            type = InventoryItemType.BACKPACK_ITEM,
            slotCost = 1,
            quantity = 1
        )
        items += backpackItem

        val capacitySlots = 10 + strengthScore

        return CharacterInventory(
            capacitySlots = capacitySlots,
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


    private fun generateBaseScores(classId: String): Map<AbilityType, Int> {
        val priority = when (classId.lowercase()) {
            "fighter" -> listOf(
                AbilityType.STRENGTH,
                AbilityType.CONSTITUTION,
                AbilityType.DEXTERITY,
                AbilityType.WISDOM,
                AbilityType.CHARISMA,
                AbilityType.INTELLIGENCE
            )
            "rogue" -> listOf(
                AbilityType.DEXTERITY,
                AbilityType.CONSTITUTION,
                AbilityType.WISDOM,
                AbilityType.CHARISMA,
                AbilityType.INTELLIGENCE,
                AbilityType.STRENGTH
            )
            "wizard" -> listOf(
                AbilityType.INTELLIGENCE,
                AbilityType.CONSTITUTION,
                AbilityType.DEXTERITY,
                AbilityType.WISDOM,
                AbilityType.CHARISMA,
                AbilityType.STRENGTH
            )
            "cleric" -> listOf(
                AbilityType.WISDOM,
                AbilityType.CONSTITUTION,
                AbilityType.STRENGTH,
                AbilityType.DEXTERITY,
                AbilityType.CHARISMA,
                AbilityType.INTELLIGENCE
            )
            else -> listOf(
                AbilityType.STRENGTH,
                AbilityType.DEXTERITY,
                AbilityType.CONSTITUTION,
                AbilityType.WISDOM,
                AbilityType.CHARISMA,
                AbilityType.INTELLIGENCE
            )
        }

        val scores = listOf(15, 14, 13, 12, 10, 8)

        return priority.mapIndexed { index, ability ->
            ability to scores[index]
        }.toMap()
    }

    private fun starterWeaponIdFor(classId: String): String {
        return when (classId.lowercase()) {
            "fighter" -> "longsword"
            "wizard" -> "quarterstaff"
            "cleric" -> "quarterstaff"
            "rogue" -> "dagger"
            else -> "dagger"
        }
    }

    private fun starterArmourIdFor(classId: String): String? {
        return when (classId.lowercase()) {
            "fighter" -> "chainmail"
            "cleric" -> "chainshirt"
            "rogue" -> "leather"
            "wizard" -> null
            else -> "leather"
        }
    }

    private fun hasShieldFor(classId: String): Boolean {
        return when (classId.lowercase()) {
            "fighter", "cleric" -> true
            else -> false
        }
    }

    private fun starterSpellIdsFor(classId: String): List<String> {
        return when (classId.lowercase()) {
            "wizard" -> listOf("fire_bolt", "magic_missile")
            "cleric" -> listOf("sacred_flame", "cure_wounds")
            else -> emptyList()
        }
    }

    private fun formatSpellSlots(clazz: ClassDefinition, level: Int): String {
        val slots = clazz.spellSlotProgression[level.coerceIn(1, 20)] ?: emptyList()
        if (slots.isEmpty()) return "None"

        return slots.mapIndexedNotNull { index, count ->
            if (count > 0) "L${index + 1} x$count" else null
        }.joinToString(", ")
    }
}