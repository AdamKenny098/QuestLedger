package ie.setu.questledger.data.compendium

object SeedCompendiumData {

    val races = listOf(
        RaceDefinition(
            id = "human",
            name = "Human",
            description = "Versatile and adaptable.",
            statBonuses = mapOf(
                AbilityType.STRENGTH to 1,
                AbilityType.DEXTERITY to 1,
                AbilityType.CONSTITUTION to 1,
                AbilityType.INTELLIGENCE to 1,
                AbilityType.WISDOM to 1,
                AbilityType.CHARISMA to 1
            ),
            speed = 30,
            passiveTraits = listOf("Adaptable")
        ),
        RaceDefinition(
            id = "elf",
            name = "Elf",
            description = "Graceful, quick, and perceptive.",
            statBonuses = mapOf(
                AbilityType.DEXTERITY to 2
            ),
            speed = 30,
            passiveTraits = listOf("Darkvision", "Keen Senses", "Fey Ancestry")
        ),
        RaceDefinition(
            id = "dwarf",
            name = "Dwarf",
            description = "Sturdy, resilient, and stubborn.",
            statBonuses = mapOf(
                AbilityType.CONSTITUTION to 2
            ),
            speed = 25,
            passiveTraits = listOf("Darkvision", "Dwarven Resilience")
        ),
        RaceDefinition(
            id = "halforc",
            name = "Half-Orc",
            description = "Powerful and relentless.",
            statBonuses = mapOf(
                AbilityType.STRENGTH to 2,
                AbilityType.CONSTITUTION to 1
            ),
            speed = 30,
            passiveTraits = listOf("Darkvision", "Relentless Endurance", "Savage Attacks")
        )
    )

    val classes = listOf(
        ClassDefinition(
            id = "fighter",
            name = "Fighter",
            hitDie = 10,
            primaryStats = listOf(AbilityType.STRENGTH, AbilityType.DEXTERITY),
            savingThrowProficiencies = listOf(AbilityType.STRENGTH, AbilityType.CONSTITUTION),
            armourProficiencies = listOf(ArmourType.LIGHT, ArmourType.MEDIUM, ArmourType.HEAVY, ArmourType.SHIELD),
            weaponProficiencies = listOf("Simple Weapons", "Martial Weapons"),
            spellcastingAbility = null,
            spellSlotProgression = emptyMap(),
            classFeaturesByLevel = mapOf(
                1 to listOf("Fighting Style", "Second Wind"),
                2 to listOf("Action Surge")
            )
        ),
        ClassDefinition(
            id = "wizard",
            name = "Wizard",
            hitDie = 6,
            primaryStats = listOf(AbilityType.INTELLIGENCE),
            savingThrowProficiencies = listOf(AbilityType.INTELLIGENCE, AbilityType.WISDOM),
            armourProficiencies = emptyList(),
            weaponProficiencies = listOf("Daggers", "Darts", "Slings", "Quarterstaffs", "Light Crossbows"),
            spellcastingAbility = AbilityType.INTELLIGENCE,
            spellSlotProgression = mapOf(
                1 to listOf(2),
                2 to listOf(3),
                3 to listOf(4, 2),
                4 to listOf(4, 3),
                5 to listOf(4, 3, 2)
            ),
            classFeaturesByLevel = mapOf(
                1 to listOf("Spellcasting", "Arcane Recovery"),
                2 to listOf("Arcane Tradition")
            )
        ),
        ClassDefinition(
            id = "cleric",
            name = "Cleric",
            hitDie = 8,
            primaryStats = listOf(AbilityType.WISDOM),
            savingThrowProficiencies = listOf(AbilityType.WISDOM, AbilityType.CHARISMA),
            armourProficiencies = listOf(ArmourType.LIGHT, ArmourType.MEDIUM, ArmourType.SHIELD),
            weaponProficiencies = listOf("Simple Weapons"),
            spellcastingAbility = AbilityType.WISDOM,
            spellSlotProgression = mapOf(
                1 to listOf(2),
                2 to listOf(3),
                3 to listOf(4, 2),
                4 to listOf(4, 3),
                5 to listOf(4, 3, 2)
            ),
            classFeaturesByLevel = mapOf(
                1 to listOf("Spellcasting", "Divine Domain"),
                2 to listOf("Channel Divinity")
            )
        ),
        ClassDefinition(
            id = "rogue",
            name = "Rogue",
            hitDie = 8,
            primaryStats = listOf(AbilityType.DEXTERITY),
            savingThrowProficiencies = listOf(AbilityType.DEXTERITY, AbilityType.INTELLIGENCE),
            armourProficiencies = listOf(ArmourType.LIGHT),
            weaponProficiencies = listOf("Simple Weapons", "Hand Crossbows", "Longswords", "Rapiers", "Shortswords"),
            spellcastingAbility = null,
            spellSlotProgression = emptyMap(),
            classFeaturesByLevel = mapOf(
                1 to listOf("Sneak Attack", "Thieves' Cant"),
                2 to listOf("Cunning Action")
            )
        )
    )

