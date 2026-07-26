package ie.setu.questledger.models.inventory

data class InventoryItemModel(
    val id: String = "",
    val name: String = "",
    val type: InventoryItemType = InventoryItemType.BACKPACK_ITEM,
    val slotCost: Int = 1,
    val quantity: Int = 1,
    val attackBonus: Int = 0,
    val damageDice: String = "",
    val armourBonus: Int = 0,
    val baseArmourClass: Int? = null,
    val maxDexBonus: Int? = null,
    val shieldBonus: Int = 0,
    val movementPenalty: Int = 0,
    val spellcastingBlocked: Boolean = false,
    val catalogueId: String = "",
    val categoryLabel: String = "",
    val description: String = "",
    val costCp: Int = 0,
    val weightLb: Double = 0.0,
    val stackSize: Int = 1,
    val properties: List<String> = emptyList(),
    val minimumStrength: Int? = null,
    val stealthDisadvantage: Boolean = false
)
