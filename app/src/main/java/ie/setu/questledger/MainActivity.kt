package ie.setu.questledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ie.setu.questledger.ui.navigation.QuestLedgerNavHost
import dagger.hilt.android.AndroidEntryPoint
import ie.setu.questledger.ui.theme.QuestLedgerTheme


@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuestLedgerTheme {
                QuestLedgerNavHost()
            }
        }
    }
}