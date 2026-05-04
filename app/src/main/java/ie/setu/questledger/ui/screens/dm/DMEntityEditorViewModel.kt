package ie.setu.questledger.ui.screens.dm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.data.firestore.DMWorkspaceService
import ie.setu.questledger.models.dm.DMCampaignModel
import ie.setu.questledger.models.dm.DMNpcModel
import ie.setu.questledger.models.dm.DMPlaceModel
import ie.setu.questledger.models.dm.DMQuestModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DMEntityEditorViewModel @Inject constructor(
    private val authService: AuthService,
    private val dmService: DMWorkspaceService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val email: String
        get() = authService.email

    val isLoading = mutableStateOf(false)
    val isErr = mutableStateOf(false)
    val error = mutableStateOf("")

    val campaign = mutableStateOf(DMCampaignModel())
    val quest = mutableStateOf(DMQuestModel())
    val npc = mutableStateOf(DMNpcModel())
    val place = mutableStateOf(DMPlaceModel())

    private val campaignId: String? = savedStateHandle["campaignId"]
    private val questId: String? = savedStateHandle["questId"]
    private val npcId: String? = savedStateHandle["npcId"]
    private val placeId: String? = savedStateHandle["placeId"]

    init {
        when {
            !campaignId.isNullOrBlank() -> loadCampaign(campaignId)
            !questId.isNullOrBlank() -> loadQuest(questId)
            !npcId.isNullOrBlank() -> loadNpc(npcId)
            !placeId.isNullOrBlank() -> loadPlace(placeId)
        }
    }

    private fun loadCampaign(id: String) = viewModelScope.launch {
        try {
            isLoading.value = true
            campaign.value = dmService.getCampaign(email, id) ?: DMCampaignModel()
            isLoading.value = false
        } catch (e: Exception) {
            isLoading.value = false
            isErr.value = true
            error.value = e.message ?: "Failed to load campaign"
        }
    }

    private fun loadQuest(id: String) = viewModelScope.launch {
        try {
            isLoading.value = true
            quest.value = dmService.getQuest(email, id) ?: DMQuestModel()
            isLoading.value = false
        } catch (e: Exception) {
            isLoading.value = false
            isErr.value = true
            error.value = e.message ?: "Failed to load quest"
        }
    }

    private fun loadNpc(id: String) = viewModelScope.launch {
        try {
            isLoading.value = true
            npc.value = dmService.getNpc(email, id) ?: DMNpcModel()
            isLoading.value = false
        } catch (e: Exception) {
            isLoading.value = false
            isErr.value = true
            error.value = e.message ?: "Failed to load npc"
        }
    }

    private fun loadPlace(id: String) = viewModelScope.launch {
        try {
            isLoading.value = true
            place.value = dmService.getPlace(email, id) ?: DMPlaceModel()
            isLoading.value = false
        } catch (e: Exception) {
            isLoading.value = false
            isErr.value = true
            error.value = e.message ?: "Failed to load place"
        }
    }

    fun saveCampaign(model: DMCampaignModel, onDone: () -> Unit) = viewModelScope.launch {
        try {
            isLoading.value = true
            isErr.value = false
            error.value = ""
            dmService.upsertCampaign(email, model)
            isLoading.value = false
            onDone()
        } catch (e: Exception) {
            isLoading.value = false
            isErr.value = true
            error.value = e.message ?: "Failed to save campaign"
        }
    }

    fun saveQuest(model: DMQuestModel, onDone: () -> Unit) = viewModelScope.launch {
        try {
            isLoading.value = true
            isErr.value = false
            error.value = ""
            dmService.upsertQuest(email, model)
            isLoading.value = false
            onDone()
        } catch (e: Exception) {
            isLoading.value = false
            isErr.value = true
            error.value = e.message ?: "Failed to save quest"
        }
    }

    fun saveNpc(model: DMNpcModel, onDone: () -> Unit) = viewModelScope.launch {
        try {
            isLoading.value = true
            isErr.value = false
            error.value = ""
            dmService.upsertNpc(email, model)
            isLoading.value = false
            onDone()
        } catch (e: Exception) {
            isLoading.value = false
            isErr.value = true
            error.value = e.message ?: "Failed to save npc"
        }
    }

    fun savePlace(model: DMPlaceModel, onDone: () -> Unit) = viewModelScope.launch {
        try {
            isLoading.value = true
            isErr.value = false
            error.value = ""
            dmService.upsertPlace(email, model)
            isLoading.value = false
            onDone()
        } catch (e: Exception) {
            isLoading.value = false
            isErr.value = true
            error.value = e.message ?: "Failed to save place"
        }
    }
}