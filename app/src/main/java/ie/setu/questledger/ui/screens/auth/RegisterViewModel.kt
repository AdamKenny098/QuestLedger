package ie.setu.questledger.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onDisplayNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(displayName = value, errorMessage = null)
    }

    fun onEmailChanged(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun register(onSuccess: () -> Unit) {
        val displayName = _uiState.value.displayName.trim()
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (displayName.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "All fields are required."
            )
            return
        }

        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Password must be at least 6 characters."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            runCatching {
                authService.signUp(email, password, displayName)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Registration failed."
                )
            }
        }
    }
}