package ie.setu.questledger.data.compendium

object CompendiumLookup {
    private fun norm(value: String): String = value.trim().lowercase()

    fun findRace(value: String): RaceDefinition? {
        if (value.isBlank()) return null
        val key = norm(value)
        return SeedCompendiumData.races.firstOrNull{
            norm(it.id) == key || norm(it.name) == key
        }
    }

    fun findClass(value: String): ClassDefinition? {
        if (value.isBlank()) return null
        val key = norm(value)
        return SeedCompendiumData.classes.firstOrNull{
            norm(it.id) == key || norm(it.name) == key
        }
    }

    fun raceDisplayName(value: String): String {
        return findRace(value)?.name ?: value.ifBlank { "Unknown Race" }
    }

    fun classDisplayName(value: String): String {
        return findClass(value)?.name ?: value.ifBlank { "Unknown Class" }
    }

    fun abilityLabel(ability: AbilityType): String{
        return when (ability) {
            AbilityType.STRENGTH -> "Strength"
            AbilityType.DEXTERITY -> "Dexterity"
            AbilityType.CONSTITUTION -> "Constitution"
            AbilityType.INTELLIGENCE -> "Intelligence"
            AbilityType.WISDOM -> "Wisdom"
            AbilityType.CHARISMA -> "Charisma"
            AbilityType.NONE -> "None"
        }
    }

    fun formatAbilityList(abilities: List<AbilityType>): String {
        return abilities.joinToString(", ") { abilityLabel(it) }
    }

    fun formatStatBonuses(statBonuses: Map<AbilityType, Int>): String{
        if (statBonuses.isEmpty()) return "No bonuses"

        return statBonuses.entries.joinToString (", " )
        { entry -> "${abilityLabel(entry.key)} +${entry.value}"}
    }

    fun formatStatBonuses(race: RaceDefinition): String {
        val fixed = formatStatBonuses(race.statBonuses)
        val flexible = race.flexibleStatBonuses.joinToString(", ") { "+$it to another ability" }
        return listOf(fixed, flexible)
            .filter { it.isNotBlank() && it != "No bonuses" }
            .joinToString(", ")
            .ifBlank { "No bonuses" }
    }
}
