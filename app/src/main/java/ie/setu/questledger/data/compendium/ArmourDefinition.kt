package ie.setu.questledger.data.compendium

data class ArmourDefinition(
    val id: String,
    val name: String,
    val baseAc: Int,
    val maxDexBonus: Int?,
    val weight: Double,
    val armourType: ArmourType,
    val costCp: Int = 0,
    val minimumStrength: Int? = null,
    val stealthDisadvantage: Boolean = false,
    val donMinutes: Int = 1,
    val doffMinutes: Int = 1,
    val description: String = ""
)
