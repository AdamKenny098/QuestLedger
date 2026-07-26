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
import ie.setu.questledger.data.rules.CharacterSubclassRules
import ie.setu.questledger.data.rules.CharacterAdvancementRules
import ie.setu.questledger.data.rules.FullSetupEngine
import ie.setu.questledger.data.rules.FullSetupResult
import ie.setu.questledger.data.rules.SpellRules
import ie.setu.questledger.data.rules.StartingSpellLimits
import ie.setu.questledger.models.FullSetupConfig
import ie.setu.questledger.models.characters.CharacterModel
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
    fun getSubclassesForClass(classId: String) =
        compendiumService.getSubclassesForClass(classId)
    fun getSubclassChoiceGroups(subclassId: String, level: Int = 1) =
        CharacterSubclassRules.unlockedChoiceGroups(
            compendiumService.getSubclassById(subclassId),
            level
        )
    fun getRaces() = compendiumService.getRaces()
    fun getRaceVariantsForRace(raceId: String) =
        compendiumService.getRaceVariantsForRace(raceId)
    fun getBackgrounds() = compendiumService.getBackgrounds()
    fun getWeapons() = compendiumService.getWeapons()
    fun getArmour() = compendiumService.getArmour()
    fun getEquipmentPacks() = compendiumService.getEquipmentPacks()
    fun getFeats() = compendiumService.getFeats()

    fun getEligibleFeats(
        classId: String,
        raceId: String,
        raceVariantId: String,
        selectedFlexibleAbilityIds: List<String>,
        level: Int,
        baseScores: Map<AbilityType, Int>
    ) = compendiumService.getClassById(classId)?.let { clazz ->
        val race = compendiumService.getRaceById(raceId) ?: return@let emptyList()
        val variant = compendiumService.getRaceVariantById(raceVariantId)
            ?.takeIf { it.raceId == raceId }
        val scores = runCatching {
            ie.setu.questledger.data.rules.AbilityScoreRules.applyRaceBonuses(
                baseScores = baseScores,
                race = race,
                raceVariant = variant,
                classPriority = clazz.quickBuildAbilityPriority,
                selectedFlexibleAbilities = selectedFlexibleAbilityIds.map(
                    AbilityType::valueOf
                )
            )
        }.getOrElse { baseScores }
        CharacterAdvancementRules.eligibleFeats(
            characterClass = clazz,
            level = level,
            scores = scores,
            feats = compendiumService.getFeats()
        )
    }.orEmpty()

    fun getFlexibleAbilityChoices(
        raceId: String,
        raceVariantId: String
    ): List<AbilityType> {
        val race = compendiumService.getRaceById(raceId) ?: return emptyList()
        val variant = compendiumService.getRaceVariantById(raceVariantId)
            ?.takeIf { it.raceId == raceId }
        return CharacterRaceRules.availableFlexibleAbilities(race, variant)
    }

    fun getFlexibleAbilityChoiceCount(raceId: String, raceVariantId: String): Int {
        val race = compendiumService.getRaceById(raceId) ?: return 0
        val variant = compendiumService.getRaceVariantById(raceVariantId)
            ?.takeIf { it.raceId == raceId }
        return ie.setu.questledger.data.rules.AbilityScoreRules
            .effectiveFlexibleBonuses(race, variant)
            .size
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

    fun getSuggestedPackIdsForClass(classId: String): List<String> {
        return compendiumService.getClassById(classId)?.starterPackIds.orEmpty()
    }

    fun getSuggestedSpellIdsForClass(classId: String): List<String> {
        return compendiumService.getClassById(classId)?.starterSpellIds.orEmpty()
    }

    fun getStartingSpellOptions(
        classId: String,
        level: Int = 1,
        subclassId: String = "",
        selectedSubclassChoiceIds: List<String> = emptyList()
    ) = SpellRules.availableSpells(
        character = CharacterModel(
            characterClass = classId,
            subclass = subclassId,
            level = level,
            selectedSubclassChoiceIds = selectedSubclassChoiceIds
        ),
        compendiumService = compendiumService
    )

    fun getStartingSpellLimits(classId: String): StartingSpellLimits {
        val clazz = compendiumService.getClassById(classId)
            ?: return StartingSpellLimits(cantrips = 0, levelledSpells = 0)
        return SpellRules.startingLimits(clazz)
    }

    fun classUsesSpells(classId: String, level: Int = 1): Boolean {
        val clazz = compendiumService.getClassById(classId) ?: return false
        return clazz.canCastAt(level)
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
