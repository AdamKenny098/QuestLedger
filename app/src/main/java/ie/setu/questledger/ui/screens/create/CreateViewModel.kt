package ie.setu.questledger.ui.screens.create

import android.net.Uri
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
import ie.setu.questledger.data.rules.CharacterSessionRules
import ie.setu.questledger.data.rules.CharacterSubclassRules
import ie.setu.questledger.data.storage.StorageService
import ie.setu.questledger.models.characters.CharacterModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val repository: FirestoreService,
    private val authService: AuthService,
    private val storageService: StorageService,
    private val compendiumService: CompendiumService
) : ViewModel() {

    var isErr = mutableStateOf(false)
    var error = mutableStateOf(Exception())
    var isLoading = mutableStateOf(false)

    fun getRaces(): List<RaceDefinition> = compendiumService.getRaces()
    fun getClasses(): List<ClassDefinition> = compendiumService.getClasses()
    fun getBackgrounds(): List<BackgroundDefinition> = compendiumService.getBackgrounds()

    fun addCharacter(
        name: String,
        characterClass: String,
        race: String,
        level: Int,
        notes: String,
        imageUri: Uri?,
        strength: Int,
        dexterity: Int,
        constitution: Int,
        intelligence: Int,
        wisdom: Int,
        charisma: Int
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

                val baseCharacter = CharacterModel(
                    name = name,
                    characterClass = characterClass,
                    race = race,
                    level = level,
                    notes = notes,
                    email = authService.email,
                    imageUri = uploadedImageUri,
                    strength = strength,
                    dexterity = dexterity,
                    constitution = constitution,
                    intelligence = intelligence,
                    wisdom = wisdom,
                    charisma = charisma
                )

                val finalCharacter = CharacterSessionRules.initialise(
                    CharacterSubclassRules.normalise(baseCharacter)
                )

                repository.insert(authService.email, finalCharacter)

                isLoading.value = false
            } catch (e: Exception) {
                isLoading.value = false
                isErr.value = true
                error.value = e
            }
        }
    }
}
