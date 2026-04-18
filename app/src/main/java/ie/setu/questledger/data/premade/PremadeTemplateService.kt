package ie.setu.questledger.data.premade

import ie.setu.questledger.models.PremadeCharacterTemplate

interface PremadeTemplateService {
    fun getTemplates(): List<PremadeCharacterTemplate>
    fun getTemplateById(id: String): PremadeCharacterTemplate?
}