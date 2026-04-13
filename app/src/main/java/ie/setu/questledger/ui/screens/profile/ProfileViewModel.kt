package ie.setu.questledger.ui.screens.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    val displayName get() = authService.displayName
    val email get() = authService.email
    val photoUri get() = authService.customPhotoUri

    fun signOut() {
        authService.signOut()
    }
}