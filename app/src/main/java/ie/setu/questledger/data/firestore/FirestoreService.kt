package ie.setu.questledger.data.firestore

import ie.setu.questledger.models.CharacterModel
import kotlinx.coroutines.flow.Flow

typealias Character = CharacterModel
typealias Characters = Flow<List<Character>>

interface FirestoreService {
    suspend fun getAll(email: String): Characters
    suspend fun get(email: String, characterId: String): Character?
    suspend fun insert(email: String, character: Character)
    suspend fun update(email: String, character: Character)
    suspend fun delete(email: String, characterId: String)
}