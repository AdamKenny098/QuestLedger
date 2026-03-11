package ie.setu.questledger.ui.screens.create

import androidx.compose.runtime.mutableStateOf
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

    var isErr = mutableStateOf(false)
    var error = mutableStateOf(Exception())
    var isLoading = mutableStateOf(false)

    fun addCharacter(
        name: String,
        characterClass: String,
        race: String,
        level: Int,
        notes: String
    ) {
        viewModelScope.launch {
            try{
                isLoading.value = true
                isErr.value = false

                val createdCharacter = repository.insertToApi(
                    CharacterEntity(
                        name = name,
                        characterClass = characterClass,
                        race = race,
                        level = level,
                        notes = notes
                    )
                )
            repository.insert(createdCharacter)

            isLoading.value = false
            } catch (e : Exception){
                isLoading.value = false
                isErr.value = true
                error.value = e
            }
        }
    }
}
