package ie.setu.questledger.models.quests

data class QuestModel(
    val id: String = "",
    val email: String = "",
    val title: String = "",
    val description: String = "",
    val status: String = "Active",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)