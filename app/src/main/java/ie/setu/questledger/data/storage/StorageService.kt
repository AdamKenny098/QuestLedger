package ie.setu.questledger.data.storage

import android.net.Uri

interface StorageService {
    suspend fun uploadFile(uri: Uri, directory: String): Uri
}