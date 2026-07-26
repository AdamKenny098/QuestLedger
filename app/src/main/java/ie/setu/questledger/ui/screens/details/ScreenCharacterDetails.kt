package ie.setu.questledger.ui.screens.details

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import ie.setu.questledger.data.compendium.CompendiumLookup
import ie.setu.questledger.data.rules.CharacterStatEngine
import ie.setu.questledger.ui.components.general.equipment.CharacterEquipmentCard
import ie.setu.questledger.ui.components.general.backgrounds.CharacterBackgroundCard
import ie.setu.questledger.ui.components.general.ancestry.CharacterAncestryCard
import ie.setu.questledger.ui.components.general.inventory.CharacterInventoryCard
import ie.setu.questledger.ui.components.general.session.CharacterSessionCard
import ie.setu.questledger.ui.components.general.stats.CharacterDerivedStatsCard
import ie.setu.questledger.ui.components.general.advancement.CharacterAdvancementCard
import ie.setu.questledger.ui.components.general.features.CharacterFeaturesCard
import ie.setu.questledger.ui.components.general.subclasses.CharacterSubclassCard

@Composable
fun ScreenCharacterDetails(
    onOpenEdit: (String) -> Unit,
    onOpenSpellbook: (String) -> Unit = {},
    vm: CharacterDetailsViewModel = hiltViewModel()
) {
    val c by vm.character
    val isSessionSaving by vm.isSessionSaving
    val sessionMessage by vm.sessionMessage

    val isSpellcaster = CharacterStatEngine.build(c).spellcastingAbilityLabel != null

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Character Details", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            if (c.imageUri.isNotBlank()) {
                AsyncImage(
                    model = c.imageUri,
                    contentDescription = "Character Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(Modifier.height(12.dp))
            }

            Text(
                text = c.name,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = buildString {
                    append(
                        CompendiumLookup.characterRaceDisplayName(
                            c.race,
                            c.raceVariant
                        )
                    )
                    if (c.subclass.isNotBlank()) {
                        append(" ")
                        append(CompendiumLookup.subclassDisplayName(c.subclass))
                    }
                    append(" ")
                    append(CompendiumLookup.classDisplayName(c.characterClass))
                    append(" • Level ${c.level}")
                },
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = if (c.notes.isBlank()) "No notes." else c.notes,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onOpenEdit(c.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Character")
            }

            if (isSpellcaster) {
                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { onOpenSpellbook(c.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Spellbook")
                }
            }

            Spacer(Modifier.height(16.dp))

            CharacterSessionCard(
                character = c,
                isSaving = isSessionSaving,
                message = sessionMessage,
                onDamage = vm::takeDamage,
                onHeal = vm::heal,
                onSetTemporaryHitPoints = vm::setTemporaryHitPoints,
                onRollDeathSave = vm::rollDeathSave,
                onResetDeathSaves = vm::resetDeathSaves,
                onSpendHitDie = vm::spendHitDie,
                onUseSpellSlot = vm::useSpellSlot,
                onRestoreSpellSlot = vm::restoreSpellSlot,
                onShortRest = vm::takeShortRest,
                onLongRest = vm::takeLongRest,
                onToggleInspiration = vm::toggleInspiration
            )

            Spacer(Modifier.height(12.dp))

            CharacterFeaturesCard(
                character = c,
                isSaving = isSessionSaving,
                onUseFeature = vm::useFeature,
                onRestoreFeature = vm::restoreFeature,
                onEndFeature = vm::endFeature
            )

            Spacer(Modifier.height(12.dp))

            CharacterDerivedStatsCard(character = c)

            Spacer(Modifier.height(12.dp))

            CharacterAncestryCard(character = c)

            Spacer(Modifier.height(12.dp))

            CharacterSubclassCard(character = c)

            Spacer(Modifier.height(12.dp))

            CharacterBackgroundCard(character = c)

            Spacer(Modifier.height(12.dp))

            CharacterAdvancementCard(character = c)

            Spacer(Modifier.height(12.dp))

            CharacterEquipmentCard(character = c)

            Spacer(Modifier.height(12.dp))

            CharacterInventoryCard(character = c)
        }
    }
}
