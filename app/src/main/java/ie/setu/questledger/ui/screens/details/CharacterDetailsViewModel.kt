package ie.setu.questledger.ui.screens.details

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.setu.questledger.data.local.CharacterEntity
import ie.setu.questledger.data.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterDetailsViewModel @Inject constructor(
    private val repository: CharacterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val character = mutableStateOf(
        CharacterEntity(
            id = 0,
            name = "",
            characterClass = "",
            race = "",
            level = 1,
            notes = ""
        )
    )

    private val id: Long = checkNotNull(savedStateHandle["id"])

    var isErr = mutableStateOf(false)
    var error = mutableStateOf(Exception())
    var isLoading = mutableStateOf(false)

    init {
        viewModelScope.launch {
            try {
                isLoading.value = true
                character.value = repository.getFromApi(id)
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

                val updatedCharacter = repository.updateInApi(
                    character.value.copy(
                        name = name,
                        characterClass = characterClass,
                        race = race,
                        level = level,
                        notes = notes
                    )
                )

                repository.update(updatedCharacter)
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