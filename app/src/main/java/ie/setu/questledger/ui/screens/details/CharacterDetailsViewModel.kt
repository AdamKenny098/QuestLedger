package ie.setu.questledger.ui.screens.details

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.data.firestore.FirestoreService
import ie.setu.questledger.models.CharacterModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterDetailsViewModel @Inject constructor(
    private val repository: FirestoreService,
    private val authService: AuthService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val character = mutableStateOf(CharacterModel())

    private val id: String = checkNotNull(savedStateHandle["id"])

    var isErr = mutableStateOf(false)
    var error = mutableStateOf(Exception())
    var isLoading = mutableStateOf(false)

    init {
        viewModelScope.launch {
            try {
                isLoading.value = true
                character.value = repository.get(authService.email, id) ?: CharacterModel()
                isLoading.value = false
            } catch (e: Exception) {
                isLoading.value = false
                isErr.value = true
                error.value = e
            }
        }
    }

    fun updateCharacter(
        name: String,
        characterClass: String,
        race: String,
        level: Int,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                isErr.value = false

                val updatedCharacter = character.value.copy(
                    email = authService.email,
                    name = name,
                    characterClass = characterClass,
                    race = race,
                    level = level,
                    notes = notes
                )

                repository.update(authService.email, updatedCharacter)
                character.value = updatedCharacter

                isLoading.value = false
            } catch (e: Exception) {
                isLoading.value = false
                isErr.value = true
                error.value = e
            }
        }
    }
}