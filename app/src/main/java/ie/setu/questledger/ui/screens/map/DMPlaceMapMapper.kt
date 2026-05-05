package ie.setu.questledger.ui.screens.map

import ie.setu.questledger.models.dm.DMPlaceModel

object DMPlaceMapMapper {

    fun mapPlaces(places: List<DMPlaceModel>): List<DMPlaceMapMarker> {
        return places.mapNotNull { place ->
            val coords = parseCoordinates(place.mapCoordinates) ?: return@mapNotNull null

            DMPlaceMapMarker(
                id = place.id,
                title = place.name.ifBlank { "Unnamed Place" },
                region = place.region,
                description = place.description,
                latitude = coords.first,
                longitude = coords.second
            )
        }
    }

    private fun parseCoordinates(raw: String): Pair<Double, Double>? {
        val parts = raw.split(",")
            .map { it.trim() }

        if (parts.size != 2) return null

        val latitude = parts[0].toDoubleOrNull() ?: return null
        val longitude = parts[1].toDoubleOrNull() ?: return null

        return latitude to longitude
    }
}