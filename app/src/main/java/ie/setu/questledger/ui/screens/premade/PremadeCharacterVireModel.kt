package ie.setu.questledger.ui.screens.premade

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.data.firestore.FirestoreService
import ie.setu.questledger.data.premade.PremadeTemplateService
import ie.setu.questledger.data.rules.QuickSetupEngine
import ie.setu.questledger.data.rules.QuickSetupResult
import ie.setu.questledger.models.PremadeCharacterTemplate
import ie.setu.questledger.models.QuickSetupConfig
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremadeCharactersViewModel @Inject constructor(
    private val premadeTemplateService: PremadeTemplateService,
    private val quickSetupEngine: QuickSetupEngine,
    private val repository: FirestoreService,
    private val authService: AuthService
) : ViewModel() {

    var isLoading = mutableStateOf(false)
    var isErr = mutableStateOf(false)
    var error = mutableStateOf("")

    fun getTemplates(): List<PremadeCharacterTemplate> = premadeTemplateService.getTemplates()

    fun buildPreview(templateId: String, customName: String): QuickSetupResult? {
        val template = premadeTemplateService.getTemplateById(templateId) ?: return null

        val config = QuickSetupConfig(
            name = if (customName.isBlank()) template.defaultName else customName.trim(),
            raceId = template.raceId,
            classId = template.classId,
            level = template.level
        )

        return runCatching { quickSetupEngine.build(config) }.getOrNull()
    }

    fun savePremade(
        templateId: String,
        customName: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                isErr.value = false

                val template = premadeTemplateService.getTemplateById(templateId)
                    ?: throw IllegalArgumentException("Unknown premade template")

                val config = QuickSetupConfig(
                    name = if (customName.isBlank()) template.defaultName else customName.trim(),
                    raceId = template.raceId,
                    classId = template.classId,
                    level = template.level
                )

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
                error.value = e.message ?: "Failed to save premade character"
            }
        }
    }
}