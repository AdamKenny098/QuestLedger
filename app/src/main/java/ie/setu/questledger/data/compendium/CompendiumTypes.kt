package ie.setu.questledger.data.compendium

enum class AbilityType {
    STRENGTH,
    DEXTERITY,
    CONSTITUTION,
    INTELLIGENCE,
    WISDOM,
    CHARISMA,
    NONE
}

enum class DamageType {
    SLASHING,
    PIERCING,
    BLUDGEONING,
    FIRE,
    COLD,
    LIGHTNING,
    FORCE,
    NECROTIC,
    RADIANT,
    POISON,
    PSYCHIC,
    THUNDER
}

enum class Handedness {
    ONE_HANDED,
    TWO_HANDED,
    VERSATILE
}

enum class WeaponCategory {
    SIMPLE,
    MARTIAL
}

enum class WeaponRangeType {
    MELEE,
    RANGED
}

enum class ArmourType {
    LIGHT,
    MEDIUM,
    HEAVY,
    SHIELD
}

enum class SpellSchool {
    ABJURATION,
    CONJURATION,
    DIVINATION,
    ENCHANTMENT,
    EVOCATION,
    ILLUSION,
    NECROMANCY,
    TRANSMUTATION
}

enum class SpellComponent {
    VERBAL,
    SOMATIC,
    MATERIAL
}

enum class SpellAttackType {
    MELEE,
    RANGED
}

enum class SpellLearningMode {
    NONE,
    KNOWN,
    PREPARED,
    SPELLBOOK
}

enum class SpellSource {
    SRD_5_1,
    QUESTLEDGER_COMPATIBILITY
}
