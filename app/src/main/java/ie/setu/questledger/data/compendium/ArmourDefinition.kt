package ie.setu.questledger.data.compendium

data class ArmourDefinition(
    val id: String,
    val name: String,
    val baseAc: Int,
    val maxDexBonus: Int?,
    val weight: Double,
    val armourType: ArmourType
)