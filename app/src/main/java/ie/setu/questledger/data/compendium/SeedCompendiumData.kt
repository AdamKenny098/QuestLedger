package ie.setu.questledger.data.compendium

/**
 * Stable facade used by the repository and existing UI.
 *
 * The individual catalogues are split by domain so adding a class no longer
 * means editing one large mixed data file.
 */
object SeedCompendiumData {
    val races = SeedRaceData.races
    val raceVariants = SeedRaceVariantData.variants
    val classes = SeedClassData.classes
    val subclasses = SeedSubclassData.subclasses
    val backgrounds = SeedBackgroundData.backgrounds
    val weapons = SeedEquipmentData.weapons
    val armour = SeedEquipmentData.armour
    val equipment = SeedEquipmentData.equipment
    val equipmentPacks = SeedEquipmentData.equipmentPacks
    val equipmentCatalogue = SeedEquipmentData.catalogueItems
    val spells = SeedSpellData.spells
    val feats = SeedFeatData.feats
    val features = SeedFeatureData.features
}
