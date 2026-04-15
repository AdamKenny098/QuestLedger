package ie.setu.questledger.ui.screens.details

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import ie.setu.questledger.models.CharacterModel
import ie.setu.questledger.ui.components.general.AbilityScoreField
import ie.setu.questledger.ui.components.general.CharacterDerivedStatsCard
import ie.setu.questledger.ui.components.general.ShowPhotoPicker

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
    var selectedImageUri by remember(c.id) { mutableStateOf<Uri?>(null) }

    var strengthText by remember(c.id) { mutableStateOf(c.strength.toString()) }
    var dexterityText by remember(c.id) { mutableStateOf(c.dexterity.toString()) }
    var constitutionText by remember(c.id) { mutableStateOf(c.constitution.toString()) }
    var intelligenceText by remember(c.id) { mutableStateOf(c.intelligence.toString()) }
    var wisdomText by remember(c.id) { mutableStateOf(c.wisdom.toString()) }
    var charismaText by remember(c.id) { mutableStateOf(c.charisma.toString()) }

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
        selectedImageUri = null
    }

    val previewCharacter = CharacterModel(
        id = c.id,
        email = c.email,
        name = name,
        characterClass = characterClass,
        race = race,
        level = levelText.toIntOrNull() ?: 1,
        notes = text,
        imageUri = c.imageUri,
        strength = strengthText.toIntOrNull() ?: 10,
        dexterity = dexterityText.toIntOrNull() ?: 10,
        constitution = constitutionText.toIntOrNull() ?: 10,
        intelligence = intelligenceText.toIntOrNull() ?: 10,
        wisdom = wisdomText.toIntOrNull() ?: 10,
        charisma = charismaText.toIntOrNull() ?: 10,
        currentHp = c.currentHp,
        armourBonus = c.armourBonus,
        shieldBonus = c.shieldBonus
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Character Details", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            if (selectedImageUri != null || c.imageUri.isNotBlank()) {
                AsyncImage(
                    model = selectedImageUri ?: c.imageUri,
                    contentDescription = "Character Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(Modifier.height(12.dp))
            }

            ShowPhotoPicker(
                onPhotoUriChanged = { selectedImageUri = it }
            )

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
                onValueChange = { levelText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Level (1–20)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Text("Ability Scores", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))

            AbilityScoreField("Strength", strengthText, { strengthText = it })
            Spacer(Modifier.height(8.dp))

            AbilityScoreField("Dexterity", dexterityText, { dexterityText = it })
            Spacer(Modifier.height(8.dp))

            AbilityScoreField("Constitution", constitutionText, { constitutionText = it })
            Spacer(Modifier.height(8.dp))

            AbilityScoreField("Intelligence", intelligenceText, { intelligenceText = it })
            Spacer(Modifier.height(8.dp))

            AbilityScoreField("Wisdom", wisdomText, { wisdomText = it })
            Spacer(Modifier.height(8.dp))

            AbilityScoreField("Charisma", charismaText, { charismaText = it })

            Spacer(Modifier.height(16.dp))

            CharacterDerivedStatsCard(character = previewCharacter)

            Spacer(Modifier.height(12.dp))

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
                    val str = strengthText.toIntOrNull()
                    val dex = dexterityText.toIntOrNull()
                    val con = constitutionText.toIntOrNull()
                    val intScore = intelligenceText.toIntOrNull()
                    val wis = wisdomText.toIntOrNull()
                    val cha = charismaText.toIntOrNull()

                    when {
                        name.isBlank() -> error = "Name is required"
                        characterClass.isBlank() -> error = "Class is required"
                        race.isBlank() -> error = "Race is required"
                        level == null || level !in 1..20 -> error = "Level must be 1–20"
                        listOf(str, dex, con, intScore, wis, cha).any { it == null || it !in 1..20 } ->
                            error = "All ability scores must be between 1 and 20"
                        else -> {
                            error = null
                            vm.updateCharacter(
                                name = name.trim(),
                                characterClass = characterClass.trim(),
                                race = race.trim(),
                                level = level,
                                notes = trimmedNotes,
                                strength = str!!,
                                dexterity = dex!!,
                                constitution = con!!,
                                intelligence = intScore!!,
                                wisdom = wis!!,
                                charisma = cha!!,
                                imageUri = selectedImageUri,
                                onSuccess = onDone
                            )
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