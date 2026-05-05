package ie.setu.questledger.ui.screens.dm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ie.setu.questledger.models.dm.DMCampaignModel

@Composable
fun ScreenDMCampaignEditor(
    onDone: () -> Unit,
    vm: DMEntityEditorViewModel = hiltViewModel()
) {
    val existing by vm.campaign

    var title by remember(existing.id) { mutableStateOf(existing.title) }
    var setting by remember(existing.id) { mutableStateOf(existing.setting) }
    var summary by remember(existing.id) { mutableStateOf(existing.summary) }
    var sessionCountText by remember(existing.id) { mutableStateOf(existing.sessionCount.toString()) }
    var localError by remember { mutableStateOf<String?>(null) }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                if (existing.id.isBlank()) "New Campaign" else "Edit Campaign",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(setting, { setting = it }, label = { Text("Setting") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(summary, { summary = it }, label = { Text("Summary") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = sessionCountText,
                onValueChange = { sessionCountText = it.filter(Char::isDigit) },
                label = { Text("Session Count") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            if (vm.isLoading.value) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
            }

            localError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            if (vm.isErr.value && vm.error.value.isNotBlank()) {
                Text(vm.error.value, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (title.isBlank()) {
                        localError = "Title is required"
                    } else {
                        localError = null
                        vm.saveCampaign(
                            DMCampaignModel(
                                id = existing.id,
                                title = title.trim(),
                                setting = setting.trim(),
                                summary = summary.trim(),
                                sessionCount = sessionCountText.toIntOrNull() ?: 0
                            ),
                            onDone
                        )
                    }
                },
                enabled = !vm.isLoading.value,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Campaign")
            }
        }
    }
}