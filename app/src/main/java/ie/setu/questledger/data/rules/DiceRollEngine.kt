package ie.setu.questledger.data.rules

import ie.setu.questledger.models.characters.CharacterModel
import ie.setu.questledger.models.dice.DiceRollRequest
import ie.setu.questledger.models.dice.DiceRollResult
import ie.setu.questledger.models.dice.DiceRollType
import ie.setu.questledger.models.dice.DiceType
import kotlin.random.Random

object DiceRollEngine {

    fun roll(
        character: CharacterModel,
        request: DiceRollRequest
    ): DiceRollResult {
        val autoModifier = resolveAutoModifier(character, request)
        val finalModifier = request.modifier + autoModifier

        val rolls = when {
            request.advantage && request.disadvantage -> listOf(singleRoll(request.diceType))
            request.advantage -> listOf(singleRoll(request.diceType), singleRoll(request.diceType))
            request.disadvantage -> listOf(singleRoll(request.diceType), singleRoll(request.diceType))
            else -> listOf(singleRoll(request.diceType))
        }

        val keptRoll = when {
            request.advantage && !request.disadvantage -> rolls.max()
            request.disadvantage && !request.advantage -> rolls.min()
            else -> rolls.first()
        }

        val total = keptRoll + finalModifier

        val detailText = buildDetailText(
            request = request,
            rolls = rolls,
            keptRoll = keptRoll,
            finalModifier = finalModifier,
            total = total
        )

        return DiceRollResult(
            rollType = request.rollType,
            diceType = request.diceType,
            rolls = rolls,
            keptRoll = keptRoll,
            modifier = finalModifier,
            total = total,
            label = request.label.ifBlank { request.rollType.label },
            detailText = detailText
        )
    }

    private fun resolveAutoModifier(
        character: CharacterModel,
        request: DiceRollRequest
    ): Int {
        val derived = CharacterStatEngine.build(character)

        return when (request.rollType) {
            DiceRollType.GENERIC -> 0
            DiceRollType.ATTACK -> derived.meleeAttackBonus
            DiceRollType.INITIATIVE -> derived.initiativeBonus
            DiceRollType.ABILITY_CHECK -> 0
            DiceRollType.SAVING_THROW -> 0
            DiceRollType.DAMAGE -> 0
        }
    }

    private fun singleRoll(diceType: DiceType): Int {
        return when (diceType) {
            DiceType.PERCENTILE -> Random.nextInt(1, 101)
            else -> Random.nextInt(1, (diceType.sides ?: 20) + 1)
        }
    }

    private fun buildDetailText(
        request: DiceRollRequest,
        rolls: List<Int>,
        keptRoll: Int,
        finalModifier: Int,
        total: Int
    ): String {
        val mode = when {
            request.advantage && !request.disadvantage -> "Advantage"
            request.disadvantage && !request.advantage -> "Disadvantage"
            else -> "Normal"
        }

        val rollText = if (rolls.size > 1) {
            "${rolls.joinToString()} -> kept $keptRoll"
        } else {
            keptRoll.toString()
        }

        val modText = if (finalModifier >= 0) "+$finalModifier" else finalModifier.toString()

        return "${request.rollType.label} • ${request.diceType.label} • $mode • Roll: $rollText • Modifier: $modText • Total: $total"
    }
}