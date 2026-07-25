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
import ie.setu.questledger.data.rules.CharacterBackgroundRules
import ie.setu.questledger.data.rules.CharacterRaceRules
import ie.setu.questledger.data.rules.InventoryEngine
import ie.setu.questledger.data.storage.StorageService
import ie.setu.questledger.models.characters.CharacterModel
import ie.setu.questledger.models.inventory.InventoryItemModel
import ie.setu.questledger.models.inventory.InventoryItemType
import kotlinx.coroutines.launch
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

    init {
        viewModelScope.launch {
            try {
                isLoading.value = true
                character.value = repository.get(authService.email, id) ?: CharacterModel()
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

    fun getBackgrounds(): List<BackgroundDefinition> = compendiumService.getBackgrounds()

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
            inventory = current.inventory.copy(
                equipped = current.inventory.equipped.copy(spellFocusId = null)
            )
        )
    }

    fun removeItem(itemId: String) {
        val current = character.value
        val removedInventory = InventoryEngine.removeItem(current.inventory, itemId)
        val cleanedInventory = removedInventory.copy(
            equipped = removedInventory.equipped.copy(
                weaponId = if (removedInventory.equipped.weaponId == itemId) null else removedInventory.equipped.weaponId,
                armourId = if (removedInventory.equipped.armourId == itemId) null else removedInventory.equipped.armourId,
                offhandId = if (removedInventory.equipped.offhandId == itemId) null else removedInventory.equipped.offhandId,
                spellFocusId = if (removedInventory.equipped.spellFocusId == itemId) null else removedInventory.equipped.spellFocusId
            )
        )
        character.value = current.copy(inventory = cleanedInventory)
    }

    fun addTestPotion() {
        val current = character.value
        val item = InventoryItemModel(
            id = "potion_healing_${System.currentTimeMillis()}",
            name = "Healing Potion",
            type = InventoryItemType.CONSUMABLE,
            slotCost = 1,
            quantity = 1
        )
        val updated = InventoryEngine.addItem(current.inventory, item)
        character.value = current.copy(inventory = updated)
    }

    fun addTestTool() {
        val current = character.value
        val item = InventoryItemModel(
            id = "tool_kit_${System.currentTimeMillis()}",
            name = "Tool Kit",
            type = InventoryItemType.TOOL,
            slotCost = 2,
            quantity = 1
        )
        val updated = InventoryEngine.addItem(current.inventory, item)
        character.value = current.copy(inventory = updated)
    }

    fun addTestShield() {
        val current = character.value
        val item = InventoryItemModel(
            id = "shield_extra_${System.currentTimeMillis()}",
            name = "Extra Shield",
            type = InventoryItemType.SHIELD,
            slotCost = 2,
            quantity = 1,
            shieldBonus = 2
        )
        val updated = InventoryEngine.addItem(current.inventory, item)
        character.value = current.copy(inventory = updated)
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
    fun updateCharacter(
        name: String,
        characterClass: String,
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

                val baseCharacter = characterWithBackground.copy(
                    email = authService.email,
                    name = name,
                    characterClass = characterClass,
                    race = race,
                    raceVariant = characterWithBackground.raceVariant,
                    background = background,
                    level = level,
                    notes = notes,
                    imageUri = uploadedImageUri,
                    knownSpellIds = characterWithBackground.knownSpellIds,
                    preparedSpellIds = characterWithBackground.preparedSpellIds
                )

                val derived = CharacterStatEngine.build(baseCharacter)

                val updatedCharacter = baseCharacter.copy(
                    currentHp = when {
                        character.value.currentHp <= 0 -> derived.maxHp
                        character.value.currentHp > derived.maxHp -> derived.maxHp
                        else -> character.value.currentHp
                    }
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
