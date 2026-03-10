package ie.setu.questledger.main

import android.app.Application
import timber.log.Timber
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QuestLedgerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.i("Starting QuestLedger Application")
    }
}