package ie.setu.questledger.data.auth

import android.net.Uri

interface AuthService {
    val currentUserId: String
    val hasUser: Boolean
    val displayName: String
    val email: String
    val customPhotoUri: Uri?

    suspend fun signIn(email: String, password: String)
    suspend fun signUp(email: String, password: String, displayName: String)
    fun signOut()
}