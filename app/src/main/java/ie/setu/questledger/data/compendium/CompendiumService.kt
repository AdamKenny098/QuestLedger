package ie.setu.questledger.data.compendium

interface CompendiumService {
    fun getRaces(): List<RaceDefinition>
    fun getClasses(): List<ClassDefinition>
    fun getWeapons(): List<WeaponDefinition>
    fun getArmour(): List<ArmourDefinition>
    fun getSpells(): List<SpellDefinition>

    fun getRaceById(id: String): RaceDefinition?
    fun getClassById(id: String): ClassDefinition?
    fun getWeaponById(id: String): WeaponDefinition?
    fun getArmourById(id: String): ArmourDefinition?
    fun getSpellById(id: String): SpellDefinition?
}