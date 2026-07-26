package ie.setu.questledger.data.rules

import ie.setu.questledger.data.compendium.SeedCompendiumData

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
        return classDefinitionFor(classId)?.hitDie ?: 8
    }

    fun spellSlotsForClassAndLevel(classId: String, level: Int): List<Int> {
        val clazz = classDefinitionFor(classId) ?: return emptyList()
        return clazz.spellSlotProgression[level.coerceIn(1, 20)].orEmpty()
    }

    fun unlockedFeaturesForClassAndLevel(
        classId: String,
        level: Int,
        subclassId: String = ""
    ): List<String> {
        val clazz = classDefinitionFor(classId) ?: return emptyList()
        val clampedLevel = level.coerceIn(1, 20)
        val classFeatures = clazz.classFeaturesByLevel
            .filterKeys { it <= clampedLevel }
            .entries
            .sortedBy { it.key }
            .flatMap { it.value }
        val subclassFeatures = SeedCompendiumData.subclasses
            .firstOrNull {
                it.id.equals(subclassId, ignoreCase = true) &&
                    it.classId.equals(classId, ignoreCase = true) &&
                    it.selectionLevel <= clampedLevel
            }
            ?.unlockedFeatures(clampedLevel)
            .orEmpty()
            .map { it.name }
        return (classFeatures + subclassFeatures).distinct()
    }

    fun build(
        classId: String,
        level: Int,
        subclassId: String = ""
    ): CharacterProgression {
        return CharacterProgression(
            proficiencyBonus = proficiencyBonusForLevel(level),
            hitDie = hitDieForClass(classId),
            spellSlotsByLevel = spellSlotsForClassAndLevel(classId, level),
            unlockedFeatures = unlockedFeaturesForClassAndLevel(
                classId,
                level,
                subclassId
            )
        )
    }

    private fun classDefinitionFor(classId: String) =
        SeedCompendiumData.classes.firstOrNull { it.id.equals(classId, ignoreCase = true) }
}
