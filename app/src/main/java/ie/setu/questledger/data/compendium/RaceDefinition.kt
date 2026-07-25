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
    val languages: List<String> = listOf("Common"),
    val languageChoiceCount: Int = 0,
    val skillProficiencyIds: List<String> = emptyList(),
    val skillChoiceCount: Int = 0,
    val skillChoiceOptions: List<String> = emptyList(),
    val weaponProficiencyIds: List<String> = emptyList(),
    val armourProficiencyIds: List<String> = emptyList(),
    val toolProficiencyIds: List<String> = emptyList(),
    val grantedSpellIdsByLevel: Map<Int, List<String>> = emptyMap()
)
