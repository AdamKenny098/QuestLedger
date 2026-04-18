package ie.setu.questledger.data.premade

import ie.setu.questledger.models.PremadeCharacterTemplate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremadeTemplateRepository @Inject constructor() : PremadeTemplateService {

    override fun getTemplates(): List<PremadeCharacterTemplate> {
        return SeedPremadeTemplates.templates
    }

    override fun getTemplateById(id: String): PremadeCharacterTemplate? {
        return SeedPremadeTemplates.templates.firstOrNull { it.id == id }
    }
}