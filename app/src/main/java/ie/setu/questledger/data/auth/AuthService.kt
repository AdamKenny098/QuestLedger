package ie.setu.questledger.data.auth

import android.net.Uri
import com.google.firebase.Firebase

interface AuthService {
    val currentUserId: String
    val hasUser: Boolean
    val displayName: String
    val email: String
    val customPhotoUri: Uri?

    suspend fun signIn(email: String, password: String)
    suspend fun signUp(email: String, password: String, displayName: String)

    suspend fun updatePhoto(uri: Uri)
    fun signOut()
}