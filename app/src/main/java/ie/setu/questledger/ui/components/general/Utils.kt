package ie.setu.questledger.ui.components.general

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ShowError(
    headline: String,
    subtitle: String,
    onClick: (() -> Unit)
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge
            )

            Button(onClick = onClick) {
                Text("Retry")
            }
        }
    }
}