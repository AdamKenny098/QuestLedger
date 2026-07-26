package ie.setu.questledger.ui.components.general.spells

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ie.setu.questledger.data.compendium.AbilityType
import ie.setu.questledger.data.compendium.CompendiumService
import ie.setu.questledger.data.compendium.SpellDefinition
import ie.setu.questledger.data.compendium.SpellLearningMode
import ie.setu.questledger.data.rules.CharacterStatEngine
import ie.setu.questledger.data.rules.CharacterSessionRules
import ie.setu.questledger.data.rules.SpellRules
import ie.setu.questledger.models.characters.CharacterModel

@Composable
fun CharacterSpellbookCard(
    character: CharacterModel,
    compendiumService: CompendiumService,
    modifier: Modifier = Modifier
) {
    val playableCharacter = CharacterSessionRules.normalise(character)
    val derived = CharacterStatEngine.build(playableCharacter)
    val characterClass = compendiumService.getClassById(character.characterClass)
    val learningMode = characterClass?.spellLearningMode ?: SpellLearningMode.NONE
    val knownSpells = character.knownSpellIds
        .mapNotNull(compendiumService::getSpellById)
        .distinctBy(SpellDefinition::id)
        .sortedWith(compareBy(SpellDefinition::level, SpellDefinition::name))
    val preparedSpells = character.preparedSpellIds
        .mapNotNull(compendiumService::getSpellById)
        .distinctBy(SpellDefinition::id)
        .sortedWith(compareBy(SpellDefinition::level, SpellDefinition::name))

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Spellbook", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            Text("Spellcasting Ability: ${derived.spellcastingAbilityLabel ?: "None"}")
            Text(
                "Spell Attack Bonus: " +
                    if (derived.spellAttackBonus >= 0) {
                        "+${derived.spellAttackBonus}"
                    } else {
                        derived.spellAttackBonus.toString()
                    }
            )
            Text("Spell Save DC: ${derived.spellSaveDc}")

            val slotsText = derived.spellSlotsByLevel
                .mapIndexedNotNull { index, count ->
                    if (count > 0) {
                        val remaining = playableCharacter.remainingSpellSlotsByLevel
                            .getOrElse(index) { count }
                        "L${index + 1} $remaining/$count"
                    } else {
                        null
                    }
                }
                .joinToString(", ")
                .ifBlank { "None" }
            Text("Spell Slots: $slotsText")

            if (
                characterClass != null &&
                (
                    learningMode == SpellLearningMode.PREPARED ||
                        learningMode == SpellLearningMode.SPELLBOOK
                    )
            ) {
                Text(
                    "Prepared Limit: ${preparedSpells.count { !it.isCantrip }}/" +
                        SpellRules.preparedSpellLimit(character, characterClass)
                )
            }

            Spacer(Modifier.height(14.dp))
            Text(knownSectionTitle(learningMode), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
            SpellList(spells = knownSpells)

            if (
                learningMode == SpellLearningMode.PREPARED ||
                learningMode == SpellLearningMode.SPELLBOOK
            ) {
                Spacer(Modifier.height(14.dp))
                Text("Prepared Spells", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                SpellList(spells = preparedSpells)
            }
        }
    }
}

@Composable
private fun SpellList(spells: List<SpellDefinition>) {
    if (spells.isEmpty()) {
        Text("None")
        return
    }
    spells
        .groupBy(SpellDefinition::level)
        .toSortedMap()
        .forEach { (level, levelSpells) ->
            Text(
                text = if (level == 0) "Cantrips" else "Level $level",
                style = MaterialTheme.typography.labelLarge
            )
            levelSpells.forEach { spell ->
                SpellEntry(spell)
            }
            Spacer(Modifier.height(6.dp))
        }
}

@Composable
private fun SpellEntry(spell: SpellDefinition) {
    var expanded by remember(spell.id) { mutableStateOf(false) }

    TextButton(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "${spell.name} • ${spell.school.displayName()}" +
                buildList {
                    if (spell.ritual) add("Ritual")
                    if (spell.concentration) add("Concentration")
                }.joinToString(prefix = if (spell.ritual || spell.concentration) " • " else "")
        )
    }

    if (!expanded) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text("Casting Time: ${spell.castingTime}")
        Text("Range: ${spell.range}")
        Text("Duration: ${spell.duration}")
        Text("Components: ${spell.componentLabel}")
        spell.materialComponent?.let { Text("Material: $it") }
        spell.attackType?.let { Text("Spell Attack: ${it.name.lowercase().capitalized()}") }
        spell.saveType
            ?.takeIf { it != AbilityType.NONE }
            ?.let { Text("Saving Throw: ${it.name.lowercase().capitalized()}") }
        spell.damageDice?.let { dice ->
            Text("Damage: $dice${spell.damageType?.let { " $it" }.orEmpty()}")
        }
        spell.healingDice?.let { Text("Healing: $it") }
        Spacer(Modifier.height(6.dp))
        Text(spell.description, style = MaterialTheme.typography.bodySmall)
        spell.higherLevels?.let {
            Spacer(Modifier.height(6.dp))
            Text("At Higher Levels: $it", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun knownSectionTitle(mode: SpellLearningMode): String {
    return when (mode) {
        SpellLearningMode.SPELLBOOK -> "Spells in Spellbook"
        SpellLearningMode.PREPARED -> "Selected Class Spells"
        else -> "Known Spells"
    }
}

private fun Enum<*>.displayName(): String = name.lowercase().capitalized()

private fun String.capitalized(): String = replaceFirstChar { it.uppercase() }
