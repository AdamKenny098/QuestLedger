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

    fun findRaceVariant(value: String): RaceVariantDefinition? {
        if (value.isBlank()) return null
        val key = norm(value)
        return SeedCompendiumData.raceVariants.firstOrNull {
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

    fun findBackground(value: String): BackgroundDefinition? {
        if (value.isBlank()) return null
        val key = norm(value)
        return SeedCompendiumData.backgrounds.firstOrNull {
            norm(it.id) == key || norm(it.name) == key
        }
    }

    fun raceDisplayName(value: String): String {
        return findRace(value)?.name ?: value.ifBlank { "Unknown Race" }
    }

    fun raceVariantDisplayName(value: String): String {
        return findRaceVariant(value)?.name ?: value.ifBlank { "No Ancestry" }
    }

    fun characterRaceDisplayName(race: String, raceVariant: String): String {
        return findRaceVariant(raceVariant)?.name ?: raceDisplayName(race)
    }

    fun classDisplayName(value: String): String {
        return findClass(value)?.name ?: value.ifBlank { "Unknown Class" }
    }

    fun backgroundDisplayName(value: String): String {
        return findBackground(value)?.name ?: value.ifBlank { "No Background" }
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

    fun formatStatBonuses(
        race: RaceDefinition,
        raceVariant: RaceVariantDefinition?
    ): String {
        val fixed = (race.statBonuses.keys + raceVariant?.statBonuses.orEmpty().keys)
            .distinct()
            .associateWith { ability ->
                (race.statBonuses[ability] ?: 0) +
                    (raceVariant?.statBonuses?.get(ability) ?: 0)
            }
            .filterValues { it != 0 }
        val fixedText = formatStatBonuses(fixed)
        val flexible = race.flexibleStatBonuses.joinToString(", ") {
            "+$it to another ability"
        }
        return listOf(fixedText, flexible)
            .filter { it.isNotBlank() && it != "No bonuses" }
            .joinToString(", ")
            .ifBlank { "No bonuses" }
    }
}
