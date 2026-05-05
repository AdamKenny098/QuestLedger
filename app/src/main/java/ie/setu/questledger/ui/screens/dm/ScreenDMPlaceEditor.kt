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
import ie.setu.questledger.models.dm.DMPlaceModel

@Composable
fun ScreenDMPlaceEditor(
    onDone: () -> Unit,
    vm: DMEntityEditorViewModel = hiltViewModel()
) {
    val existing by vm.place

    var name by remember(existing.id) { mutableStateOf(existing.name) }
    var region by remember(existing.id) { mutableStateOf(existing.region) }
    var description by remember(existing.id) { mutableStateOf(existing.description) }
    var mapCoordinates by remember(existing.id) { mutableStateOf(existing.mapCoordinates) }
    var localError by remember { mutableStateOf<String?>(null) }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                if (existing.id.isBlank()) "New Place" else "Edit Place",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(region, { region = it }, label = { Text("Region") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(mapCoordinates, { mapCoordinates = it }, label = { Text("Map Coordinates") }, modifier = Modifier.fillMaxWidth())

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
                        vm.savePlace(
                            DMPlaceModel(
                                id = existing.id,
                                name = name.trim(),
                                region = region.trim(),
                                description = description.trim(),
                                mapCoordinates = mapCoordinates.trim()
                            ),
                            onDone
                        )
                    }
                },
                enabled = !vm.isLoading.value,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Place")
            }
        }
    }
}