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
import ie.setu.questledger.models.dm.DMNpcModel

@Composable
fun ScreenDMNpcEditor(
    onDone: () -> Unit,
    vm: DMEntityEditorViewModel = hiltViewModel()
) {
    val existing by vm.npc

    var name by remember(existing.id) { mutableStateOf(existing.name) }
    var role by remember(existing.id) { mutableStateOf(existing.role) }
    var faction by remember(existing.id) { mutableStateOf(existing.faction) }
    var notes by remember(existing.id) { mutableStateOf(existing.notes) }
    var statSummary by remember(existing.id) { mutableStateOf(existing.statSummary) }
    var localError by remember { mutableStateOf<String?>(null) }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                if (existing.id.isBlank()) "New NPC" else "Edit NPC",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(role, { role = it }, label = { Text("Role") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(faction, { faction = it }, label = { Text("Faction") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(statSummary, { statSummary = it }, label = { Text("Stats if needed") }, modifier = Modifier.fillMaxWidth())

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
                    if (name.isBlank()) {
                        localError = "Name is required"
                    } else {
                        localError = null
                        vm.saveNpc(
                            DMNpcModel(
                                id = existing.id,
                                name = name.trim(),
                                role = role.trim(),
                                faction = faction.trim(),
                                notes = notes.trim(),
                                statSummary = statSummary.trim()
                            ),
                            onDone
                        )
                    }
                },
                enabled = !vm.isLoading.value,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save NPC")
            }
        }
    }
}