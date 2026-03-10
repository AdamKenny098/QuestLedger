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

    init {
        viewModelScope.launch {
            repository.getById(id).collect { loaded ->
                character.value = loaded
            }
        }
    }

    fun updateCharacter(updated: CharacterEntity) {
        viewModelScope.launch {
            repository.update(updated)
        }
    }
}
