package ie.setu.questledger.ui.components.general

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import ie.setu.questledger.models.CharacterModel


@Composable
fun CharacterDerivedStatsCard(
    character: CharacterModel,
    modifier: Modifier = Modifier
) {
    val derived = CharacterStatEngine.build(character)

    fun fmt(value: Int): String = if (value >= 0) "+$value" else "$value"

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Derived Stats",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("STR ${derived.strengthScore} (${fmt(derived.strMod)})")
                Text("DEX ${derived.dexterityScore} (${fmt(derived.dexMod)})")
                Text("CON ${derived.constitutionScore} (${fmt(derived.conMod)})")
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("INT ${derived.intelligenceScore} (${fmt(derived.intMod)})")
                Text("WIS ${derived.wisdomScore} (${fmt(derived.wisMod)})")
                Text("CHA ${derived.charismaScore} (${fmt(derived.chaMod)})")
            }

            Spacer(Modifier.height(12.dp))

            Text("HP Max: ${derived.maxHp}")
            Text("AC: ${derived.armourClass}")
            Text("Prof Bonus: ${fmt(derived.proficiencyBonus)}")
            Text("Initiative: ${fmt(derived.initiativeBonus)}")
            Text("Speed: ${derived.speed}")
            Text("Carry Capacity: ${derived.carryCapacity}")
            Text("Hit Die: d${derived.hitDie}")

            if (derived.spellAttackBonus != 0 || derived.spellSaveDc != 0) {
                Spacer(Modifier.height(8.dp))
                Text("Spell Attack: ${fmt(derived.spellAttackBonus)}")
                Text("Spell Save DC: ${derived.spellSaveDc}")
            }
        }
    }
}