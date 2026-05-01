package ie.setu.questledger.ui.components.general.spells

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.models.characters.CharacterModel

@Composable
fun CharacterSpellbookEditorCard(
    character: CharacterModel,
    compendiumService: CompendiumService,
    onToggleKnownSpell: (String) -> Unit,
    onTogglePreparedSpell: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tinySpellIds = listOf(
        "fire_bolt",
        "magic_missile",
        "shield",
        "sacred_flame",
        "cure_wounds",
        "bless"
    )

    val availableSpells = tinySpellIds.mapNotNull { compendiumService.getSpellById(it) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Edit Spellbook",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            Text("Toggle Known Spells", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            availableSpells.forEach { spell ->
                val known = character.knownSpellIds.contains(spell.id)
                val prepared = character.preparedSpellIds.contains(spell.id)

                Button(
                    onClick = { onToggleKnownSpell(spell.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (known) "Remove Known: ${spell.name}" else "Add Known: ${spell.name}")
                }

                Spacer(Modifier.height(6.dp))

                OutlinedButton(
                    onClick = { onTogglePreparedSpell(spell.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (prepared) "Unprepare: ${spell.name}" else "Prepare: ${spell.name}")
                }

                Spacer(Modifier.height(10.dp))
            }
        }
    }
}