package ie.setu.questledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import ie.setu.questledger.data.auth.AuthService
import ie.setu.questledger.ui.navigation.QuestLedgerNavHost
import ie.setu.questledger.ui.theme.QuestLedgerTheme
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuestLedgerTheme {
                QuestLedgerNavHost(authService = authService)
            }
        }
    }
}