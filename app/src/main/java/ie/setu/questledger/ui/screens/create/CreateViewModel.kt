package ie.setu.questledger.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.setu.questledger.data.local.CharacterEntity
import ie.setu.questledger.data.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val repository: CharacterRepository
) : ViewModel() {

    fun addCharacter(
        name: String,
        characterClass: String,
        race: String,
        level: Int,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insert(
                CharacterEntity(
                    name = name,
                    characterClass = characterClass,
                    race = race,
                    level = level,
                    notes = notes
                )
            )
        }
    }
}
