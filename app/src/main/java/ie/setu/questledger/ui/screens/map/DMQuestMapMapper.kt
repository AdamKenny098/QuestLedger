package ie.setu.questledger.ui.screens.map

import ie.setu.questledger.models.dm.DMQuestModel

object DMQuestMapMapper {

    fun mapQuests(quests: List<DMQuestModel>): List<DMQuestMapMarker> {
        return quests.mapNotNull { quest ->
            val coords = parseCoordinates(quest.mapCoordinates) ?: return@mapNotNull null

            DMQuestMapMarker(
                id = quest.id,
                title = quest.title.ifBlank { "Unnamed Quest" },
                status = quest.status,
                summary = quest.summary,
                latitude = coords.first,
                longitude = coords.second
            )
        }
    }

    private fun parseCoordinates(raw: String): Pair<Double, Double>? {
        val parts = raw.split(",").map { it.trim() }
        if (parts.size != 2) return null

        val latitude = parts[0].toDoubleOrNull() ?: return null
        val longitude = parts[1].toDoubleOrNull() ?: return null

        return latitude to longitude
    }
}