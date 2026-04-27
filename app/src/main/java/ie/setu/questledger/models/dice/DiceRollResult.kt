package ie.setu.questledger.models.dice

data class DiceRollResult(
    val rollType: DiceRollType,
    val diceType: DiceType,
    val rolls: List<Int>,
    val keptRoll: Int,
    val modifier: Int,
    val total: Int,
    val label: String,
    val detailText: String
)