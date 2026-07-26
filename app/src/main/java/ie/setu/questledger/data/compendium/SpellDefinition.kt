package ie.setu.questledger.data.compendium

data class SpellDefinition(
    val id: String,
    val name: String,
    val level: Int,
    val classIds: Set<String>,
    val school: SpellSchool,
    val castingTime: String,
    val range: String,
    val damageDice: String?,
    val saveType: AbilityType?,
    val description: String,
    val duration: String = "Instantaneous",
    val components: Set<SpellComponent> = emptySet(),
    val materialComponent: String? = null,
    val concentration: Boolean = false,
    val ritual: Boolean = false,
    val attackType: SpellAttackType? = null,
    val damageType: String? = null,
    val healingDice: String? = null,
    val higherLevels: String? = null,
    val source: SpellSource = SpellSource.SRD_5_1
) {
    init {
        require(level in 0..9) { "Spell level must be between 0 and 9" }
        require(!components.contains(SpellComponent.MATERIAL) || materialComponent != null) {
            "Material spells must describe their material component"
        }
    }

    val isCantrip: Boolean
        get() = level == 0

    val levelLabel: String
        get() = if (isCantrip) "Cantrip" else "Level $level"

    val componentLabel: String
        get() = components
            .sortedBy { it.ordinal }
            .joinToString(", ") {
                when (it) {
                    SpellComponent.VERBAL -> "V"
                    SpellComponent.SOMATIC -> "S"
                    SpellComponent.MATERIAL -> "M"
                }
            }
            .ifBlank { "None" }
}
