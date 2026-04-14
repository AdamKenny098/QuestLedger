package ie.setu.questledger.ui.screens.create

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.data.firestore.FirestoreService
import ie.setu.questledger.data.storage.StorageService
import ie.setu.questledger.models.CharacterModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val repository: FirestoreService,
    private val authService: AuthService,
    private val storageService: StorageService
) : ViewModel() {

    var isErr = mutableStateOf(false)
    var error = mutableStateOf(Exception())
    var isLoading = mutableStateOf(false)

    fun addCharacter(
        name: String,
        characterClass: String,
        race: String,
        level: Int,
        notes: String,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                isErr.value = false

                val uploadedImageUri =
                    if (imageUri != null) {
                        storageService.uploadFile(imageUri, "characters").toString()
                    } else {
                        ""
                    }

                val character = CharacterModel(
                    name = name,
                    characterClass = characterClass,
                    race = race,
                    level = level,
                    notes = notes,
                    email = authService.email,
                    imageUri = uploadedImageUri
                )

                repository.insert(authService.email, character)

                isLoading.value = false
            } catch (e: Exception) {
                isLoading.value = false
                isErr.value = true
                error.value = e
            }
        }
    }
}