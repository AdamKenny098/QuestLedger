package ie.setu.questledger.ui.screens.spellbook

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.setu.questledger.ui.components.general.spells.CharacterSpellbookCard
import ie.setu.questledger.ui.components.general.spells.CharacterSpellbookEditorCard

@Composable
fun ScreenCharacterSpellbook(
    onDone: () -> Unit,
    vm: CharacterSpellbookViewModel = hiltViewModel()
) {
    val character by vm.character
    val isLoading by vm.isLoading
    val isErr by vm.isErr
    val error by vm.error

    val compendiumService = vm.getCompendiumService()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                CircularProgressIndicator()
            }
            return@Surface
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Spellbook",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "${character.name} • ${character.characterClass}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            if (!vm.isSpellcaster()) {
                Text("This character is not a spellcaster.")
                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back")
                }

                return@Column
            }

            CharacterSpellbookCard(
                character = character,
                compendiumService = compendiumService
            )

            Spacer(Modifier.height(12.dp))

            CharacterSpellbookEditorCard(
                character = character,
                compendiumService = compendiumService,
                onToggleKnownSpell = vm::toggleKnownSpell,
                onTogglePreparedSpell = vm::togglePreparedSpell
            )

            Spacer(Modifier.height(12.dp))

            if (isErr && error.isNotBlank()) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = { vm.saveSpellbook(onDone) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Spellbook")
            }
        }
    }
}