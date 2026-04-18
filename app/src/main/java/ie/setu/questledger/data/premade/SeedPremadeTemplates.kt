package ie.setu.questledger.data.premade

import ie.setu.questledger.models.PremadeCharacterTemplate

object SeedPremadeTemplates {

    val templates = listOf(
        PremadeCharacterTemplate(
            id = "human_fighter",
            title = "Human Fighter",
            subtitle = "Frontline melee starter",
            summary = "Simple and durable. Good for new players who want straightforward combat.",
            defaultName = "Garrick",
            raceId = "human",
            classId = "fighter",
            level = 1
        ),
        PremadeCharacterTemplate(
            id = "elf_wizard",
            title = "Elf Wizard",
            subtitle = "Ranged spellcaster starter",
            summary = "Fragile but powerful. Good for players who want magic and utility.",
            defaultName = "Aeris",
            raceId = "elf",
            classId = "wizard",
            level = 1
        ),
        PremadeCharacterTemplate(
            id = "dwarf_cleric",
            title = "Dwarf Cleric",
            subtitle = "Support and healing starter",
            summary = "Balanced, sturdy, and useful in most parties. Good all-round pick.",
            defaultName = "Brom",
            raceId = "dwarf",
            classId = "cleric",
            level = 1
        ),
        PremadeCharacterTemplate(
            id = "halforc_rogue",
            title = "Half-Orc Rogue",
            subtitle = "Fast skirmisher starter",
            summary = "More aggressive rogue build with strong damage and sneaky potential.",
            defaultName = "Krag",
            raceId = "halforc",
            classId = "rogue",
            level = 1
        )
    )
}