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
    val selectedProficiencyIds: List<String>,
    val starterWeaponId: String?,
    val starterArmourId: String?,
    val hasShield: Boolean,
    val starterSpellIds: List<String>
)