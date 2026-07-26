package ie.setu.questledger.data.compendium

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompendiumRepository @Inject constructor() : CompendiumService {

    override fun getRaces(): List<RaceDefinition> = SeedCompendiumData.races

    override fun getRaceVariants(): List<RaceVariantDefinition> =
        SeedCompendiumData.raceVariants

    override fun getRaceVariantsForRace(raceId: String): List<RaceVariantDefinition> {
        return SeedCompendiumData.raceVariants.filter { it.raceId == raceId }
    }

    override fun getClasses(): List<ClassDefinition> = SeedCompendiumData.classes

    override fun getSubclasses(): List<SubclassDefinition> =
        SeedCompendiumData.subclasses

    override fun getSubclassesForClass(classId: String): List<SubclassDefinition> {
        return SeedCompendiumData.subclasses.filter { it.classId == classId }
    }

    override fun getBackgrounds(): List<BackgroundDefinition> = SeedCompendiumData.backgrounds

    override fun getWeapons(): List<WeaponDefinition> = SeedCompendiumData.weapons

    override fun getArmour(): List<ArmourDefinition> = SeedCompendiumData.armour

    override fun getEquipment(): List<EquipmentDefinition> =
        SeedCompendiumData.equipment

    override fun getEquipmentPacks(): List<EquipmentPackDefinition> =
        SeedCompendiumData.equipmentPacks

    override fun getEquipmentCatalogue(): List<EquipmentCatalogueItem> =
        SeedCompendiumData.equipmentCatalogue

    override fun getSpells(): List<SpellDefinition> = SeedCompendiumData.spells

    override fun getFeats(): List<FeatDefinition> = SeedCompendiumData.feats

    override fun getFeatures(): List<FeatureDefinition> = SeedCompendiumData.features

    override fun getRaceById(id: String): RaceDefinition? {
        return SeedCompendiumData.races.firstOrNull { it.id == id }
    }

    override fun getRaceVariantById(id: String): RaceVariantDefinition? {
        return SeedCompendiumData.raceVariants.firstOrNull { it.id == id }
    }

    override fun getClassById(id: String): ClassDefinition? {
        return SeedCompendiumData.classes.firstOrNull { it.id == id }
    }

    override fun getSubclassById(id: String): SubclassDefinition? {
        return SeedCompendiumData.subclasses.firstOrNull { it.id == id }
    }

    override fun getBackgroundById(id: String): BackgroundDefinition? {
        return SeedCompendiumData.backgrounds.firstOrNull { it.id == id }
    }

    override fun getWeaponById(id: String): WeaponDefinition? {
        return SeedCompendiumData.weapons.firstOrNull { it.id == id }
    }

    override fun getArmourById(id: String): ArmourDefinition? {
        return SeedCompendiumData.armour.firstOrNull { it.id == id }
    }

    override fun getEquipmentById(id: String): EquipmentDefinition? {
        return SeedCompendiumData.equipment.firstOrNull { it.id == id }
    }

    override fun getEquipmentPackById(id: String): EquipmentPackDefinition? {
        return SeedCompendiumData.equipmentPacks.firstOrNull { it.id == id }
    }

    override fun getEquipmentCatalogueItemById(id: String): EquipmentCatalogueItem? {
        return SeedCompendiumData.equipmentCatalogue.firstOrNull { it.id == id }
    }

    override fun getSpellById(id: String): SpellDefinition? {
        return SeedCompendiumData.spells.firstOrNull { it.id == id }
    }

    override fun getFeatById(id: String): FeatDefinition? {
        return SeedCompendiumData.feats.firstOrNull { it.id == id }
    }

    override fun getFeatureById(id: String): FeatureDefinition? {
        return SeedCompendiumData.features.firstOrNull { it.id == id }
    }
}
