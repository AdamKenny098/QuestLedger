package ie.setu.questledger.models.dm

data class DMQuestModel(
    val id: String = "",
    val email: String = "",
    val title: String = "",
    val summary: String = "",
    val status: String = "Open",
    val linkedCampaignId: String = "",
    val linkedNpcIds: List<String> = emptyList(),
    val linkedPlaceIds: List<String> = emptyList(),
    val mapCoordinates: String = ""
)