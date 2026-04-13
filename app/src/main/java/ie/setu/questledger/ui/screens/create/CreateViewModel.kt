package ie.setu.questledger.ui.screens.create

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.data.firestore.FirestoreService
import ie.setu.questledger.models.CharacterModel

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val repository: FirestoreService,
    private val authService: AuthService
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

                val character = CharacterModel(
                    name = name,
                    characterClass = characterClass,
                    race = race,
                    level = level,
                    notes = notes,
                    email = authService.email
                )
            repository.insert(authService.email, character)

            isLoading.value = false
            } catch (e : Exception){
                isLoading.value = false
                isErr.value = true
                error.value = e
            }
        }
    }
}
