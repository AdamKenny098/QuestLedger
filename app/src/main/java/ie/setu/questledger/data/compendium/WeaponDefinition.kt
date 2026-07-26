package ie.setu.questledger.data.compendium

data class WeaponDefinition(
    val id: String,
    val name: String,
    val damageDice: String,
    val damageType: DamageType,
    val propertyTags: List<String>,
    val weight: Double,
    val requiredStat: AbilityType,
    val handedness: Handedness,
    val costCp: Int = 0,
    val weaponCategory: WeaponCategory = WeaponCategory.SIMPLE,
    val rangeType: WeaponRangeType = WeaponRangeType.MELEE,
    val normalRangeFt: Int? = null,
    val longRangeFt: Int? = null,
    val versatileDamageDice: String? = null,
    val description: String = ""
)
