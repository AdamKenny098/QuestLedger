package ie.setu.questledger.data.compendium

data class WeaponDefinition(
    val id: String,
    val name: String,
    val damageDice: String,
    val damageType: DamageType,
    val propertyTags: List<String>,
    val weight: Double,
    val requiredStat: AbilityType,
    val handedness: Handedness
)