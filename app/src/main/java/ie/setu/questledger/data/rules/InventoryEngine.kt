package ie.setu.questledger.data.rules

import ie.setu.questledger.models.inventory.CharacterInventory
import ie.setu.questledger.models.inventory.EquipmentLoadout
import ie.setu.questledger.models.inventory.InventoryItemModel
import ie.setu.questledger.models.inventory.InventoryItemType

object InventoryEngine {

    fun usedSlots(inventory: CharacterInventory): Int {
        return inventory.items.sumOf { it.slotCost * it.quantity.coerceAtLeast(1) }
    }

    fun remainingSlots(inventory: CharacterInventory): Int {
        return (inventory.capacitySlots - usedSlots(inventory)).coerceAtLeast(0)
    }

    fun canAddItem(
        inventory: CharacterInventory,
        item: InventoryItemModel
    ): Boolean {
        val required = item.slotCost * item.quantity.coerceAtLeast(1)
        return usedSlots(inventory) + required <= inventory.capacitySlots
    }

    fun addItem(
        inventory: CharacterInventory,
        item: InventoryItemModel
    ): CharacterInventory {
        if (!canAddItem(inventory, item)) return inventory
        return inventory.copy(items = inventory.items + item)
    }

    fun removeItem(
        inventory: CharacterInventory,
        itemId: String
    ): CharacterInventory {
        return inventory.copy(
            items = inventory.items.filterNot { it.id == itemId }
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
            else -> equipped
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
}