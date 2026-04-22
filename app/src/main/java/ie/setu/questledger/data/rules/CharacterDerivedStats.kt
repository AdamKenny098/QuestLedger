package ie.setu.questledger.data.rules

data class CharacterDerivedStats(
    val strengthScore: Int,
    val dexterityScore: Int,
    val constitutionScore: Int,
    val intelligenceScore: Int,
    val wisdomScore: Int,
    val charismaScore: Int,

    val strMod: Int,
    val dexMod: Int,
    val conMod: Int,
    val intMod: Int,
    val wisMod: Int,
    val chaMod: Int,

    val proficiencyBonus: Int,

    val maxHp: Int,
    val armourClass: Int,

    val meleeAttackBonus: Int,
    val rangedAttackBonus: Int,
    val spellAttackBonus: Int,
    val spellSaveDc: Int,

    val initiativeBonus: Int,
    val passivePerception: Int,
    val carryCapacity: Int,
    val inventoryCapacity: Int,

    val speed: Int,
    val hitDie: Int,

    val weaponName: String?,
    val damageRoll: String,
    val spellcastingBlocked: Boolean,

    val spellSlotsByLevel: List<Int>,
    val unlockedFeatures: List<String>
)