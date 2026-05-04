package ie.setu.questledger.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import ie.setu.questledger.models.dm.DMCampaignModel
import ie.setu.questledger.models.dm.DMNpcModel
import ie.setu.questledger.models.dm.DMPlaceModel
import ie.setu.questledger.models.dm.DMQuestModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DMWorkspaceRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : DMWorkspaceService {

    override suspend fun upsertCampaign(email: String, campaign: DMCampaignModel) {
        val id = campaign.id.ifBlank { UUID.randomUUID().toString() }

        firestore.collection("users")
            .document(email)
            .collection("dm_campaigns")
            .document(id)
            .set(campaign.copy(id = id, email = email))
            .await()
    }

    override suspend fun upsertQuest(email: String, quest: DMQuestModel) {
        val id = quest.id.ifBlank { UUID.randomUUID().toString() }

        firestore.collection("users")
            .document(email)
            .collection("dm_quests")
            .document(id)
            .set(quest.copy(id = id, email = email))
            .await()
    }

    override suspend fun upsertNpc(email: String, npc: DMNpcModel) {
        val id = npc.id.ifBlank { UUID.randomUUID().toString() }

        firestore.collection("users")
            .document(email)
            .collection("dm_npcs")
            .document(id)
            .set(npc.copy(id = id, email = email))
            .await()
    }

    override suspend fun upsertPlace(email: String, place: DMPlaceModel) {
        val id = place.id.ifBlank { UUID.randomUUID().toString() }

        firestore.collection("users")
            .document(email)
            .collection("dm_places")
            .document(id)
            .set(place.copy(id = id, email = email))
            .await()
    }

    override suspend fun deleteCampaign(email: String, id: String) {
        firestore.collection("users")
            .document(email)
            .collection("dm_campaigns")
            .document(id)
            .delete()
            .await()
    }

    override suspend fun deleteQuest(email: String, id: String) {
        firestore.collection("users")
            .document(email)
            .collection("dm_quests")
            .document(id)
            .delete()
            .await()
    }

    override suspend fun deleteNpc(email: String, id: String) {
        firestore.collection("users")
            .document(email)
            .collection("dm_npcs")
            .document(id)
            .delete()
            .await()
    }

    override suspend fun deletePlace(email: String, id: String) {
        firestore.collection("users")
            .document(email)
            .collection("dm_places")
            .document(id)
            .delete()
            .await()
    }

    override fun getCampaigns(email: String): Flow<List<DMCampaignModel>> =
        collectionFlow(email, "dm_campaigns", DMCampaignModel::class.java)

    override fun getQuests(email: String): Flow<List<DMQuestModel>> =
        collectionFlow(email, "dm_quests", DMQuestModel::class.java)

    override fun getNpcs(email: String): Flow<List<DMNpcModel>> =
        collectionFlow(email, "dm_npcs", DMNpcModel::class.java)

    override fun getPlaces(email: String): Flow<List<DMPlaceModel>> =
        collectionFlow(email, "dm_places", DMPlaceModel::class.java)

    private fun <T : Any> collectionFlow(
        email: String,
        collectionName: String,
        clazz: Class<T>
    ): Flow<List<T>> = callbackFlow {
        val registration = firestore.collection("users")
            .document(email)
            .collection(collectionName)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val items = snapshot?.documents
                    ?.mapNotNull { it.toObject(clazz) }
                    .orEmpty()

                trySend(items)
            }

        awaitClose { registration.remove() }
    }
}