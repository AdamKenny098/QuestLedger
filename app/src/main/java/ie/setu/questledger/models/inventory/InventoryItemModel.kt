package ie.setu.questledger.models.inventory

import ie.setu.questledger.models.inventory.InventoryItemType

data class InventoryItemModel(
    val id: String = "",
    val name: String = "",
    val type: InventoryItemType = InventoryItemType.BACKPACK_ITEM,
    val slotCost: Int = 1,
    val quantity: Int = 1,
    val attackBonus: Int = 0,
    val damageDice: String = "",
    val armourBonus: Int = 0,
    val shieldBonus: Int = 0,
    val movementPenalty: Int = 0,
    val spellcastingBlocked: Boolean = false
)