package ie.setu.questledger.ui.screens.spellbook

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.data.firestore.FirestoreService
import ie.setu.questledger.data.rules.CharacterStatEngine
import ie.setu.questledger.models.characters.CharacterModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterSpellbookViewModel @Inject constructor(
    private val repository: FirestoreService,
    private val authService: AuthService,
    private val compendiumService: CompendiumService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val character = mutableStateOf(CharacterModel())

    var isLoading = mutableStateOf(false)
    var isErr = mutableStateOf(false)
    var error = mutableStateOf("")

    private val id: String = checkNotNull(savedStateHandle["id"])

    init {
        viewModelScope.launch {
            try {
                isLoading.value = true
                character.value = repository.get(authService.email, id) ?: CharacterModel()
                isLoading.value = false
            } catch (e: Exception) {
                isLoading.value = false
                isErr.value = true
                error.value = e.message ?: "Failed to load spellbook"
            }
        }
    }

    fun getCompendiumService(): CompendiumService = compendiumService

    fun isSpellcaster(): Boolean {
        return CharacterStatEngine.build(character.value).spellcastingAbilityLabel != null
    }

    fun toggleKnownSpell(spellId: String) {
        val current = character.value
        val known = current.knownSpellIds.toMutableList()

        if (known.contains(spellId)) {
            known.remove(spellId)
        } else {
            known.add(spellId)
        }

        val prepared = current.preparedSpellIds.filter { it in known }

        character.value = current.copy(
            knownSpellIds = known,
            preparedSpellIds = prepared
        )
    }

    fun togglePreparedSpell(spellId: String) {
        val current = character.value
        if (!current.knownSpellIds.contains(spellId)) return

        val prepared = current.preparedSpellIds.toMutableList()

        if (prepared.contains(spellId)) {
            prepared.remove(spellId)
        } else {
            prepared.add(spellId)
        }

        character.value = current.copy(
            preparedSpellIds = prepared
        )
    }

    fun saveSpellbook(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                isErr.value = false
                error.value = ""

                repository.update(authService.email, character.value)

                isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                isLoading.value = false
                isErr.value = true
                error.value = e.message ?: "Failed to save spellbook"
            }
        }
    }
}