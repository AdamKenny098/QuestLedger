package ie.setu.questledger.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ie.setu.questledger.data.auth.AuthService
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    val displayName: String
        get() = authService.displayName

    val email: String
        get() = authService.email

    val photoUri: Uri?
        get() = authService.customPhotoUri

    fun updatePhotoUri(uri: Uri) {
        viewModelScope.launch {
            authService.updatePhoto(uri)
        }
    }

    fun signOut() {
        authService.signOut()
    }
}