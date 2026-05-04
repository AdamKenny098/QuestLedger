package ie.setu.questledger.ui.screens.dm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.data.firestore.DMWorkspaceService
import ie.setu.questledger.models.dm.DMCampaignModel
import ie.setu.questledger.models.dm.DMNpcModel
import ie.setu.questledger.models.dm.DMPlaceModel
import ie.setu.questledger.models.dm.DMQuestModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DMWorkspaceViewModel @Inject constructor(
    private val authService: AuthService,
    private val dmService: DMWorkspaceService
) : ViewModel() {

    private val email: String
        get() = authService.email

    val campaigns = dmService.getCampaigns(email)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quests = dmService.getQuests(email)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val npcs = dmService.getNpcs(email)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val places = dmService.getPlaces(email)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveCampaign(campaign: DMCampaignModel) = viewModelScope.launch {
        dmService.upsertCampaign(email, campaign)
    }

    fun saveQuest(quest: DMQuestModel) = viewModelScope.launch {
        dmService.upsertQuest(email, quest)
    }

    fun saveNpc(npc: DMNpcModel) = viewModelScope.launch {
        dmService.upsertNpc(email, npc)
    }

    fun savePlace(place: DMPlaceModel) = viewModelScope.launch {
        dmService.upsertPlace(email, place)
    }

    fun deleteCampaign(id: String) = viewModelScope.launch {
        dmService.deleteCampaign(email, id)
    }

    fun deleteQuest(id: String) = viewModelScope.launch {
        dmService.deleteQuest(email, id)
    }

    fun deleteNpc(id: String) = viewModelScope.launch {
        dmService.deleteNpc(email, id)
    }

    fun deletePlace(id: String) = viewModelScope.launch {
        dmService.deletePlace(email, id)
    }
}