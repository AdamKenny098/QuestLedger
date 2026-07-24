package ie.setu.questledger.data.compendium

data class RaceDefinition(
    val id: String,
    val name: String,
    val description: String,
    val statBonuses: Map<AbilityType, Int>,
    val flexibleStatBonuses: List<Int> = emptyList(),
    val speed: Int,
    val size: String = "Medium",
    val passiveTraits: List<String>,
    val languages: List<String> = listOf("Common")
)
