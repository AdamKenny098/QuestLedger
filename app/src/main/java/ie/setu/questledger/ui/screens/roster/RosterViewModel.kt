package ie.setu.questledger.ui.screens.roster

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ie.setu.questledger.data.local.CharacterEntity
import ie.setu.questledger.data.repository.CharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

enum class RosterSort {
    NAME_ASC,
    LEVEL_DESC
}
@HiltViewModel
class RosterViewModel @Inject constructor(
    private val repository: CharacterRepository
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    //current sort mode (default: name A-Z)
    private val _sort = MutableStateFlow(RosterSort.NAME_ASC)
    val sort: StateFlow<RosterSort> = _sort.asStateFlow()
    val uiCharacters: StateFlow<List<CharacterEntity>> =
        combine(repository.getAll(), query, sort) { list, q, s ->
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
    fun onQueryChange(newValue: String) {
        _query.value = newValue.trimStart()
    }

    fun onSortChange(newSort: RosterSort) {
        _sort.value = newSort
    }

    fun deleteCharacter(character: CharacterEntity) {
        viewModelScope.launch {
            repository.delete(character)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }
}
