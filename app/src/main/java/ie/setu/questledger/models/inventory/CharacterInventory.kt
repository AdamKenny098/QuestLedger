package ie.setu.questledger.models.inventory

import ie.setu.questledger.models.inventory.EquipmentLoadout
import ie.setu.questledger.models.inventory.InventoryItemModel

data class CharacterInventory(
    val capacitySlots: Int = 10,
    val items: List<InventoryItemModel> = emptyList(),
    val equipped: EquipmentLoadout = EquipmentLoadout()
)