package ie.setu.questledger.models.dm

data class DMCampaignModel(
    val id: String = "",
    val email: String = "",
    val title: String = "",
    val setting: String = "",
    val summary: String = "",
    val activeQuestIds: List<String> = emptyList(),
    val sessionCount: Int = 0
)