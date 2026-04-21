package ie.setu.questledger.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ie.setu.questledger.R
@Composable
fun ScreenCharacterCreate(
    onOpenQuickSetup: () -> Unit = {},
    onOpenPremade: () -> Unit = {},
    onOpenFullSetup: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Text(
                text="Create Character",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth()
                )

            Spacer(Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.default_profile),
                contentDescription = "Add Character",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = onOpenQuickSetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use Quick Setup")
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onOpenPremade,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use Premade Character")
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onOpenFullSetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use Full Setup")
            }
        }
    }
}