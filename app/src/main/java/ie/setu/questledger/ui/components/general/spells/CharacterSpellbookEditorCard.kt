package ie.setu.questledger.ui.components.general.spells

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.data.compendium.SpellLearningMode
import ie.setu.questledger.data.rules.SpellRules
import ie.setu.questledger.models.characters.CharacterModel

@Composable
fun CharacterSpellbookEditorCard(
    character: CharacterModel,
    compendiumService: CompendiumService,
    onToggleKnownSpell: (String) -> Unit,
    onTogglePreparedSpell: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val characterClass = compendiumService.getClassById(character.characterClass)
    val learningMode = characterClass?.spellLearningMode ?: SpellLearningMode.NONE
    val availableSpells = SpellRules.availableSpells(character, compendiumService)
    val maximumLevel = availableSpells.maxOfOrNull { it.level } ?: 0
    val preparedLimit = characterClass?.let {
        SpellRules.preparedSpellLimit(character, it)
    } ?: 0
    val preparedLevelledCount = character.preparedSpellIds.count { preparedId ->
        preparedId !in character.subclassSpellIds &&
            compendiumService.getSpellById(preparedId)?.isCantrip == false
    }

    var query by rememberSaveable { mutableStateOf("") }
    var levelFilter by rememberSaveable { mutableIntStateOf(-1) }
    val filteredSpells = availableSpells.filter { spell ->
        (levelFilter < 0 || spell.level == levelFilter) &&
            (
                query.isBlank() ||
                    spell.name.contains(query, ignoreCase = true) ||
                    spell.description.contains(query, ignoreCase = true) ||
                    spell.school.name.contains(query, ignoreCase = true)
                )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Edit Spellbook", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search spells") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        levelFilter = when {
                            levelFilter < 0 -> maximumLevel
                            levelFilter == 0 -> -1
                            else -> levelFilter - 1
                        }
                    }
                ) {
                    Text("Previous")
                }
                Text(
                    text = when (levelFilter) {
                        -1 -> "All levels"
                        0 -> "Cantrips"
                        else -> "Level $levelFilter"
                    },
                    modifier = Modifier.padding(top = 12.dp)
                )
                OutlinedButton(
                    onClick = {
                        levelFilter = when {
                            levelFilter < 0 -> 0
                            levelFilter >= maximumLevel -> -1
                            else -> levelFilter + 1
                        }
                    }
                ) {
                    Text("Next")
                }
            }

            Spacer(Modifier.height(6.dp))
            Text("Showing ${filteredSpells.size} of ${availableSpells.size} available spells")
            if (
                learningMode == SpellLearningMode.PREPARED ||
                learningMode == SpellLearningMode.SPELLBOOK
            ) {
                Text(
                    "Prepared: $preparedLevelledCount/$preparedLimit " +
                        "(cantrips do not need preparation)"
                )
            }
            Spacer(Modifier.height(10.dp))

            if (filteredSpells.isEmpty()) {
                Text("No spells match these filters.")
            }

            filteredSpells.forEach { spell ->
                val known = character.knownSpellIds.contains(spell.id)
                val prepared = character.preparedSpellIds.contains(spell.id)
                val automaticSubclassSpell =
                    spell.id in character.subclassSpellIds

                Text(
                    "${spell.name} • ${spell.levelLabel} • " +
                        spell.school.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    buildList {
                        add(spell.castingTime)
                        add(spell.range)
                        if (spell.ritual) add("Ritual")
                        if (spell.concentration) add("Concentration")
                    }.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall
                )
                if (automaticSubclassSpell) {
                    Text(
                        "Always prepared by subclass",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(5.dp))

                Button(
                    onClick = { onToggleKnownSpell(spell.id) },
                    enabled = !automaticSubclassSpell,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (automaticSubclassSpell) {
                            "Granted by subclass: ${spell.name}"
                        } else {
                            knownButtonText(known, learningMode, spell.name)
                        }
                    )
                }

                if (
                    !spell.isCantrip &&
                    (
                        learningMode == SpellLearningMode.PREPARED ||
                            learningMode == SpellLearningMode.SPELLBOOK
                        )
                ) {
                    Spacer(Modifier.height(5.dp))
                    OutlinedButton(
                        onClick = { onTogglePreparedSpell(spell.id) },
                        enabled = !automaticSubclassSpell &&
                            known &&
                            (prepared || preparedLevelledCount < preparedLimit),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (prepared) {
                                "Unprepare: ${spell.name}"
                            } else {
                                "Prepare: ${spell.name}"
                            }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

private fun knownButtonText(
    known: Boolean,
    learningMode: SpellLearningMode,
    spellName: String
): String {
    val noun = if (learningMode == SpellLearningMode.SPELLBOOK) {
        "Spellbook"
    } else {
        "Known"
    }
    return if (known) {
        "Remove from $noun: $spellName"
    } else {
        "Add to $noun: $spellName"
    }
}
