package ie.setu.questledger.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ScreenProfile(
    onSignOut: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Name: ${profileViewModel.displayName}")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Email: ${profileViewModel.email}")
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                profileViewModel.signOut()
                onSignOut()
            }
        ) {
            Text("Logout")
        }
    }
}