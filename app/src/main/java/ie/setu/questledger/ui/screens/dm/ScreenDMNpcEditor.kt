package ie.setu.questledger.ui.screens.dm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
    vm: DMWorkspaceViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var faction by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var statSummary by remember { mutableStateOf("") }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("New NPC", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = role,
                onValueChange = { role = it },
                label = { Text("Role") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = faction,
                onValueChange = { faction = it },
                label = { Text("Faction") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = statSummary,
                onValueChange = { statSummary = it },
                label = { Text("Stats if needed") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    vm.saveNpc(
                        DMNpcModel(
                            name = name.trim(),
                            role = role.trim(),
                            faction = faction.trim(),
                            notes = notes.trim(),
                            statSummary = statSummary.trim()
                        )
                    )
                    onDone()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save NPC")
            }
        }
    }
}