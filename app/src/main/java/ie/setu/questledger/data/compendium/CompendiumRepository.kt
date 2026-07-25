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

    override fun getBackgrounds(): List<BackgroundDefinition> = SeedCompendiumData.backgrounds

    override fun getWeapons(): List<WeaponDefinition> = SeedCompendiumData.weapons

    override fun getArmour(): List<ArmourDefinition> = SeedCompendiumData.armour

    override fun getSpells(): List<SpellDefinition> = SeedCompendiumData.spells

    override fun getRaceById(id: String): RaceDefinition? {
        return SeedCompendiumData.races.firstOrNull { it.id == id }
    }

    override fun getRaceVariantById(id: String): RaceVariantDefinition? {
        return SeedCompendiumData.raceVariants.firstOrNull { it.id == id }
    }

    override fun getClassById(id: String): ClassDefinition? {
        return SeedCompendiumData.classes.firstOrNull { it.id == id }
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

    override fun getSpellById(id: String): SpellDefinition? {
        return SeedCompendiumData.spells.firstOrNull { it.id == id }
    }
}
