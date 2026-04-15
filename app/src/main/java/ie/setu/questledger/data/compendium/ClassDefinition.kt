package ie.setu.questledger.data.compendium

data class ClassDefinition(
    val id: String,
    val name: String,
    val hitDie: Int,
    val primaryStats: List<AbilityType>,
    val savingThrowProficiencies: List<AbilityType>,
    val armourProficiencies: List<ArmourType>,
    val weaponProficiencies: List<String>,
    val spellcastingAbility: AbilityType?,
    val spellSlotProgression: Map<Int, List<Int>>,
    val classFeaturesByLevel: Map<Int, List<String>>
)