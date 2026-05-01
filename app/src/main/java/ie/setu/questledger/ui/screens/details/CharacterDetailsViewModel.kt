package ie.setu.questledger.ui.screens.details

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.data.compendium.ClassDefinition
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.data.compendium.RaceDefinition
import ie.setu.questledger.data.firestore.FirestoreService
import ie.setu.questledger.data.rules.CharacterStatEngine
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

    fun getClasses(): List<ClassDefinition> = compendiumService.getClasses()

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
        level: Int,
        notes: String,
        strength: Int,
        dexterity: Int,
        constitution: Int,
        intelligence: Int,
        wisdom: Int,
        charisma: Int,
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

                val baseCharacter = character.value.copy(
                    email = authService.email,
                    name = name,
                    characterClass = characterClass,
                    race = race,
                    level = level,
                    notes = notes,
                    imageUri = uploadedImageUri,
                    strength = strength,
                    dexterity = dexterity,
                    constitution = constitution,
                    intelligence = intelligence,
                    wisdom = wisdom,
                    charisma = charisma,
                    inventory = character.value.inventory,
                    knownSpellIds = character.value.knownSpellIds,
                    preparedSpellIds = character.value.preparedSpellIds
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