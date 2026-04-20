package ie.setu.questledger.models

data class FullSetupConfig(
    val name: String,
    val raceId: String,
    val classId: String,
    val level: Int,
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int,
    val selectedProficiencies: List<String>,
    val selectedWeapon: String?,
    val selectedArmour: String?,
    val selectedSpells: List<String>
)