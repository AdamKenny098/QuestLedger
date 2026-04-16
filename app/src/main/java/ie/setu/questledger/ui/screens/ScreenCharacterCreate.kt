package ie.setu.questledger.ui.screens

import android.net.Uri
import android.widget.NumberPicker
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import ie.setu.questledger.data.compendium.CompendiumLookup
import ie.setu.questledger.models.CharacterModel
import ie.setu.questledger.ui.components.general.AbilityScoreField
import ie.setu.questledger.ui.components.general.CharacterDerivedStatsCard
import ie.setu.questledger.ui.components.general.CompendiumDropdown
import ie.setu.questledger.ui.components.general.CompendiumOption
import ie.setu.questledger.ui.components.general.ShowPhotoPicker
import ie.setu.questledger.ui.screens.create.CreateViewModel
@Composable
fun ScreenCharacterCreate() {
    val vm: CreateViewModel = hiltViewModel()

    val races = remember { vm.getRaces() }
    val classes = remember { vm.getClasses() }

    var name by remember { mutableStateOf("") }
    var selectedClassId by remember { mutableStateOf(classes.firstOrNull()?.id.orEmpty()) }
    var selectedRaceId by remember { mutableStateOf(races.firstOrNull()?.id.orEmpty()) }
    var level by remember { mutableStateOf(1) }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var strengthText by remember { mutableStateOf("10") }
    var dexterityText by remember { mutableStateOf("10") }
    var constitutionText by remember { mutableStateOf("10") }
    var intelligenceText by remember { mutableStateOf("10") }
    var wisdomText by remember { mutableStateOf("10") }
    var charismaText by remember { mutableStateOf("10") }

    val selectedRace = remember(selectedRaceId) {
        races.firstOrNull { it.id == selectedRaceId }
    }

    val selectedClass = remember(selectedClassId) {
        classes.firstOrNull { it.id == selectedClassId }
    }


    val previewCharacter = CharacterModel(
        name = name,
        characterClass = selectedClassId,
        race = selectedRaceId,
        level = level,
        notes = notes,
        strength = strengthText.toIntOrNull() ?: 10,
        dexterity = dexterityText.toIntOrNull() ?: 10,
        constitution = constitutionText.toIntOrNull() ?: 10,
        intelligence = intelligenceText.toIntOrNull() ?: 10,
        wisdom = wisdomText.toIntOrNull() ?: 10,
        charisma = charismaText.toIntOrNull() ?: 10
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

            Text("Create Character", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            CompendiumDropdown(
                label = "Class",
                options = classes.map { CompendiumOption(it.id, it.name) },
                selectedId = selectedClassId,
                onSelected = { selectedClassId = it }
            )

            selectedClass?.let { classDef ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Hit Die: d${classDef.hitDie} • Primary Stats: ${CompendiumLookup.formatAbilityList(classDef.primaryStats)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Spellcasting: ${classDef.spellcastingAbility?.let { CompendiumLookup.abilityLabel(it) } ?: "None"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(10.dp))

            CompendiumDropdown(
                label = "Race",
                options = classes.map { CompendiumOption(it.id, it.name) },
                selectedId = selectedRaceId,
                onSelected = { selectedRaceId = it }
            )

            selectedRace?.let { raceDef ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = raceDef.description,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Bonuses: ${CompendiumLookup.formatStatBonuses(raceDef.statBonuses)} • Speed: ${raceDef.speed}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(10.dp))

            Text("Level (1–20)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    factory = { context ->
                        NumberPicker(context).apply {
                            minValue = 1
                            maxValue = 20
                            value = level
                            wrapSelectorWheel = true
                            setOnValueChangedListener { _, _, newVal ->
                                level = newVal
                            }
                        }
                    },
                    update = { picker ->
                        if (picker.value != level) picker.value = level
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            selectedImageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Selected Character Image",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }

            ShowPhotoPicker(
                onPhotoUriChanged = { selectedImageUri = it }
            )

            Spacer(Modifier.height(16.dp))

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

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    val str = strengthText.toIntOrNull()
                    val dex = dexterityText.toIntOrNull()
                    val con = constitutionText.toIntOrNull()
                    val intScore = intelligenceText.toIntOrNull()
                    val wis = wisdomText.toIntOrNull()
                    val cha = charismaText.toIntOrNull()

                    when {
                        name.isBlank() -> error = "Name is required"
                        selectedClassId.isBlank() -> error = "Class is required"
                        selectedRaceId.isBlank() -> error = "Race is required"
                        level !in 1..20 -> error = "Level must be between 1 and 20"
                        listOf(str, dex, con, intScore, wis, cha).any { it == null || it !in 1..20 } ->
                            error = "All ability scores must be between 1 and 20"
                        else -> {
                            error = null
                            vm.addCharacter(
                                name = name.trim(),
                                characterClass = selectedClassId,
                                race = selectedRaceId,
                                level = level,
                                notes = notes.trim(),
                                imageUri = selectedImageUri,
                                strength = str!!,
                                dexterity = dex!!,
                                constitution = con!!,
                                intelligence = intScore!!,
                                wisdom = wis!!,
                                charisma = cha!!
                            )

                            name = ""
                            selectedClassId = ""
                            selectedRaceId = ""
                            level = 1
                            notes = ""
                            selectedImageUri = null
                            strengthText = "10"
                            dexterityText = "10"
                            constitutionText = "10"
                            intelligenceText = "10"
                            wisdomText = "10"
                            charismaText = "10"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}