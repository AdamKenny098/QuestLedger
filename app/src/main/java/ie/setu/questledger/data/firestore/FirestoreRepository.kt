package ie.setu.questledger.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.models.CharacterModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirestoreService {

    override suspend fun getAll(email: String): Flow<List<CharacterModel>> = callbackFlow {
        val listener = firestore.collection(Constants.CHARACTER_COLLECTION)
            .whereEqualTo(Constants.USER_EMAIL, email)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val characters = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CharacterModel::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(characters).isSuccess
            }

        awaitClose { listener.remove() }
    }

    override suspend fun get(email: String, characterId: String): CharacterModel? {
        val snapshot = firestore.collection(Constants.CHARACTER_COLLECTION)
            .document(characterId)
            .get()
            .await()

        return snapshot.toObject(CharacterModel::class.java)?.copy(id = snapshot.id)
    }

    override suspend fun insert(email: String, character: CharacterModel) {
        val characterWithEmail = character.copy(email = email)

        firestore.collection(Constants.CHARACTER_COLLECTION)
            .add(characterWithEmail)
            .await()
    }

    override suspend fun update(email: String, character: CharacterModel) {
        firestore.collection(Constants.CHARACTER_COLLECTION)
            .document(character.id)
            .set(character.copy(email = email))
            .await()
    }

    override suspend fun delete(email: String, characterId: String) {
        firestore.collection(Constants.CHARACTER_COLLECTION)
            .document(characterId)
            .delete()
            .await()
    }
}