package ie.setu.questledger.models

data class CharacterModel(
    val id: String = "",
    val name: String = "",
    val characterClass: String = "",
    val race: String = "",
    val level: Int = 1,
    val notes: String = "",
    var email: String = "",
    var imageUri: String = ""
)