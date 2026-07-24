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
    val description: String
)
