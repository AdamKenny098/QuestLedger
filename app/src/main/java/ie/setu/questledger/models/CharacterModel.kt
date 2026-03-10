package ie.setu.questledger.models

import java.util.UUID

data class CharacterModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val characterClass: String,
    val race: String,
    val level: Int,
    val notes: String = ""
)