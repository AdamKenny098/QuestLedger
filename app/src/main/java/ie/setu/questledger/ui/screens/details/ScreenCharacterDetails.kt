package ie.setu.questledger.ui.screens.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ScreenCharacterDetails(
    onDone: () -> Unit
) {
    val vm: CharacterDetailsViewModel = hiltViewModel()
    val c = vm.character.value

    var name by remember(c.id) { mutableStateOf(c.name) }
    var characterClass by remember(c.id) { mutableStateOf(c.characterClass) }
    var race by remember(c.id) { mutableStateOf(c.race) }
    var levelText by remember(c.id) { mutableStateOf(c.level.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    val errorEmptyNotes = "Notes cannot be empty..."
    val errorShortNotes = "Notes must be at least 2 characters"

    var text by rememberSaveable { mutableStateOf("") }
    var onNotesChanged by rememberSaveable { mutableStateOf(false) }
    var isEmptyError by rememberSaveable { mutableStateOf(false) }
    var isShortError by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(c.id) {
        text = c.notes
        onNotesChanged = false
        isEmptyError = false
        isShortError = false
    }



    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Character Details", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = characterClass,
                onValueChange = { characterClass = it },
                label = { Text("Class") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = race,
                onValueChange = { race = it },
                label = { Text("Race") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = levelText,
                onValueChange = { levelText = it },
                label = { Text("Level (1–20)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onNotesChanged = true

                    val trimmed = it.trim()
                    isEmptyError = trimmed.isEmpty()
                    isShortError = trimmed.isNotEmpty() && trimmed.length < 2
                },
                label = { Text("Notes") },
                isError = isEmptyError || isShortError,
                modifier = Modifier.fillMaxWidth()
            )

            if (isEmptyError) {
                Text(errorEmptyNotes, color = MaterialTheme.colorScheme.error)
            } else if (isShortError) {
                Text(errorShortNotes, color = MaterialTheme.colorScheme.error)
            }


            Spacer(Modifier.height(12.dp))

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    val trimmedNotes = text.trim()
                    isEmptyError = trimmedNotes.isEmpty()
                    isShortError = trimmedNotes.isNotEmpty() && trimmedNotes.length < 2
                    if (isEmptyError || isShortError) return@Button

                    val level = levelText.toIntOrNull()
                    when {
                        name.isBlank() -> error = "Name is required"
                        characterClass.isBlank() -> error = "Class is required"
                        race.isBlank() -> error = "Race is required"
                        level == null || level !in 1..20 -> error = "Level must be 1–20"
                        else -> {
                            error = null
                            vm.updateCharacter(
                                c.copy(
                                    name = name.trim(),
                                    characterClass = characterClass.trim(),
                                    race = race.trim(),
                                    level = level,
                                    notes = trimmedNotes
                                )
                            )
                            onDone()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}