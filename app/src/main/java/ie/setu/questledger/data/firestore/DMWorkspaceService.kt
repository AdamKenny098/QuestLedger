package ie.setu.questledger.data.firestore

import ie.setu.questledger.models.dm.DMCampaignModel
import ie.setu.questledger.models.dm.DMNpcModel
import ie.setu.questledger.models.dm.DMPlaceModel
import ie.setu.questledger.models.dm.DMQuestModel
import kotlinx.coroutines.flow.Flow

interface DMWorkspaceService {
    suspend fun upsertCampaign(email: String, campaign: DMCampaignModel)
    suspend fun upsertQuest(email: String, quest: DMQuestModel)
    suspend fun upsertNpc(email: String, npc: DMNpcModel)
    suspend fun upsertPlace(email: String, place: DMPlaceModel)

    suspend fun getCampaign(email: String, id: String): DMCampaignModel?
    suspend fun getQuest(email: String, id: String): DMQuestModel?
    suspend fun getNpc(email: String, id: String): DMNpcModel?
    suspend fun getPlace(email: String, id: String): DMPlaceModel?

    suspend fun deleteCampaign(email: String, id: String)
    suspend fun deleteQuest(email: String, id: String)
    suspend fun deleteNpc(email: String, id: String)
    suspend fun deletePlace(email: String, id: String)

    fun getCampaigns(email: String): Flow<List<DMCampaignModel>>
    fun getQuests(email: String): Flow<List<DMQuestModel>>
    fun getNpcs(email: String): Flow<List<DMNpcModel>>
    fun getPlaces(email: String): Flow<List<DMPlaceModel>>
}