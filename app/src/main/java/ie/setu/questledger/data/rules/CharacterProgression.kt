package ie.setu.questledger.data.rules

data class CharacterProgression(
    val proficiencyBonus: Int,
    val hitDie: Int,
    val spellSlotsByLevel: List<Int>,
    val unlockedFeatures: List<String>
)

object CharacterProgressionRules {

    fun proficiencyBonusForLevel(level: Int): Int {
        return when (level.coerceIn(1, 20)) {
            in 1..4 -> 2
            in 5..8 -> 3
            in 9..12 -> 4
            in 13..16 -> 5
            else -> 6
        }
    }

    fun hitDieForClass(classId: String): Int {
        return when (classId.lowercase()) {
            "fighter", "paladin", "ranger" -> 10
            "cleric", "druid", "rogue", "bard", "warlock", "monk" -> 8
            "wizard", "sorcerer" -> 6
            "barbarian" -> 12
            else -> 8
        }
    }

    fun spellSlotsForClassAndLevel(
        classId: String,
        level: Int
    ): List<Int> {
        val clamped = level.coerceIn(1, 20)

        return when (classId.lowercase()) {
            "wizard", "cleric", "druid", "sorcerer", "bard" -> when (clamped) {
                1 -> listOf(2)
                2 -> listOf(3)
                3 -> listOf(4, 2)
                4 -> listOf(4, 3)
                5 -> listOf(4, 3, 2)
                6 -> listOf(4, 3, 3)
                7 -> listOf(4, 3, 3, 1)
                8 -> listOf(4, 3, 3, 2)
                9 -> listOf(4, 3, 3, 3, 1)
                10 -> listOf(4, 3, 3, 3, 2)
                else -> listOf(4, 3, 3, 3, 2)
            }

            "paladin", "ranger" -> when (clamped) {
                in 1..1 -> emptyList()
                2 -> listOf(2)
                3 -> listOf(3)
                4 -> listOf(3)
                5 -> listOf(4, 2)
                6 -> listOf(4, 2)
                7 -> listOf(4, 3)
                8 -> listOf(4, 3)
                9 -> listOf(4, 3, 2)
                10 -> listOf(4, 3, 2)
                else -> listOf(4, 3, 2)
            }

            else -> emptyList()
        }
    }

    fun unlockedFeaturesForClassAndLevel(
        classId: String,
        level: Int
    ): List<String> {
        val clamped = level.coerceIn(1, 20)
        val features = mutableListOf<String>()

        when (classId.lowercase()) {
            "fighter" -> {
                if (clamped >= 1) features += "Fighting Style"
                if (clamped >= 1) features += "Second Wind"
                if (clamped >= 2) features += "Action Surge"
                if (clamped >= 5) features += "Extra Attack"
            }

            "wizard" -> {
                if (clamped >= 1) features += "Spellcasting"
                if (clamped >= 1) features += "Arcane Recovery"
                if (clamped >= 2) features += "Arcane Tradition"
            }

            "cleric" -> {
                if (clamped >= 1) features += "Spellcasting"
                if (clamped >= 1) features += "Divine Domain"
                if (clamped >= 2) features += "Channel Divinity"
            }

            "rogue" -> {
                if (clamped >= 1) features += "Sneak Attack"
                if (clamped >= 1) features += "Thieves' Cant"
                if (clamped >= 2) features += "Cunning Action"
                if (clamped >= 5) features += "Uncanny Dodge"
            }

            "ranger" -> {
                if (clamped >= 1) features += "Favored Enemy"
                if (clamped >= 1) features += "Natural Explorer"
                if (clamped >= 2) features += "Spellcasting"
                if (clamped >= 5) features += "Extra Attack"
            }
        }

        return features
    }

    fun build(
        classId: String,
        level: Int
    ): CharacterProgression {
        return CharacterProgression(
            proficiencyBonus = proficiencyBonusForLevel(level),
            hitDie = hitDieForClass(classId),
            spellSlotsByLevel = spellSlotsForClassAndLevel(classId, level),
            unlockedFeatures = unlockedFeaturesForClassAndLevel(classId, level)
        )
    }
}