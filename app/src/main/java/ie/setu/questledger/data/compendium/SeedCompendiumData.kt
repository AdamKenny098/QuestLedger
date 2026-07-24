package ie.setu.questledger.data.compendium

/**
 * Stable facade used by the repository and existing UI.
 *
 * The individual catalogues are split by domain so adding a class no longer
 * means editing one large mixed data file.
 */
object SeedCompendiumData {
    val races = SeedRaceData.races
    val classes = SeedClassData.classes
    val weapons = SeedEquipmentData.weapons
    val armour = SeedEquipmentData.armour
    val spells = SeedSpellData.spells
}
