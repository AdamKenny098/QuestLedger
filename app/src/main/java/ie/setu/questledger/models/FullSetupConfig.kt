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
    val starterSpellIds: List<String>,
    val backgroundId: String = "folk_hero",
    val personalityTraits: List<String> = emptyList(),
    val ideal: String = "",
    val bond: String = "",
    val flaw: String = "",
    val raceVariantId: String = "",
    val selectedFlexibleAbilityIds: List<String> = emptyList(),
    val selectedRacialSkillIds: List<String> = emptyList(),
    val selectedRacialLanguageIds: List<String> = emptyList(),
    val selectedRacialSpellId: String = ""
)
