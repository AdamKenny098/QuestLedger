package ie.setu.questledger.ui.screens.fullsetup

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.data.compendium.AbilityType
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.data.firestore.FirestoreService
import ie.setu.questledger.data.rules.FullSetupEngine
import ie.setu.questledger.data.rules.FullSetupResult
import ie.setu.questledger.models.FullSetupConfig
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FullSetupViewModel @Inject constructor(
    private val compendiumService: CompendiumService,
    private val fullSetupEngine: FullSetupEngine,
    private val repository: FirestoreService,
    private val authService: AuthService
) : ViewModel() {

    var isLoading = mutableStateOf(false)
    var isErr = mutableStateOf(false)
    var error = mutableStateOf("")

    fun getClasses() = compendiumService.getClasses()
    fun getRaces() = compendiumService.getRaces()
    fun getWeapons() = compendiumService.getWeapons()
    fun getSpells() = compendiumService.getSpells()

    fun getSuggestedProficienciesForClass(classId: String): List<String> {
        return when (classId.lowercase()) {
            "fighter" -> listOf("Athletics", "Intimidation", "Survival", "Perception")
            "wizard" -> listOf("Arcana", "History", "Investigation", "Insight")
            "cleric" -> listOf("Religion", "Insight", "Medicine", "Persuasion")
            "rogue" -> listOf("Stealth", "Acrobatics", "Sleight of Hand", "Perception")
            else -> listOf("Perception", "Insight", "Survival")
        }
    }

    fun getSuggestedWeaponIdsForClass(classId: String): List<String> {
        return when (classId.lowercase()) {
            "fighter" -> listOf("longsword", "dagger")
            "wizard" -> listOf("quarterstaff", "dagger")
            "cleric" -> listOf("quarterstaff", "mace")
            "rogue" -> listOf("dagger", "shortsword")
            else -> listOf("dagger")
        }
    }

    fun getSuggestedArmourIdsForClass(classId: String): List<String> {
        return when (classId.lowercase()) {
            "fighter" -> listOf("chainmail", "leather")
            "cleric" -> listOf("chainshirt", "leather")
            "rogue" -> listOf("leather")
            "wizard" -> emptyList()
            else -> listOf("leather")
        }
    }

    fun getSuggestedSpellIdsForClass(classId: String): List<String> {
        return when (classId.lowercase()) {
            "wizard" -> listOf("fire_bolt", "magic_missile", "shield")
            "cleric" -> listOf("sacred_flame", "cure_wounds", "bless")
            else -> emptyList()
        }
    }

    fun classUsesSpells(classId: String): Boolean {
        val clazz = compendiumService.getClassById(classId) ?: return false
        return clazz.spellcastingAbility != AbilityType.NONE
    }

    fun buildPreview(config: FullSetupConfig): FullSetupResult? {
        return runCatching { fullSetupEngine.build(config) }.getOrNull()
    }

    fun saveCharacter(
        config: FullSetupConfig,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                isErr.value = false

                val result = fullSetupEngine.build(config)
                val finalCharacter = result.character.copy(
                    email = authService.email
                )

                repository.insert(authService.email, finalCharacter)

                isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                isLoading.value = false
                isErr.value = true
                error.value = e.message ?: "Failed to save full setup character"
            }
        }
    }
}