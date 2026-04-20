package ie.setu.questledger.ui.screens.premade

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import ie.setu.questledger.models.PremadeCharacterTemplate
import ie.setu.questledger.ui.components.general.CharacterDerivedStatsCard

@Composable
fun ScreenPremadeCharacters(
    onDone: () -> Unit = {},
    onOpenManualSetup: () -> Unit = {},
    vm: PremadeCharactersViewModel = hiltViewModel()
) {
    val templates = remember { vm.getTemplates() }

    var selectedTemplateId by remember { mutableStateOf(templates.firstOrNull()?.id.orEmpty()) }
    var characterName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val selectedTemplate = remember(selectedTemplateId) {
        templates.firstOrNull { it.id == selectedTemplateId }
    }

    val preview = remember(selectedTemplateId, characterName) {
        vm.buildPreview(selectedTemplateId, characterName)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Premade Characters", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            Text(
                "Pick a ready-made archetype and save it instantly. This is the fastest way to get started.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onOpenManualSetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use Manual Setup Instead")
            }

            Spacer(Modifier.height(16.dp))

            Text("Choose a Template", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))

            templates.forEach { template ->
                PremadeTemplateCard(
                    template = template,
                    isSelected = template.id == selectedTemplateId,
                    onClick = { selectedTemplateId = template.id }
                )
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = characterName,
                onValueChange = { characterName = it },
                label = { Text("Character Name (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            selectedTemplate?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Leave blank to use default name: ${it.defaultName}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(16.dp))

            preview?.let { result ->
                Text("Preview", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))

                CharacterDerivedStatsCard(character = result.character)

                Spacer(Modifier.height(12.dp))

                Text("Starter Loadout", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                result.summaryLines.forEach { line ->
                    Text(line, style = MaterialTheme.typography.bodyMedium)
                }
            }

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
                    when {
                        selectedTemplateId.isBlank() -> error = "Choose a template"
                        else -> {
                            error = null
                            vm.savePremade(selectedTemplateId, characterName) {
                                onDone()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Premade Character")
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PremadeTemplateCard(
    template: PremadeCharacterTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        tonalElevation = if (isSelected) 4.dp else 1.dp,
        color = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        },
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Lv ${template.level}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = template.subtitle,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = template.summary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}