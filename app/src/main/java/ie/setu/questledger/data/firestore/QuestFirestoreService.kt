package ie.setu.questledger.data.firestore

import ie.setu.questledger.models.quests.QuestModel

interface QuestFirestoreService {
    suspend fun getAll(email: String): List<QuestModel>
    suspend fun insert(email: String, quest: QuestModel)
    suspend fun update(email: String, quest: QuestModel)
    suspend fun delete(questId: String)
}