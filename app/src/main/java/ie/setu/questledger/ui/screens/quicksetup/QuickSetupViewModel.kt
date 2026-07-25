package ie.setu.questledger.ui.screens.quicksetup

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.data.compendium.ClassDefinition
import ie.setu.questledger.data.compendium.BackgroundDefinition
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.data.compendium.RaceDefinition
import ie.setu.questledger.data.firestore.FirestoreService
import ie.setu.questledger.data.rules.QuickSetupEngine
import ie.setu.questledger.data.rules.QuickSetupResult
import ie.setu.questledger.models.QuickSetupConfig
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickSetupViewModel @Inject constructor(
    private val repository: FirestoreService,
    private val authService: AuthService,
    private val compendiumService: CompendiumService,
    private val quickSetupEngine: QuickSetupEngine
) : ViewModel() {

    var isLoading = mutableStateOf(false)
    var isErr = mutableStateOf(false)
    var error = mutableStateOf("")

    fun getRaces(): List<RaceDefinition> = compendiumService.getRaces()

    fun getRaceVariantsForRace(raceId: String) =
        compendiumService.getRaceVariantsForRace(raceId)

    fun getClasses(): List<ClassDefinition> = compendiumService.getClasses()

    fun getBackgrounds(): List<BackgroundDefinition> = compendiumService.getBackgrounds()

    fun buildPreview(config: QuickSetupConfig): QuickSetupResult? {
        if (
            config.name.isBlank() ||
            config.raceId.isBlank() ||
            config.classId.isBlank() ||
            config.backgroundId.isBlank()
        ) {
            return null
        }

        return runCatching { quickSetupEngine.build(config) }.getOrNull()
    }

    fun saveQuickSetup(
        config: QuickSetupConfig,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                isErr.value = false

                val result = quickSetupEngine.build(config)
                val finalCharacter = result.character.copy(
                    email = authService.email
                )

                repository.insert(authService.email, finalCharacter)

                isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                isLoading.value = false
                isErr.value = true
                error.value = e.message ?: "Failed to save quick setup character"
            }
        }
    }
}
