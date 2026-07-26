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

    fun findSubclass(value: String): SubclassDefinition? {
        if (value.isBlank()) return null
        val key = norm(value)
        return SeedCompendiumData.subclasses.firstOrNull {
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

    fun findEquipment(value: String): EquipmentCatalogueItem? {
        if (value.isBlank()) return null
        val key = norm(value)
        return SeedCompendiumData.equipmentCatalogue.firstOrNull {
            norm(it.id) == key || norm(it.name) == key
        }
    }

    fun findEquipmentPack(value: String): EquipmentPackDefinition? {
        if (value.isBlank()) return null
        val key = norm(value)
        return SeedCompendiumData.equipmentPacks.firstOrNull {
            norm(it.id) == key || norm(it.name) == key
        }
    }

    fun findFeat(value: String): FeatDefinition? {
        if (value.isBlank()) return null
        val key = norm(value)
        return SeedCompendiumData.feats.firstOrNull {
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

    fun subclassDisplayName(value: String): String {
        return findSubclass(value)?.name ?: value.ifBlank { "No Subclass" }
    }

    fun backgroundDisplayName(value: String): String {
        return findBackground(value)?.name ?: value.ifBlank { "No Background" }
    }

    fun equipmentDisplayName(value: String): String {
        return findEquipment(value)?.name ?: value.ifBlank { "Unknown Equipment" }
    }

    fun featDisplayName(value: String): String {
        return findFeat(value)?.name ?: value.ifBlank { "Unknown Feat" }
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
        val fixed = if (raceVariant?.replacesBaseStatBonuses == true) {
            raceVariant.statBonuses
        } else {
            (race.statBonuses.keys + raceVariant?.statBonuses.orEmpty().keys)
                .distinct()
                .associateWith { ability ->
                    (race.statBonuses[ability] ?: 0) +
                        (raceVariant?.statBonuses?.get(ability) ?: 0)
                }
                .filterValues { it != 0 }
        }
        val fixedText = formatStatBonuses(fixed)
        val flexibleBonuses = raceVariant
            ?.flexibleStatBonuses
            ?.takeIf { it.isNotEmpty() }
            ?: race.flexibleStatBonuses
        val flexible = flexibleBonuses.joinToString(", ") {
            "+$it to another ability"
        }
        return listOf(fixedText, flexible)
            .filter { it.isNotBlank() && it != "No bonuses" }
            .joinToString(", ")
            .ifBlank { "No bonuses" }
    }
}
