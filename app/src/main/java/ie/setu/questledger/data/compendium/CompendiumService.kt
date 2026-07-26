package ie.setu.questledger.data.compendium

interface CompendiumService {
    fun getRaces(): List<RaceDefinition>
    fun getRaceVariants(): List<RaceVariantDefinition>
    fun getRaceVariantsForRace(raceId: String): List<RaceVariantDefinition>
    fun getClasses(): List<ClassDefinition>
    fun getSubclasses(): List<SubclassDefinition>
    fun getSubclassesForClass(classId: String): List<SubclassDefinition>
    fun getBackgrounds(): List<BackgroundDefinition>
    fun getWeapons(): List<WeaponDefinition>
    fun getArmour(): List<ArmourDefinition>
    fun getEquipment(): List<EquipmentDefinition>
    fun getEquipmentPacks(): List<EquipmentPackDefinition>
    fun getEquipmentCatalogue(): List<EquipmentCatalogueItem>
    fun getSpells(): List<SpellDefinition>
    fun getFeats(): List<FeatDefinition>
    fun getFeatures(): List<FeatureDefinition>

    fun getRaceById(id: String): RaceDefinition?
    fun getRaceVariantById(id: String): RaceVariantDefinition?
    fun getClassById(id: String): ClassDefinition?
    fun getSubclassById(id: String): SubclassDefinition?
    fun getBackgroundById(id: String): BackgroundDefinition?
    fun getWeaponById(id: String): WeaponDefinition?
    fun getArmourById(id: String): ArmourDefinition?
    fun getEquipmentById(id: String): EquipmentDefinition?
    fun getEquipmentPackById(id: String): EquipmentPackDefinition?
    fun getEquipmentCatalogueItemById(id: String): EquipmentCatalogueItem?
    fun getSpellById(id: String): SpellDefinition?
    fun getFeatById(id: String): FeatDefinition?
    fun getFeatureById(id: String): FeatureDefinition?
}
