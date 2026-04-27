package ie.setu.questledger.models.inventory

data class EquipmentLoadout(
    val weaponId: String? = null,
    val armourId: String? = null,
    val offhandId: String? = null,
    val spellFocusId: String? = null
)