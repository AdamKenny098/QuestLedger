package ie.setu.questledger.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import ie.setu.questledger.models.QuestModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : QuestFirestoreService {

    private val collection = firestore.collection("quests")

    override suspend fun getAll(email: String): List<QuestModel> {
        return collection
            .whereEqualTo("email", email)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(QuestModel::class.java)?.copy(id = doc.id)
            }
    }

    override suspend fun insert(email: String, quest: QuestModel) {
        val docId =
            if (quest.id.isBlank()) collection.document().id
            else quest.id

        val questToSave = quest.copy(
            id = docId,
            email = email
        )

        collection.document(docId).set(questToSave).await()
    }

    override suspend fun update(email: String, quest: QuestModel) {
        val questToSave = quest.copy(email = email)
        collection.document(quest.id).set(questToSave).await()
    }

    override suspend fun delete(questId: String) {
        collection.document(questId).delete().await()
    }
}