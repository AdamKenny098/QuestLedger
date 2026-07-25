package ie.setu.questledger.models

data class PremadeCharacterTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val summary: String,
    val defaultName: String,
    val raceId: String,
    val classId: String,
    val level: Int,
    val backgroundId: String = "folk_hero",
    val raceVariantId: String = ""
)
