package ie.setu.questledger.data.auth

import android.net.Uri
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthService {

    override val currentUserId: String
        get() = firebaseAuth.currentUser?.uid.orEmpty()

    override val hasUser: Boolean
        get() = firebaseAuth.currentUser != null

    override val displayName: String
        get() = firebaseAuth.currentUser?.displayName ?: ""

    override val email: String
        get() = firebaseAuth.currentUser?.email ?: ""

    override val customPhotoUri: Uri?
        get() = firebaseAuth.currentUser?.photoUrl

    override suspend fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun signUp(email: String, password: String, displayName: String) {
        val uri = Uri.parse("android.resource://ie.setu.questledger/drawable/default_profile")
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .setPhotoUri(uri)
            .build()

        result.user?.updateProfile(profileUpdates)?.await()
    }

    override suspend fun signInWithGoogle(credential: AuthCredential) {
        firebaseAuth.signInWithCredential(credential).await()
    }

    override suspend fun updatePhoto(uri: Uri) {
        firebaseAuth.currentUser!!.updateProfile(
            UserProfileChangeRequest
                .Builder()
                .setPhotoUri(uri)
                .build()
        ).await()
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}