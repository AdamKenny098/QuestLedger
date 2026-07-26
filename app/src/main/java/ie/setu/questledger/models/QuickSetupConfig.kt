package ie.setu.questledger.models

data class QuickSetupConfig(
    val name: String = "",
    val raceId: String = "",
    val classId: String = "",
    val subclassId: String = "",
    val level: Int = 1,
    val backgroundId: String = "folk_hero",
    val raceVariantId: String = "",
    val selectedFlexibleAbilityIds: List<String> = emptyList(),
    val selectedRacialSkillIds: List<String> = emptyList(),
    val selectedRacialLanguageIds: List<String> = emptyList(),
    val selectedRacialSpellId: String = "",
    val selectedSubclassChoiceIds: List<String> = emptyList()
)
