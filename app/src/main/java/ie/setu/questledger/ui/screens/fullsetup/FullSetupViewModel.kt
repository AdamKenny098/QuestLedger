package ie.setu.questledger.ui.screens.fullsetup

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.data.compendium.AbilityType
import ie.setu.questledger.data.compendium.SeedRaceVariantData
import ie.setu.questledger.data.firestore.FirestoreService
import ie.setu.questledger.data.rules.CharacterRaceRules
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
    fun getRaceVariantsForRace(raceId: String) =
        compendiumService.getRaceVariantsForRace(raceId)
    fun getBackgrounds() = compendiumService.getBackgrounds()
    fun getWeapons() = compendiumService.getWeapons()
    fun getArmour() = compendiumService.getArmour()
    fun getSpells() = compendiumService.getSpells()

    fun getFlexibleAbilityChoices(
        raceId: String,
        raceVariantId: String
    ): List<AbilityType> {
        val race = compendiumService.getRaceById(raceId) ?: return emptyList()
        val variant = compendiumService.getRaceVariantById(raceVariantId)
            ?.takeIf { it.raceId == raceId }
        return CharacterRaceRules.availableFlexibleAbilities(race, variant)
    }

    fun getFlexibleAbilityChoiceCount(raceId: String): Int {
        return compendiumService.getRaceById(raceId)?.flexibleStatBonuses?.size ?: 0
    }

    fun getRacialSkillOptions(raceId: String, raceVariantId: String): List<String> {
        val race = compendiumService.getRaceById(raceId) ?: return emptyList()
        val variant = compendiumService.getRaceVariantById(raceVariantId)
            ?.takeIf { it.raceId == raceId }
        return (race.skillChoiceOptions + variant?.skillChoiceOptions.orEmpty())
            .distinct()
            .ifEmpty { SeedRaceVariantData.allSkills }
    }

    fun getRacialSkillChoiceCount(raceId: String, raceVariantId: String): Int {
        val race = compendiumService.getRaceById(raceId) ?: return 0
        val variant = compendiumService.getRaceVariantById(raceVariantId)
            ?.takeIf { it.raceId == raceId }
        return race.skillChoiceCount + (variant?.skillChoiceCount ?: 0)
    }

    fun getFixedRacialSkillIds(raceId: String, raceVariantId: String): List<String> {
        val race = compendiumService.getRaceById(raceId) ?: return emptyList()
        val variant = compendiumService.getRaceVariantById(raceVariantId)
            ?.takeIf { it.raceId == raceId }
        return (race.skillProficiencyIds + variant?.skillProficiencyIds.orEmpty())
            .distinct()
    }

    fun getRacialLanguageChoiceCount(raceId: String, raceVariantId: String): Int {
        val race = compendiumService.getRaceById(raceId) ?: return 0
        val variant = compendiumService.getRaceVariantById(raceVariantId)
            ?.takeIf { it.raceId == raceId }
        return race.languageChoiceCount + (variant?.languageChoiceCount ?: 0)
    }

    fun getRacialLanguageOptions(
        raceId: String,
        raceVariantId: String,
        backgroundId: String
    ): List<String> {
        val race = compendiumService.getRaceById(raceId) ?: return emptyList()
        val variant = compendiumService.getRaceVariantById(raceVariantId)
            ?.takeIf { it.raceId == raceId }
        val background = compendiumService.getBackgroundById(backgroundId)
        val blocked = (
            race.languages +
                variant?.languages.orEmpty() +
                background?.suggestedLanguages.orEmpty()
            ).toSet()
        return SeedRaceVariantData.languageOptions.filterNot { it in blocked }
    }

    fun getRacialCantripOptions(raceVariantId: String) =
        CharacterRaceRules.availableRacialCantrips(
            variant = compendiumService.getRaceVariantById(raceVariantId),
            spells = compendiumService.getSpells()
        )

    fun getSuggestedProficienciesForClass(classId: String): List<String> {
        return compendiumService.getClassById(classId)?.skillProficiencies.orEmpty()
    }

    fun getProficiencyChoiceCount(classId: String): Int {
        return compendiumService.getClassById(classId)?.skillChoiceCount ?: 0
    }

    fun getSuggestedWeaponIdsForClass(classId: String): List<String> {
        return compendiumService.getClassById(classId)?.starterWeaponIds.orEmpty()
    }

    fun getSuggestedArmourIdsForClass(classId: String): List<String> {
        return compendiumService.getClassById(classId)?.starterArmourIds.orEmpty()
    }

    fun getSuggestedSpellIdsForClass(classId: String): List<String> {
        return compendiumService.getClassById(classId)?.starterSpellIds.orEmpty()
    }

    fun classUsesSpells(classId: String): Boolean {
        val clazz = compendiumService.getClassById(classId) ?: return false
        return clazz.canCastAt(level = 1)
    }

    fun classCanStartWithShield(classId: String): Boolean {
        val clazz = compendiumService.getClassById(classId) ?: return false
        return ie.setu.questledger.data.compendium.ArmourType.SHIELD in clazz.armourProficiencies
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
