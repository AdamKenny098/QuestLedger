package ie.setu.questledger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String = "",
    val name: String,
    val characterClass: String,
    val race: String,
    val level: Int,
    val notes: String
)
