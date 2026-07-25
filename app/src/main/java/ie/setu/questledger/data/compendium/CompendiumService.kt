package ie.setu.questledger.data.compendium

interface CompendiumService {
    fun getRaces(): List<RaceDefinition>
    fun getRaceVariants(): List<RaceVariantDefinition>
    fun getRaceVariantsForRace(raceId: String): List<RaceVariantDefinition>
    fun getClasses(): List<ClassDefinition>
    fun getBackgrounds(): List<BackgroundDefinition>
    fun getWeapons(): List<WeaponDefinition>
    fun getArmour(): List<ArmourDefinition>
    fun getSpells(): List<SpellDefinition>

    fun getRaceById(id: String): RaceDefinition?
    fun getRaceVariantById(id: String): RaceVariantDefinition?
    fun getClassById(id: String): ClassDefinition?
    fun getBackgroundById(id: String): BackgroundDefinition?
    fun getWeaponById(id: String): WeaponDefinition?
    fun getArmourById(id: String): ArmourDefinition?
    fun getSpellById(id: String): SpellDefinition?
}
