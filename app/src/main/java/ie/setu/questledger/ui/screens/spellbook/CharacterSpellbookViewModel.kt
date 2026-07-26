package ie.setu.questledger.ui.screens.spellbook

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.data.compendium.SpellLearningMode
import ie.setu.questledger.data.firestore.FirestoreService
import ie.setu.questledger.data.rules.CharacterStatEngine
import ie.setu.questledger.data.rules.CharacterSessionRules
import ie.setu.questledger.data.rules.CharacterSubclassRules
import ie.setu.questledger.data.rules.SpellRules
import ie.setu.questledger.models.characters.CharacterModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class CharacterSpellbookViewModel @Inject constructor(
    private val repository: FirestoreService,
    private val authService: AuthService,
    private val compendiumService: CompendiumService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val character = mutableStateOf(CharacterModel())

    var isLoading = mutableStateOf(false)
    var isErr = mutableStateOf(false)
    var error = mutableStateOf("")
    var isSessionSaving = mutableStateOf(false)

    private val id: String = checkNotNull(savedStateHandle["id"])
    private val sessionSaveMutex = Mutex()

    init {
        viewModelScope.launch {
            try {
                isLoading.value = true
                val loaded = repository.get(authService.email, id) ?: CharacterModel()
                character.value = sanitiseSpellbook(
                    CharacterSessionRules.normalise(
                        CharacterSubclassRules.normalise(loaded)
                    )
                )
                isLoading.value = false
            } catch (e: Exception) {
                isLoading.value = false
                isErr.value = true
                error.value = e.message ?: "Failed to load spellbook"
            }
        }
    }

    fun getCompendiumService(): CompendiumService = compendiumService

    fun isSpellcaster(): Boolean {
        return CharacterStatEngine.build(character.value).spellcastingAbilityLabel != null
    }

    fun toggleKnownSpell(spellId: String) {
        val current = character.value
        if (spellId in current.subclassSpellIds) return
        val availableSpellIds = SpellRules.availableSpells(current, compendiumService)
            .mapTo(mutableSetOf()) { it.id }
        if (spellId !in availableSpellIds) return

        val known = current.knownSpellIds.toMutableList()

        if (known.contains(spellId)) {
            if (spellId in current.racialSpellIds) return
            known.remove(spellId)
        } else {
            known.add(spellId)
        }

        val prepared = current.preparedSpellIds.filter { it in known }

        character.value = current.copy(
            knownSpellIds = known.distinct(),
            preparedSpellIds = prepared
        )
        clearError()
    }

    fun togglePreparedSpell(spellId: String) {
        val current = character.value
        if (spellId in current.subclassSpellIds) return
        val clazz = compendiumService.getClassById(current.characterClass) ?: return
        if (
            clazz.spellLearningMode != SpellLearningMode.PREPARED &&
            clazz.spellLearningMode != SpellLearningMode.SPELLBOOK
        ) {
            return
        }
        if (!current.knownSpellIds.contains(spellId)) return
        val spell = compendiumService.getSpellById(spellId) ?: return
        if (spell.isCantrip) return
        if (spellId !in SpellRules.availableSpells(current, compendiumService).map { it.id }) {
            return
        }

        val prepared = current.preparedSpellIds.toMutableList()

        if (prepared.contains(spellId)) {
            prepared.remove(spellId)
        } else {
            val limit = SpellRules.preparedSpellLimit(current, clazz)
            val preparedLevelledCount = prepared.count { preparedId ->
                preparedId !in current.subclassSpellIds &&
                    compendiumService.getSpellById(preparedId)?.isCantrip == false
            }
            if (preparedLevelledCount >= limit) {
                isErr.value = true
                error.value = "${clazz.name} can prepare up to $limit levelled spells"
                return
            }
            prepared.add(spellId)
        }

        character.value = current.copy(
            preparedSpellIds = prepared.distinct()
        )
        clearError()
    }

    fun useSpellSlot(spellLevel: Int) {
        updateSession {
            CharacterSessionRules.useSpellSlot(it, spellLevel)
        }
    }

    fun restoreSpellSlot(spellLevel: Int) {
        updateSession {
            CharacterSessionRules.restoreSpellSlot(it, spellLevel)
        }
    }

    fun saveSpellbook(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                isErr.value = false
                error.value = ""

                sessionSaveMutex.withLock {
                    repository.update(authService.email, character.value)
                }

                isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                isLoading.value = false
                isErr.value = true
                error.value = e.message ?: "Failed to save spellbook"
            }
        }
    }

    private fun clearError() {
        isErr.value = false
        error.value = ""
    }

    private fun updateSession(transform: (CharacterModel) -> CharacterModel) {
        val updated = runCatching {
            transform(character.value)
        }.getOrElse {
            isErr.value = true
            error.value = it.message ?: "Could not update spell slots"
            return
        }

        character.value = updated
        clearError()

        viewModelScope.launch {
            sessionSaveMutex.withLock {
                try {
                    isSessionSaving.value = true
                    repository.update(authService.email, character.value)
                } catch (e: Exception) {
                    isErr.value = true
                    error.value = e.message ?: "Could not save spell slots"
                } finally {
                    isSessionSaving.value = false
                }
            }
        }
    }

    private fun sanitiseSpellbook(value: CharacterModel): CharacterModel {
        val known = (value.knownSpellIds + value.subclassSpellIds).distinct()
        val prepared = (value.preparedSpellIds + value.subclassSpellIds)
            .distinct()
            .filter { it in known }
            .filter { compendiumService.getSpellById(it)?.isCantrip == false }
        return value.copy(
            knownSpellIds = known,
            preparedSpellIds = prepared
        )
    }
}
