package ie.setu.questledger.models

data class CharacterModel(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val characterClass: String = "",
    val race: String = "",
    val level: Int = 1,
    val notes: String = "",
    val imageUri: String = "",
    val strength: Int = 10,
    val dexterity: Int = 10,
    val constitution: Int = 10,
    val intelligence: Int = 10,
    val wisdom: Int = 10,
    val charisma: Int = 10,
    val currentHp: Int = 0,
    val armourBonus: Int = 0,
    val shieldBonus: Int = 0,
    val inventory: CharacterInventory = CharacterInventory()
)