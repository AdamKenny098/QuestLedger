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
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

enum class RosterSort {
    NAME_ASC,
    LEVEL_DESC
}
@HiltViewModel
class RosterViewModel @Inject constructor(
    private val repository: CharacterRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val email: String
        get() = auth.currentUser?.email ?: "uh.theo.uh@gmail.com"

    val characters = repository.getAll()
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    //current sort mode (default: name A-Z)
    private val _sort = MutableStateFlow(RosterSort.NAME_ASC)
    val sort: StateFlow<RosterSort> = _sort.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
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

    fun loadCharactersFromApi() {

        viewModelScope.launch {
            try {
                Log.d("ROSTER_DEBUG", "Loading for email: $email")
                repository.fetchCharactersFromApi(email)
                _error.value = null
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

    fun deleteCharacter(character: CharacterEntity) {
        viewModelScope.launch {
            try {
                repository.deleteFromApi(character)
                repository.delete(character)
                repository.fetchCharactersFromApi(email)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete character"
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }
}
