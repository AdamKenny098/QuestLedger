package ie.setu.questledger.data.compendium

data class ClassDefinition(
    val id: String,
    val name: String,
    val description: String,
    val hitDie: Int,
    val primaryStats: List<AbilityType>,
    val savingThrowProficiencies: List<AbilityType>,
    val armourProficiencies: List<ArmourType>,
    val weaponProficiencies: List<String>,
    val skillProficiencies: List<String>,
    val skillChoiceCount: Int,
    val quickBuildAbilityPriority: List<AbilityType>,
    val starterWeaponIds: List<String>,
    val starterArmourIds: List<String>,
    val defaultWeaponId: String?,
    val defaultArmourId: String?,
    val startsWithShield: Boolean,
    val spellcastingAbility: AbilityType?,
    val spellcastingStartLevel: Int?,
    val spellFocusName: String?,
    val starterSpellIds: List<String>,
    val spellSlotProgression: Map<Int, List<Int>>,
    val classFeaturesByLevel: Map<Int, List<String>>,
    val unarmouredDefenseAbility: AbilityType? = null
) {
    val isSpellcaster: Boolean
        get() = spellcastingAbility != null && spellcastingStartLevel != null

    fun canCastAt(level: Int): Boolean {
        return isSpellcaster && level >= requireNotNull(spellcastingStartLevel)
    }
}