    val weapons = listOf(
        WeaponDefinition(
            id = "longsword",
            name = "Longsword",
            damageDice = "1d8",
            damageType = DamageType.SLASHING,
            propertyTags = listOf("Versatile"),
            weight = 3.0,
            requiredStat = AbilityType.STRENGTH,
            handedness = Handedness.VERSATILE
        ),
        WeaponDefinition(
            id = "dagger",
            name = "Dagger",
            damageDice = "1d4",
            damageType = DamageType.PIERCING,
            propertyTags = listOf("Finesse", "Light", "Thrown"),
            weight = 1.0,
            requiredStat = AbilityType.DEXTERITY,
            handedness = Handedness.ONE_HANDED
        ),
        WeaponDefinition(
            id = "quarterstaff",
            name = "Quarterstaff",
            damageDice = "1d6",
            damageType = DamageType.BLUDGEONING,
            propertyTags = listOf("Versatile"),
            weight = 4.0,
            requiredStat = AbilityType.STRENGTH,
            handedness = Handedness.VERSATILE
        ),
        WeaponDefinition(
            id = "shortbow",
            name = "Shortbow",
            damageDice = "1d6",
            damageType = DamageType.PIERCING,
            propertyTags = listOf("Ammunition", "Two-Handed"),
            weight = 2.0,
            requiredStat = AbilityType.DEXTERITY,
            handedness = Handedness.TWO_HANDED
        )
    )

    val armour = listOf(
        ArmourDefinition(
            id = "leather",
            name = "Leather Armour",
            baseAc = 11,
            maxDexBonus = null,
            weight = 10.0,
            armourType = ArmourType.LIGHT
        ),
        ArmourDefinition(
            id = "chainshirt",
            name = "Chain Shirt",
            baseAc = 13,
            maxDexBonus = 2,
            weight = 20.0,
            armourType = ArmourType.MEDIUM
        ),
        ArmourDefinition(
            id = "chainmail",
            name = "Chain Mail",
            baseAc = 16,
            maxDexBonus = 0,
            weight = 55.0,
            armourType = ArmourType.HEAVY
        ),
        ArmourDefinition(
            id = "shield",
            name = "Shield",
            baseAc = 2,
            maxDexBonus = null,
            weight = 6.0,
            armourType = ArmourType.SHIELD
        )
    )

    val spells = listOf(
        SpellDefinition(
            id = "fire_bolt",
            name = "Fire Bolt",
            level = 0,
            school = SpellSchool.EVOCATION,
            castingTime = "1 Action",
            range = "120 feet",
            damageDice = "1d10",
            saveType = null,
            description = "Hurl a mote of fire at a creature or object."
        ),
        SpellDefinition(
            id = "magic_missile",
            name = "Magic Missile",
            level = 1,
            school = SpellSchool.EVOCATION,
            castingTime = "1 Action",
            range = "120 feet",
            damageDice = "3 x (1d4+1)",
            saveType = null,
            description = "Create three glowing darts of magical force."
        ),
        SpellDefinition(
            id = "cure_wounds",
            name = "Cure Wounds",
            level = 1,
            school = SpellSchool.EVOCATION,
            castingTime = "1 Action",
            range = "Touch",
            damageDice = null,
            saveType = null,
            description = "A creature you touch regains hit points."
        ),
        SpellDefinition(
            id = "sacred_flame",
            name = "Sacred Flame",
            level = 0,
            school = SpellSchool.EVOCATION,
            castingTime = "1 Action",
            range = "60 feet",
            damageDice = "1d8",
            saveType = AbilityType.DEXTERITY,
            description = "Flame-like radiance descends on a creature."
        )
    )
}