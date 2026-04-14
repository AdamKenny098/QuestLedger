package ie.setu.questledger.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ie.setu.questledger.data.api.CharacterApiService
import ie.setu.questledger.data.local.AppDatabase
import ie.setu.questledger.data.local.CharacterDao
import ie.setu.questledger.data.repository.CharacterRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import com.google.firebase.auth.FirebaseAuth
import ie.setu.questledger.data.auth.AuthRepository
import ie.setu.questledger.data.auth.AuthService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import ie.setu.questledger.data.firestore.FirestoreRepository
import ie.setu.questledger.data.firestore.FirestoreService
import ie.setu.questledger.data.storage.StorageRepository
import ie.setu.questledger.data.storage.StorageService

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "http://10.0.2.2:3000/"
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "questledger_db"
        ).build()

    @Provides
    @Singleton
    fun provideCharacterDao(
        database: AppDatabase
    ): CharacterDao = database.characterDao()

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideCharacterApiService(
        retrofit: Retrofit
    ): CharacterApiService =
        retrofit.create(CharacterApiService::class.java)

    @Provides
    @Singleton
    fun provideCharacterRepository(
        dao: CharacterDao,
        apiService: CharacterApiService,
        storageService: StorageService
    ): CharacterRepository = CharacterRepository(
        dao = dao,
        apiService = apiService,
        storageService = storageService
    )

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAuthService(
        authRepository: AuthRepository
    ): AuthService = authRepository

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirestoreService(
        auth: AuthService,
        firebaseFirestore: FirebaseFirestore
    ): FirestoreService = FirestoreRepository(
        firestore = firebaseFirestore
    )

    @Provides
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    fun provideStorageRepository(
        firebaseStorage: FirebaseStorage
    ): StorageService = StorageRepository(
        storage = firebaseStorage
    )
}