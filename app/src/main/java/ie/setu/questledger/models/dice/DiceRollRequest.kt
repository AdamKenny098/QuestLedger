package ie.setu.questledger.models.dice

data class DiceRollRequest(
    val rollType: DiceRollType = DiceRollType.GENERIC,
    val diceType: DiceType = DiceType.D20,
    val modifier: Int = 0,
    val advantage: Boolean = false,
    val disadvantage: Boolean = false,
    val label: String = ""
)