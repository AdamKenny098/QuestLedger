package ie.setu.questledger.ui.components.general.spells

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.data.rules.CharacterStatEngine
import ie.setu.questledger.models.characters.CharacterModel

@Composable
fun CharacterSpellbookCard(
    character: CharacterModel,
    compendiumService: CompendiumService,
    modifier: Modifier = Modifier
) {
    val derived = CharacterStatEngine.build(character)

    val knownSpells = character.knownSpellIds.mapNotNull { compendiumService.getSpellById(it) }
    val preparedSpells = character.preparedSpellIds.mapNotNull { compendiumService.getSpellById(it) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Spellbook",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            Text("Spellcasting Ability: ${derived.spellcastingAbilityLabel ?: "None"}")
            Text("Spell Attack Bonus: ${if (derived.spellAttackBonus >= 0) "+${derived.spellAttackBonus}" else derived.spellAttackBonus}")
            Text("Spell Save DC: ${derived.spellSaveDc}")

            Spacer(Modifier.height(8.dp))

            val slotsText = if (derived.spellSlotsByLevel.isEmpty()) {
                "None"
            } else {
                derived.spellSlotsByLevel.mapIndexed { index, count ->
                    "L${index + 1} x$count"
                }.joinToString(", ")
            }

            Text("Spell Slots: $slotsText")

            Spacer(Modifier.height(12.dp))

            Text("Known Spells", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
            if (knownSpells.isEmpty()) {
                Text("None")
            } else {
                knownSpells.forEach { spell ->
                    Text("• ${spell.name}")
                }
            }

            Spacer(Modifier.height(12.dp))

            Text("Prepared Spells", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
            if (preparedSpells.isEmpty()) {
                Text("None")
            } else {
                preparedSpells.forEach { spell ->
                    Text("• ${spell.name}")
                }
            }
        }
    }
}