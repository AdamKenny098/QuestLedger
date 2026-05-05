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
import ie.setu.questledger.models.dm.DMQuestModel

@Composable
fun ScreenDMQuestEditor(
    onDone: () -> Unit,
    vm: DMEntityEditorViewModel = hiltViewModel()
) {
    val existing by vm.quest

    var title by remember(existing.id) { mutableStateOf(existing.title) }
    var summary by remember(existing.id) { mutableStateOf(existing.summary) }
    var status by remember(existing.id) { mutableStateOf(existing.status) }
    var localError by remember { mutableStateOf<String?>(null) }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                if (existing.id.isBlank()) "New Quest" else "Edit Quest",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(summary, { summary = it }, label = { Text("Summary") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(status, { status = it }, label = { Text("Status") }, modifier = Modifier.fillMaxWidth())

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
                        vm.saveQuest(
                            DMQuestModel(
                                id = existing.id,
                                title = title.trim(),
                                summary = summary.trim(),
                                status = status.trim()
                            ),
                            onDone
                        )
                    }
                },
                enabled = !vm.isLoading.value,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Quest")
            }
        }
    }
}