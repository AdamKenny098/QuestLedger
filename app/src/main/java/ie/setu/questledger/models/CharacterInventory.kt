package ie.setu.questledger.models

data class CharacterInventory(
    val capacitySlots: Int = 10,
    val items: List<InventoryItemModel> = emptyList(),
    val equipped: EquipmentLoadout = EquipmentLoadout()
)