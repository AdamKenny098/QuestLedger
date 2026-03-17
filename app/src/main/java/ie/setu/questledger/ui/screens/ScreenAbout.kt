package ie.setu.questledger.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ScreenAbout(
) {
    val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "No user signed in"
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("QuestLedger", style = MaterialTheme.typography.titleLarge)
            Text("A D&D character and campaign assistant app.")
            Text("Signed in as: $userEmail")
        }
    }
}