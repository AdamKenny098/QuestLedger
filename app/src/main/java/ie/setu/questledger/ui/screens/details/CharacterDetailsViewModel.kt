package ie.setu.questledger.ui.screens.details

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.data.compendium.ClassDefinition
import ie.setu.questledger.data.compendium.BackgroundDefinition
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.data.compendium.RaceDefinition
import ie.setu.questledger.data.compendium.AbilityType
import ie.setu.questledger.data.compendium.SeedRaceVariantData
import ie.setu.questledger.data.firestore.FirestoreService
import ie.setu.questledger.data.rules.CharacterStatEngine
import ie.setu.questledger.data.rules.CharacterSessionRules
import ie.setu.questledger.data.rules.CharacterFeatureRules
import ie.setu.questledger.data.rules.CharacterBackgroundRules
import ie.setu.questledger.data.rules.CharacterAdvancementRules
import ie.setu.questledger.data.rules.CharacterRaceRules
import ie.setu.questledger.data.rules.CharacterSubclassRules
import ie.setu.questledger.data.rules.CurrencyRules
import ie.setu.questledger.data.rules.InventoryEngine
import ie.setu.questledger.data.storage.StorageService
import ie.setu.questledger.models.characters.CharacterModel
import ie.setu.questledger.models.characters.CharacterAdvancementSelection
import ie.setu.questledger.models.inventory.CurrencyDenomination
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random
import javax.inject.Inject
@HiltViewModel
class CharacterDetailsViewModel @Inject constructor(
    private val repository: FirestoreService,
    private val authService: AuthService,
    private val storageService: StorageService,
    private val compendiumService: CompendiumService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val character = mutableStateOf(CharacterModel())

    private val id: String = checkNotNull(savedStateHandle["id"])

    fun getCompendiumService(): CompendiumService = compendiumService

    var isErr = mutableStateOf(false)
    var error = mutableStateOf(Exception())
    var isLoading = mutableStateOf(false)
    var isSessionSaving = mutableStateOf(false)
    var sessionMessage = mutableStateOf("")

    private val sessionSaveMutex = Mutex()

    init {
        viewModelScope.launch {
            try {
                isLoading.value = true
                val loaded = repository.get(authService.email, id) ?: CharacterModel()
                val playableCharacter = CharacterSessionRules.normalise(
                    CharacterSubclassRules.normalise(loaded)
                )
                character.value = playableCharacter
                if (loaded.id.isNotBlank() && loaded != playableCharacter) {
                    repository.update(authService.email, playableCharacter)
                }
                isLoading.value = false
            } catch (e: Exception) {
                isLoading.value = false
                isErr.value = true
                error.value = e
            }
        }
    }

    fun getRaces(): List<RaceDefinition> = compendiumService.getRaces()

    fun getRaceVariantsForRace(raceId: String) =
        compendiumService.getRaceVariantsForRace(raceId)

    fun getClasses(): List<ClassDefinition> = compendiumService.getClasses()

    fun getSubclassesForClass(classId: String) =
        compendiumService.getSubclassesForClass(classId)

    fun getSubclassChoiceGroups(subclassId: String, level: Int) =
        CharacterSubclassRules.unlockedChoiceGroups(
            compendiumService.getSubclassById(subclassId),
            level
        )

    fun getBackgrounds(): List<BackgroundDefinition> = compendiumService.getBackgrounds()

    fun getEquipmentCatalogue() = compendiumService.getEquipmentCatalogue()

    fun getEquipmentPacks() = compendiumService.getEquipmentPacks()
    fun getFeats() = compendiumService.getFeats()

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
            compendiumService.getRaceVariantById(raceVariantId),
            compendiumService.getSpells()
        )

    fun equipItem(itemId: String) {
        val current = character.value
        val updatedInventory = InventoryEngine.equipItem(current.inventory, itemId)
        character.value = current.copy(inventory = updatedInventory)
    }

    fun unequipWeapon() {
        val current = character.value
        character.value = current.copy(
            inventory = InventoryEngine.unequipWeapon(current.inventory)
        )
    }

    fun unequipArmour() {
        val current = character.value
        character.value = current.copy(
            inventory = InventoryEngine.unequipArmour(current.inventory)
        )
    }

    fun unequipOffhand() {
        val current = character.value
        character.value = current.copy(
            inventory = InventoryEngine.unequipOffhand(current.inventory)
        )
    }

    fun unequipSpellFocus() {
        val current = character.value
        character.value = current.copy(
            inventory = InventoryEngine.unequipSpellFocus(current.inventory)
        )
    }

    fun removeItem(itemId: String) {
        val current = character.value
        character.value = current.copy(
            inventory = InventoryEngine.removeItem(current.inventory, itemId)
        )
    }

    fun changeItemQuantity(itemId: String, delta: Int) {
        val current = character.value
        character.value = current.copy(
            inventory = InventoryEngine.changeQuantity(
                current.inventory,
                itemId,
                delta
            )
        )
    }

    fun addCatalogueItem(itemId: String) {
        val current = character.value
        val entry = compendiumService.getEquipmentCatalogueItemById(itemId) ?: return
        character.value = current.copy(
            inventory = InventoryEngine.addCatalogueItem(current.inventory, entry)
        )
    }

    fun addEquipmentPack(packId: String) {
        val current = character.value
        val pack = compendiumService.getEquipmentPackById(packId) ?: return
        character.value = current.copy(
            inventory = InventoryEngine.addEquipmentPack(
                inventory = current.inventory,
                pack = pack,
                catalogue = compendiumService.getEquipmentCatalogue()
            )
        )
    }

    fun adjustCurrency(
        denomination: CurrencyDenomination,
        delta: Int
    ) {
        character.value = CurrencyRules.adjust(
            character = character.value,
            denomination = denomination,
            delta = delta
        )
    }

    fun toggleKnownSpell(spellId: String) {
        val current = character.value
        val known = current.knownSpellIds.toMutableList()

        if (known.contains(spellId)) {
            known.remove(spellId)
        } else {
            known.add(spellId)
        }

        val prepared = current.preparedSpellIds.filter { it in known }

        character.value = current.copy(
            knownSpellIds = known,
            preparedSpellIds = prepared
        )
    }

    fun togglePreparedSpell(spellId: String) {
        val current = character.value

        if (!current.knownSpellIds.contains(spellId)) return

        val prepared = current.preparedSpellIds.toMutableList()

        if (prepared.contains(spellId)) {
            prepared.remove(spellId)
        } else {
            prepared.add(spellId)
        }

        character.value = current.copy(
            preparedSpellIds = prepared
        )
    }

    fun takeDamage(amount: Int) {
        applySessionChange(
            transform = { CharacterSessionRules.takeDamage(it, amount) },
            successMessage = "Applied $amount damage."
        )
    }

    fun heal(amount: Int) {
        applySessionChange(
            transform = { CharacterSessionRules.heal(it, amount) },
            successMessage = "Restored $amount hit points."
        )
    }

    fun setTemporaryHitPoints(amount: Int) {
        applySessionChange(
            transform = { CharacterSessionRules.setTemporaryHitPoints(it, amount) },
            successMessage = "Temporary hit points set to $amount."
        )
    }

    fun rollDeathSave() {
        val roll = Random.nextInt(1, 21)
        val result = CharacterSessionRules.rollDeathSave(character.value, roll)
        applySessionChange(
            transform = { result.character },
            successMessage = "Rolled $roll. ${result.message}"
        )
    }

    fun resetDeathSaves() {
        applySessionChange(
            transform = CharacterSessionRules::resetDeathSaves,
            successMessage = "Death saves reset."
        )
    }

    fun spendHitDie() {
        val hitDie = CharacterStatEngine.build(character.value).hitDie
        val roll = Random.nextInt(1, hitDie + 1)
        runCatching {
            CharacterSessionRules.spendHitDie(character.value, roll)
        }.onSuccess { result ->
            val modifier = if (result.constitutionModifier >= 0) {
                "+${result.constitutionModifier}"
            } else {
                result.constitutionModifier.toString()
            }
            applySessionChange(
                transform = { result.character },
                successMessage =
                    "Hit die: ${result.dieRoll} $modifier. " +
                        "Recovered ${result.hitPointsRecovered} HP."
            )
        }.onFailure(::showSessionError)
    }

    fun useSpellSlot(spellLevel: Int) {
        applySessionChange(
            transform = { CharacterSessionRules.useSpellSlot(it, spellLevel) },
            successMessage = "Used a level $spellLevel spell slot."
        )
    }

    fun restoreSpellSlot(spellLevel: Int) {
        applySessionChange(
            transform = { CharacterSessionRules.restoreSpellSlot(it, spellLevel) },
            successMessage = "Restored a level $spellLevel spell slot."
        )
    }

    fun takeShortRest() {
        val restoresPactMagic = character.value.characterClass.equals(
            "warlock",
            ignoreCase = true
        )
        applySessionChange(
            transform = CharacterSessionRules::shortRest,
            successMessage = if (restoresPactMagic) {
                "Short rest complete. Pact Magic slots restored."
            } else {
                "Short rest complete. You may spend hit dice."
            }
        )
    }

    fun takeLongRest() {
        applySessionChange(
            transform = CharacterSessionRules::longRest,
            successMessage = "Long rest complete. HP and spell slots restored."
        )
    }

    fun toggleInspiration() {
        applySessionChange(
            transform = CharacterSessionRules::toggleInspiration,
            successMessage = if (character.value.hasInspiration) {
                "Inspiration removed."
            } else {
                "Inspiration granted."
            }
        )
    }

    fun useFeature(featureId: String, amount: Int) {
        val secondWindRoll = if (featureId == "fighter_second_wind") {
            Random.nextInt(1, 11)
        } else {
            null
        }
        runCatching {
            CharacterFeatureRules.useFeature(
                character = character.value,
                featureId = featureId,
                amount = amount,
                secondWindRoll = secondWindRoll
            )
        }.onSuccess { result ->
            applySessionChange(
                transform = { result.character },
                successMessage = result.message
            )
        }.onFailure(::showSessionError)
    }

    fun restoreFeature(featureId: String, amount: Int) {
        applySessionChange(
            transform = {
                CharacterFeatureRules.restoreFeature(
                    character = it,
                    featureId = featureId,
                    amount = amount
                )
            },
            successMessage = "Restored feature resource."
        )
    }

    fun endFeature(featureId: String) {
        applySessionChange(
            transform = { CharacterFeatureRules.endFeature(it, featureId) },
            successMessage = "Feature ended."
        )
    }

    private fun applySessionChange(
        transform: (CharacterModel) -> CharacterModel,
        successMessage: String
    ) {
        val updated = runCatching {
            transform(character.value)
        }.getOrElse {
            showSessionError(it)
            return
        }

        character.value = updated
        sessionMessage.value = successMessage
        isErr.value = false

        viewModelScope.launch {
            sessionSaveMutex.withLock {
                try {
                    isSessionSaving.value = true
                    repository.update(authService.email, character.value)
                } catch (e: Exception) {
                    showSessionError(e)
                } finally {
                    isSessionSaving.value = false
                }
            }
        }
    }

    private fun showSessionError(throwable: Throwable) {
        val exception = throwable as? Exception ?: Exception(throwable)
        isErr.value = true
        error.value = exception
        sessionMessage.value = exception.message ?: "Could not update session state."
    }

    fun updateCharacter(
        name: String,
        characterClass: String,
        subclass: String,
        race: String,
        raceVariant: String,
        background: String,
        level: Int,
        notes: String,
        strength: Int,
        dexterity: Int,
        constitution: Int,
        intelligence: Int,
        wisdom: Int,
        charisma: Int,
        selectedRacialAbilityBonusIds: List<String>,
        selectedRacialSkillIds: List<String>,
        selectedRacialLanguageIds: List<String>,
        selectedRacialSpellId: String,
        selectedSubclassChoiceIds: List<String>,
        advancementSelections: List<CharacterAdvancementSelection>,
        imageUri: Uri?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                isErr.value = false

                val uploadedImageUri =
                    if (imageUri != null) {
                        storageService.uploadFile(imageUri, "characters").toString()
                    } else {
                        character.value.imageUri
                    }

                val currentCharacter = character.value
                val oldBackground =
                    compendiumService.getBackgroundById(currentCharacter.background)
                val newBackground = requireNotNull(
                    compendiumService.getBackgroundById(background)
                ) {
                    "Unknown background: $background"
                }
                val oldRace = compendiumService.getRaceById(currentCharacter.race)
                val newRace = requireNotNull(compendiumService.getRaceById(race)) {
                    "Unknown race: $race"
                }
                val oldRaceVariant =
                    compendiumService.getRaceVariantById(currentCharacter.raceVariant)
                val newClass = requireNotNull(
                    compendiumService.getClassById(characterClass)
                ) {
                    "Unknown class: $characterClass"
                }
                val characterWithFormValues = currentCharacter.copy(
                    characterClass = characterClass,
                    subclass = subclass,
                    level = level,
                    strength = strength,
                    dexterity = dexterity,
                    constitution = constitution,
                    intelligence = intelligence,
                    wisdom = wisdom,
                    charisma = charisma
                )
                val characterWithRace = CharacterRaceRules.applySelection(
                    character = characterWithFormValues,
                    oldRace = oldRace,
                    oldVariant = oldRaceVariant,
                    newRace = newRace,
                    newVariantsForRace = compendiumService.getRaceVariantsForRace(race),
                    characterClass = newClass,
                    background = newBackground,
                    level = level,
                    spells = compendiumService.getSpells(),
                    requestedVariantId = raceVariant,
                    selectedFlexibleAbilityIds = selectedRacialAbilityBonusIds,
                    selectedRacialSkillIds = selectedRacialSkillIds,
                    selectedRacialLanguageIds = selectedRacialLanguageIds,
                    selectedRacialSpellId = selectedRacialSpellId
                )
                val characterWithBackground = CharacterBackgroundRules.applySelection(
                    character = characterWithRace,
                    newRace = newRace,
                    newBackground = newBackground,
                    oldRace = newRace,
                    oldBackground = oldBackground
                )
                val characterWithSubclass = CharacterSubclassRules.applySelection(
                    character = characterWithBackground,
                    characterClass = newClass,
                    level = level,
                    requestedSubclassId = subclass,
                    selectedChoiceIds = selectedSubclassChoiceIds,
                    subclasses = compendiumService.getSubclasses(),
                    spells = compendiumService.getSpells()
                )
                val characterWithoutOldAdvancement =
                    CharacterAdvancementRules.replaceSelections(
                        character = characterWithSubclass,
                        oldSelections = currentCharacter.advancementSelections,
                        newSelections = emptyList(),
                        feats = compendiumService.getFeats()
                    )
                val baseScores = mapOf(
                    AbilityType.STRENGTH to characterWithoutOldAdvancement.strength,
                    AbilityType.DEXTERITY to characterWithoutOldAdvancement.dexterity,
                    AbilityType.CONSTITUTION to characterWithoutOldAdvancement.constitution,
                    AbilityType.INTELLIGENCE to characterWithoutOldAdvancement.intelligence,
                    AbilityType.WISDOM to characterWithoutOldAdvancement.wisdom,
                    AbilityType.CHARISMA to characterWithoutOldAdvancement.charisma
                )
                val resolvedVariant = compendiumService.getRaceVariantById(
                    characterWithSubclass.raceVariant
                )
                CharacterAdvancementRules.validateSelections(
                    selections = advancementSelections,
                    characterClass = newClass,
                    raceVariant = resolvedVariant,
                    level = level,
                    baseScores = baseScores,
                    feats = compendiumService.getFeats(),
                    racialArmourProficiencies =
                        characterWithBackground.racialArmourProficiencyIds
                )
                val characterWithAdvancement =
                    CharacterAdvancementRules.replaceSelections(
                        character = characterWithSubclass,
                        oldSelections = currentCharacter.advancementSelections,
                        newSelections = advancementSelections,
                        feats = compendiumService.getFeats()
                    )

                val baseCharacter = characterWithAdvancement.copy(
                    email = authService.email,
                    name = name,
                    characterClass = characterClass,
                    subclass = characterWithAdvancement.subclass,
                    race = race,
                    raceVariant = characterWithAdvancement.raceVariant,
                    background = background,
                    level = level,
                    notes = notes,
                    imageUri = uploadedImageUri,
                    knownSpellIds = characterWithAdvancement.knownSpellIds,
                    preparedSpellIds = characterWithAdvancement.preparedSpellIds,
                    advancementSelections = advancementSelections
                )

                val updatedCharacter = CharacterSessionRules.reconcileProgression(
                    oldCharacter = currentCharacter,
                    updatedCharacter = baseCharacter
                )

                repository.update(authService.email, updatedCharacter)
                character.value = updatedCharacter

                isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                isLoading.value = false
                isErr.value = true
                error.value = e
            }
        }
    }
}
