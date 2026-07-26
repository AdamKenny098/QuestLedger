package ie.setu.questledger.data.rules

import ie.setu.questledger.data.compendium.EquipmentCatalogueItem
import ie.setu.questledger.data.compendium.EquipmentPackDefinition
import ie.setu.questledger.models.inventory.CharacterInventory
import ie.setu.questledger.models.inventory.InventoryItemModel
import ie.setu.questledger.models.inventory.InventoryItemType
import kotlin.math.ceil

object InventoryEngine {

    fun usedSlots(inventory: CharacterInventory): Int {
        return inventory.items.sumOf { item ->
            val stacks = ceil(
                item.quantity.coerceAtLeast(1).toDouble() /
                    item.stackSize.coerceAtLeast(1).toDouble()
            ).toInt()
            item.slotCost * stacks
        }
    }

    fun totalWeightLb(inventory: CharacterInventory): Double {
        return inventory.items.sumOf { item ->
            item.weightLb * item.quantity.coerceAtLeast(1)
        }
    }

    fun remainingSlots(inventory: CharacterInventory): Int {
        return (inventory.capacitySlots - usedSlots(inventory)).coerceAtLeast(0)
    }

    fun canAddItem(
        inventory: CharacterInventory,
        item: InventoryItemModel
    ): Boolean {
        val candidate = mergeItem(inventory, item)
        return usedSlots(candidate) <= inventory.capacitySlots
    }

    fun addItem(
        inventory: CharacterInventory,
        item: InventoryItemModel
    ): CharacterInventory {
        if (!canAddItem(inventory, item)) return inventory
        return mergeItem(inventory, item)
    }

    fun addCatalogueItem(
        inventory: CharacterInventory,
        entry: EquipmentCatalogueItem,
        quantity: Int = entry.defaultQuantity
    ): CharacterInventory {
        if (quantity <= 0) return inventory
        return addItem(
            inventory = inventory,
            item = entry.toInventoryItem(
                itemId = nextItemId(inventory, entry.id),
                quantity = quantity
            )
        )
    }

    fun addEquipmentPack(
        inventory: CharacterInventory,
        pack: EquipmentPackDefinition,
        catalogue: List<EquipmentCatalogueItem>
    ): CharacterInventory {
        val itemsById = catalogue.associateBy(EquipmentCatalogueItem::id)
        val proposed = pack.contents.fold(inventory) { current, content ->
            val entry = itemsById[content.itemId] ?: return@fold current
            mergeItem(
                current,
                entry.toInventoryItem(
                    itemId = nextItemId(current, entry.id),
                    quantity = content.quantity
                )
            )
        }
        return proposed.copy(
            capacitySlots = maxOf(
                inventory.capacitySlots,
                usedSlots(proposed) + 5
            )
        )
    }

    fun changeQuantity(
        inventory: CharacterInventory,
        itemId: String,
        delta: Int
    ): CharacterInventory {
        if (delta == 0) return inventory
        val item = inventory.items.firstOrNull { it.id == itemId } ?: return inventory
        val nextQuantity = item.quantity + delta
        if (nextQuantity <= 0) return removeItem(inventory, itemId)
        val updated = inventory.copy(
            items = inventory.items.map {
                if (it.id == itemId) it.copy(quantity = nextQuantity) else it
            }
        )
        return if (usedSlots(updated) <= inventory.capacitySlots) updated else inventory
    }

    fun removeItem(
        inventory: CharacterInventory,
        itemId: String
    ): CharacterInventory {
        return inventory.copy(
            items = inventory.items.filterNot { it.id == itemId },
            equipped = inventory.equipped.copy(
                weaponId = inventory.equipped.weaponId.takeUnless { it == itemId },
                armourId = inventory.equipped.armourId.takeUnless { it == itemId },
                offhandId = inventory.equipped.offhandId.takeUnless { it == itemId },
                spellFocusId = inventory.equipped.spellFocusId.takeUnless { it == itemId }
            )
        )
    }

