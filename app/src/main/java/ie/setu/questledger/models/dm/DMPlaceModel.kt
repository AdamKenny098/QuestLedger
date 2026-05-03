package ie.setu.questledger.models.dm

data class DMPlaceModel(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val region: String = "",
    val description: String = "",
    val linkedQuestIds: List<String> = emptyList(),
    val linkedNpcIds: List<String> = emptyList(),
    val mapCoordinates: String = ""
)