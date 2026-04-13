package ie.setu.questledger.ui.screens.roster

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.data.firestore.FirestoreService
import ie.setu.questledger.models.CharacterModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RosterSort {
    NAME_ASC,
    LEVEL_DESC
}
@HiltViewModel
class RosterViewModel @Inject constructor(
    private val repository: FirestoreService,
    private val authService: AuthService
) : ViewModel() {

    private val _characters = MutableStateFlow<List<CharacterModel>>(emptyList())
    private val _query = MutableStateFlow("")

    val query: StateFlow<String> = _query.asStateFlow()

    //current sort mode (default: name A-Z)
    private val _sort = MutableStateFlow(RosterSort.NAME_ASC)
    val sort: StateFlow<RosterSort> = _sort.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    val uiCharacters: StateFlow<List<CharacterModel>> =
        combine(_characters, query, sort) { list, q, s ->
            val term = q.trim().lowercase()

            val filtered =
                if (term.isEmpty()) list
                else list.filter { c ->
                    c.name.lowercase().contains(term) ||
                            c.race.lowercase().contains(term) ||
                            c.characterClass.lowercase().contains(term)
                }

            when (s) {
                RosterSort.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
                RosterSort.LEVEL_DESC -> filtered.sortedByDescending { it.level }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    init {
        getCharacters()
    }

    fun getCharacters() {
        viewModelScope.launch {
            try {
                repository.getAll(authService.email).collect { items ->
                    _characters.value = items
                    _error.value = null
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load characters"
            }
        }
    }

    fun onQueryChange(newValue: String) {
        _query.value = newValue.trimStart()
    }

    fun onSortChange(newSort: RosterSort) {
        _sort.value = newSort
    }

    fun deleteCharacter(character: CharacterModel) {
        viewModelScope.launch {
            try {
                repository.delete(authService.email, character.id)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete character"
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            try {
                _characters.value.forEach { character ->
                    repository.delete(authService.email, character.id)
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete all characters"
            }
        }
    }
}
