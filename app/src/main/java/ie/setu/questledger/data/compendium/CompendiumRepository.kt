package ie.setu.questledger.data.compendium

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompendiumRepository @Inject constructor() : CompendiumService {

    override fun getRaces(): List<RaceDefinition> = SeedCompendiumData.races

    override fun getClasses(): List<ClassDefinition> = SeedCompendiumData.classes

    override fun getWeapons(): List<WeaponDefinition> = SeedCompendiumData.weapons

    override fun getArmour(): List<ArmourDefinition> = SeedCompendiumData.armour

    override fun getSpells(): List<SpellDefinition> = SeedCompendiumData.spells

    override fun getRaceById(id: String): RaceDefinition? {
        return SeedCompendiumData.races.firstOrNull { it.id == id }
    }

    override fun getClassById(id: String): ClassDefinition? {
        return SeedCompendiumData.classes.firstOrNull { it.id == id }
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