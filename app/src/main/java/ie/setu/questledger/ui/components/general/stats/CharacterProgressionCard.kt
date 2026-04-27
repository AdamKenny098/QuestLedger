package ie.setu.questledger.ui.components.general.stats

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
import ie.setu.questledger.data.rules.CharacterStatEngine
import ie.setu.questledger.models.characters.CharacterModel

@Composable
fun CharacterProgressionCard(
    character: CharacterModel,
    modifier: Modifier = Modifier
) {
    val derived = CharacterStatEngine.build(character)

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Progression",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            Text("Hit Die: d${derived.hitDie}")
            Text("Proficiency Bonus: +${derived.proficiencyBonus}")

            Spacer(Modifier.height(8.dp))

            val slotsText = if (derived.spellSlotsByLevel.isEmpty()) {
                "None"
            } else {
                derived.spellSlotsByLevel.mapIndexed { index, count ->
                    "L${index + 1} x$count"
                }.joinToString(", ")
            }

            Text("Spell Slots: $slotsText")

            Spacer(Modifier.height(8.dp))

            Text("Unlocked Features:")
            if (derived.unlockedFeatures.isEmpty()) {
                Text("None")
            } else {
                derived.unlockedFeatures.forEach { feature ->
                    Text("• $feature")
                }
            }
        }
    }
}