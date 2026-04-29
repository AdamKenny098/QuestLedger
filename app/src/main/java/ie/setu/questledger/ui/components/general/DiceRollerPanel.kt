package ie.setu.questledger.ui.components.general

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.setu.questledger.data.rules.DiceRollEngine
import ie.setu.questledger.models.characters.CharacterModel
import ie.setu.questledger.models.dice.DiceRollRequest
import ie.setu.questledger.models.dice.DiceRollResult
import ie.setu.questledger.models.dice.DiceRollType
import ie.setu.questledger.models.dice.DiceType

@Composable
fun DiceRollerPanel(
    character: CharacterModel,
    modifier: Modifier = Modifier
) {
    var selectedRollType by remember { mutableStateOf(DiceRollType.GENERIC) }
    var selectedDiceType by remember { mutableStateOf(DiceType.D20) }
    var modifierText by remember { mutableStateOf("0") }
    var labelText by remember { mutableStateOf("") }
    var advantage by remember { mutableStateOf(false) }
    var disadvantage by remember { mutableStateOf(false) }

    val history = remember { mutableStateListOf<DiceRollResult>() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Dice Roller",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            Text("Roll Type", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            DiceRollType.entries.forEach { rollType ->
                OutlinedButton(
                    onClick = { selectedRollType = rollType },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (selectedRollType == rollType) "✓ ${rollType.label}" else rollType.label
                    )
                }
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(8.dp))

            Text("Die Type", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            DiceType.entries.forEach { dice ->
                OutlinedButton(
                    onClick = { selectedDiceType = dice },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (selectedDiceType == dice) "✓ ${dice.label}" else dice.label
                    )
                }
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = modifierText,
                onValueChange = { modifierText = sanitizeSignedInt(it) },
                label = { Text("Flat Modifier") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = labelText,
                onValueChange = { labelText = it },
                label = { Text("Label (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Checkbox(
                        checked = advantage,
                        onCheckedChange = {
                            advantage = it
                            if (it) disadvantage = false
                        }
                    )
                    Text("Advantage", modifier = Modifier.padding(top = 12.dp))
                }

                Row {
                    Checkbox(
                        checked = disadvantage,
                        onCheckedChange = {
                            disadvantage = it
                            if (it) advantage = false
                        }
                    )
                    Text("Disadvantage", modifier = Modifier.padding(top = 12.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val request = DiceRollRequest(
                        rollType = selectedRollType,
                        diceType = selectedDiceType,
                        modifier = modifierText.toIntOrNull() ?: 0,
                        advantage = advantage,
                        disadvantage = disadvantage,
                        label = labelText
                    )

                    val result = DiceRollEngine.roll(character, request)
                    history.add(0, result)

                    if (history.size > 12) {
                        history.removeAt(history.lastIndex)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Roll Dice")
            }

            Spacer(Modifier.height(16.dp))

            Text("History", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            if (history.isEmpty()) {
                Text("No rolls yet.")
            } else {
                history.forEach { result ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 1.dp,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(result.label, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            Text(result.detailText, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

private fun sanitizeSignedInt(input: String): String {
    if (input.isBlank()) return ""
    val filtered = buildString {
        input.forEachIndexed { index, c ->
            if (c.isDigit() || (c == '-' && index == 0)) append(c)
        }
    }
    return filtered
}