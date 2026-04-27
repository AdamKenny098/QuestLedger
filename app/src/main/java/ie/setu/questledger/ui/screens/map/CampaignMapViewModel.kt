package ie.setu.questledger.ui.screens.map

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.data.firestore.QuestFirestoreService
import ie.setu.questledger.models.quests.QuestModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CampaignMapViewModel @Inject constructor(
    private val questRepository: QuestFirestoreService,
    private val authService: AuthService
) : ViewModel() {

    val quests = mutableStateOf<List<QuestModel>>(emptyList())
    val isLoading = mutableStateOf(false)
    val isErr = mutableStateOf(false)
    val error = mutableStateOf("")

    val defaultCentre = LatLng(53.3498, -6.2603)

    init {
        loadQuests()
    }

    fun loadQuests() {
        viewModelScope.launch {
            try {
                isLoading.value = true
                isErr.value = false

                quests.value = questRepository.getAll(authService.email)
                    .filter { !(it.latitude == 0.0 && it.longitude == 0.0) }

                isLoading.value = false
            } catch (e: Exception) {
                isLoading.value = false
                isErr.value = true
                error.value = e.message ?: "Failed to load quests"
            }
        }
    }
}