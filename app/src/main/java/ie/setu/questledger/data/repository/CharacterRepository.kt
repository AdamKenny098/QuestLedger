package ie.setu.questledger.data.repository

import android.net.Uri
import ie.setu.questledger.data.api.CharacterApiService
import ie.setu.questledger.data.local.CharacterDao
import ie.setu.questledger.data.local.CharacterEntity
import ie.setu.questledger.data.storage.StorageService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CharacterRepository @Inject constructor(
    private val dao: CharacterDao,
    private val apiService: CharacterApiService,
    private val storageService: StorageService
) {

    fun getAll(): Flow<List<CharacterEntity>> = dao.getAll()

    fun getById(id: Long): Flow<CharacterEntity?> = dao.getById(id)

    suspend fun insert(character: CharacterEntity) = dao.insert(character)

    suspend fun update(character: CharacterEntity) = dao.update(character)

    suspend fun delete(character: CharacterEntity) = dao.delete(character)

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun fetchCharactersFromApi(email: String) {
        val remoteCharacters = apiService.getCharacters(email)
        dao.deleteAll()
        dao.insertAll(remoteCharacters)
    }
    suspend fun insertToApi(character: CharacterEntity): CharacterEntity {
        return apiService.addCharacter(character)
    }

    suspend fun deleteFromApi(character: CharacterEntity){
        apiService.deleteCharacter(character.id)
    }

    suspend fun getFromApi(email: String, id: Long): CharacterEntity {
        return apiService.getCharacterById(email, id).first()
    }

    suspend fun updateInApi(character: CharacterEntity): CharacterEntity {
        return apiService.updateCharacter(character.id, character)
    }

    suspend fun saveCharacter(character: CharacterEntity, localImageUri: Uri?) {
        val finalImageUri =
            if (localImageUri != null) {
                storageService.uploadFile(localImageUri, "characters")
            } else {
                Uri.EMPTY
            }

        val characterToSave = character.copy(
            imageUri = finalImageUri.toString()
        )

        dao.insert(characterToSave)
        apiService.addCharacter(characterToSave)
    }
}