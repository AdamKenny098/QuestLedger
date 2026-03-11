package ie.setu.questledger.data.repository

import ie.setu.questledger.data.api.CharacterApiService
import ie.setu.questledger.data.local.CharacterDao
import ie.setu.questledger.data.local.CharacterEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CharacterRepository @Inject constructor(
    private val dao: CharacterDao,
    private val apiService: CharacterApiService
) {

    fun getAll(): Flow<List<CharacterEntity>> = dao.getAll()

    fun getById(id: Long): Flow<CharacterEntity?> = dao.getById(id)

    suspend fun insert(character: CharacterEntity) = dao.insert(character)

    suspend fun update(character: CharacterEntity) = dao.update(character)

    suspend fun delete(character: CharacterEntity) = dao.delete(character)

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun fetchCharactersFromApi() {
        val remoteCharacters = apiService.getCharacters()
        dao.insertAll(remoteCharacters)
    }

    suspend fun insertToApi(character: CharacterEntity): CharacterEntity {
        return apiService.addCharacter(character)
    }

    suspend fun deleteFromApi(character: CharacterEntity){
        apiService.deleteCharacter(character.id)
    }

    suspend fun getFromApi(id: Long): CharacterEntity {
        return apiService.getCharacterById(id)
    }

    suspend fun updateInApi(character: CharacterEntity): CharacterEntity {
        return apiService.updateCharacter(character.id, character)
    }
}