    fun equipItem(
        inventory: CharacterInventory,
        itemId: String
    ): CharacterInventory {
        val item = inventory.items.firstOrNull { it.id == itemId } ?: return inventory
        val equipped = inventory.equipped

        val nextLoadout = when (item.type) {
            InventoryItemType.WEAPON -> equipped.copy(weaponId = item.id)
            InventoryItemType.ARMOUR -> equipped.copy(armourId = item.id)
            InventoryItemType.SHIELD -> equipped.copy(offhandId = item.id)
            InventoryItemType.SPELL_FOCUS -> equipped.copy(spellFocusId = item.id)
            InventoryItemType.AMMUNITION,
            InventoryItemType.CONSUMABLE,
            InventoryItemType.TOOL,
            InventoryItemType.BACKPACK_ITEM -> equipped
        }

        return inventory.copy(equipped = nextLoadout)
    }

    fun unequipWeapon(inventory: CharacterInventory): CharacterInventory {
        return inventory.copy(
            equipped = inventory.equipped.copy(weaponId = null)
        )
    }

    fun unequipArmour(inventory: CharacterInventory): CharacterInventory {
        return inventory.copy(
            equipped = inventory.equipped.copy(armourId = null)
        )
    }

    fun unequipOffhand(inventory: CharacterInventory): CharacterInventory {
        return inventory.copy(
            equipped = inventory.equipped.copy(offhandId = null)
        )
    }

    fun unequipSpellFocus(inventory: CharacterInventory): CharacterInventory {
        return inventory.copy(
            equipped = inventory.equipped.copy(spellFocusId = null)
        )
    }

    fun findEquippedWeapon(inventory: CharacterInventory): InventoryItemModel? {
        return inventory.items.firstOrNull { it.id == inventory.equipped.weaponId }
    }

    fun findEquippedArmour(inventory: CharacterInventory): InventoryItemModel? {
        return inventory.items.firstOrNull { it.id == inventory.equipped.armourId }
    }

    fun findEquippedOffhand(inventory: CharacterInventory): InventoryItemModel? {
        return inventory.items.firstOrNull { it.id == inventory.equipped.offhandId }
    }

    fun findSpellFocus(inventory: CharacterInventory): InventoryItemModel? {
        return inventory.items.firstOrNull { it.id == inventory.equipped.spellFocusId }
    }

    private fun mergeItem(
        inventory: CharacterInventory,
        item: InventoryItemModel
    ): CharacterInventory {
        val catalogueId = item.catalogueId.ifBlank { item.id }
        val existing = inventory.items.firstOrNull {
            it.catalogueId.ifBlank { it.id } == catalogueId &&
                it.type == item.type &&
                it.stackSize > 1
        }
        return if (existing == null) {
            inventory.copy(items = inventory.items + item)
        } else {
            inventory.copy(
                items = inventory.items.map {
                    if (it.id == existing.id) {
                        it.copy(quantity = it.quantity + item.quantity)
                    } else {
                        it
                    }
                }
            )
        }
    }

    private fun nextItemId(
        inventory: CharacterInventory,
        catalogueId: String
    ): String {
        if (inventory.items.none { it.id == catalogueId }) return catalogueId
        var suffix = 2
        while (inventory.items.any { it.id == "${catalogueId}_$suffix" }) {
            suffix++
        }
        return "${catalogueId}_$suffix"
    }

    fun EquipmentCatalogueItem.toInventoryItem(
        itemId: String = id,
        quantity: Int = defaultQuantity
    ) = InventoryItemModel(
        id = itemId,
        catalogueId = id,
        name = name,
        type = type,
        slotCost = slotCost,
        quantity = quantity,
        damageDice = damageDice,
        armourBonus = armourBonus,
        baseArmourClass = baseArmourClass,
        maxDexBonus = maxDexBonus,
        shieldBonus = shieldBonus,
        movementPenalty = 0,
        categoryLabel = categoryLabel,
        description = description,
        costCp = costCp,
        weightLb = weightLb,
        stackSize = stackSize,
        properties = properties,
        minimumStrength = minimumStrength,
        stealthDisadvantage = stealthDisadvantage
    )
}
