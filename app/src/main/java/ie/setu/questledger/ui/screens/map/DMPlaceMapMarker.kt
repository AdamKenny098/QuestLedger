package ie.setu.questledger.ui.screens.map

data class DMPlaceMapMarker(
    val id: String,
    val title: String,
    val region: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)