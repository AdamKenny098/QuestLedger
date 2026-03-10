package ie.setu.questledger.data.repository

import ie.setu.questledger.data.local.CharacterDao
import ie.setu.questledger.data.local.CharacterEntity
import kotlinx.coroutines.flow.Flow

class CharacterRepository(private val dao: CharacterDao) {

    fun getAll(): Flow<List<CharacterEntity>> = dao.getAll()

    fun getById(id: Long) = dao.getById(id)

    suspend fun insert(character: CharacterEntity) = dao.insert(character)

    suspend fun update(character: CharacterEntity) = dao.update(character)

    suspend fun delete(character: CharacterEntity) = dao.delete(character)

    suspend fun deleteAll() = dao.deleteAll()
}
