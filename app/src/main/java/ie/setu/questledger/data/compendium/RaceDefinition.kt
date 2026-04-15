package ie.setu.questledger.data.compendium

data class RaceDefinition(
    val id: String,
    val name: String,
    val description: String,
    val statBonuses: Map<AbilityType, Int>,
    val speed: Int,
    val passiveTraits: List<String>
)