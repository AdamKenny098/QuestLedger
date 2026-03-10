package ie.setu.questledger.data.di

import android.content.Context
import androidx.room.Room
import ie.setu.questledger.data.local.AppDatabase
import ie.setu.questledger.data.local.CharacterDao
import ie.setu.questledger.data.repository.CharacterRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "questledger_db").build()

    @Provides
    fun provideCharacterDao(db: AppDatabase): CharacterDao = db.characterDao()

    @Provides
    @Singleton
    fun provideCharacterRepository(dao: CharacterDao): CharacterRepository =
        CharacterRepository(dao)
}
