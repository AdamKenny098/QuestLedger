package ie.setu.questledger.models.dice

enum class DiceRollType(val label: String) {
    GENERIC("Generic Roll"),
    ATTACK("Attack Roll"),
    INITIATIVE("Initiative"),
    ABILITY_CHECK("Ability Check"),
    SAVING_THROW("Saving Throw"),
    DAMAGE("Damage Roll")
}