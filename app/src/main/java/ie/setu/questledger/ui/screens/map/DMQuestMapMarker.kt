package ie.setu.questledger.ui.screens.map

data class DMQuestMapMarker(
    val id: String,
    val title: String,
    val status: String,
    val summary: String,
    val latitude: Double,
    val longitude: Double
)