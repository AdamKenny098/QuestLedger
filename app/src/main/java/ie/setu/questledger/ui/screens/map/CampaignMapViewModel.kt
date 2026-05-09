package ie.setu.questledger.ui.screens.map

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.data.firestore.DMWorkspaceService
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CampaignMapViewModel @Inject constructor(
    private val dmWorkspaceService: DMWorkspaceService,
    private val authService: AuthService
) : ViewModel() {

    val quests = mutableStateOf<List<DMQuestMapMarker>>(emptyList())
    val places = mutableStateOf<List<DMPlaceMapMarker>>(emptyList())

    val isLoading = mutableStateOf(false)
    val isErr = mutableStateOf(false)
    val error = mutableStateOf("")

    val defaultCentre = LatLng(53.3498, -6.2603)

    init {
        loadMapData()
    }

    fun loadMapData() {
        viewModelScope.launch {
            try {
                isLoading.value = true
                isErr.value = false
                error.value = ""

                val email = authService.email

                combine(
                    dmWorkspaceService.getQuests(email),
                    dmWorkspaceService.getPlaces(email)
                ) { dmQuests, dmPlaces ->
                    quests.value = DMQuestMapMapper.mapQuests(dmQuests)
                    places.value = DMPlaceMapMapper.mapPlaces(dmPlaces)
                }.collect {
                    isLoading.value = false
                }
            } catch (e: Exception) {
                isLoading.value = false
                isErr.value = true
                error.value = e.message ?: "Failed to load map data"
            }
        }
    }
}