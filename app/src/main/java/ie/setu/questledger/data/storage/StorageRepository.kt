package ie.setu.questledger.data.storage

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class StorageRepository @Inject constructor(
    private val storage: FirebaseStorage
) : StorageService {

    override suspend fun uploadFile(uri: Uri, directory: String): Uri {
        val storageRef = storage.reference
        val imageRef = storageRef.child("$directory/${uri.lastPathSegment}")

        imageRef.putFile(uri).await()
        return imageRef.downloadUrl.await()
    }
}