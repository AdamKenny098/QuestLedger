package ie.setu.questledger.ui.screens.details

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ie.setu.questledger.data.rules.CharacterSessionRules
import ie.setu.questledger.data.rules.CharacterStatEngine
import ie.setu.questledger.ui.components.general.advancement.CharacterAdvancementCard
import ie.setu.questledger.ui.components.general.ancestry.CharacterAncestryCard
import ie.setu.questledger.ui.components.general.backgrounds.CharacterBackgroundCard
import ie.setu.questledger.ui.components.general.features.CharacterFeaturesCard
import ie.setu.questledger.ui.components.general.session.CharacterSessionCard
import ie.setu.questledger.ui.components.general.subclasses.CharacterSubclassCard
import ie.setu.questledger.ui.components.ledger.CharacterAbilitiesBeyondPanel
import ie.setu.questledger.ui.components.ledger.CharacterCombatRollsPanel
import ie.setu.questledger.ui.components.ledger.CharacterFeaturesOverviewPanel
import ie.setu.questledger.ui.components.ledger.CharacterInventoryItemCards
import ie.setu.questledger.ui.components.ledger.CharacterPlayOverviewPanel
import ie.setu.questledger.ui.components.ledger.CharacterSheetHeader
import ie.setu.questledger.ui.components.ledger.CharacterSheetSection
import ie.setu.questledger.ui.components.ledger.CharacterSheetTabs
import ie.setu.questledger.ui.components.ledger.CharacterStoryOverviewPanel
import ie.setu.questledger.ui.components.ledger.LedgerDisclosureSection
import ie.setu.questledger.ui.components.ledger.LedgerPanel

@Composable
fun ScreenCharacterDetails(
    onOpenSpellbook: (String) -> Unit = {},
    vm: CharacterDetailsViewModel = hiltViewModel()
) {
    val loadedCharacter by vm.character
    val isLoading by vm.isLoading
    val isError by vm.isErr
    val error by vm.error
    val isSessionSaving by vm.isSessionSaving
    val sessionMessage by vm.sessionMessage

    when {
        isLoading && loadedCharacter.id.isBlank() -> {
            CharacterSheetLoading()
            return
        }

        isError && loadedCharacter.id.isBlank() -> {
            CharacterSheetError(
                message = error.message ?: "The character could not be loaded."
            )
            return
        }
    }

    val character = CharacterSessionRules.normalise(loadedCharacter)
    val derived = CharacterStatEngine.build(character)
    val showSpellbook =
        derived.spellcastingAbilityLabel != null ||
            character.knownSpellIds.isNotEmpty() ||
            character.racialSpellIds.isNotEmpty() ||
            character.subclassSpellIds.isNotEmpty()
    val sections = remember {
        buildList {
            add(CharacterSheetSection.OVERVIEW)
            add(CharacterSheetSection.COMBAT)
            add(CharacterSheetSection.ABILITIES)
            add(CharacterSheetSection.FEATURES)
            add(CharacterSheetSection.INVENTORY)
            add(CharacterSheetSection.STORY)
        }
    }
    var selectedSectionName by rememberSaveable {
        mutableStateOf(CharacterSheetSection.OVERVIEW.name)
    }
    val selectedSection = sections.firstOrNull {
        it.name == selectedSectionName
    } ?: CharacterSheetSection.OVERVIEW
    val scrollStates = remember {
        CharacterSheetSection.values().associateWith { ScrollState(0) }
    }
    val selectSection: (CharacterSheetSection) -> Unit = {
        selectedSectionName = it.name
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CharacterSheetHeader(
                character = character,
                derived = derived,
                showSpellbook = showSpellbook,
                isSaving = isSessionSaving,
                onOpenSpellbook = { onOpenSpellbook(character.id) },
                modifier = Modifier.padding(
                    start = 12.dp,
                    top = 10.dp,
                    end = 12.dp,
                    bottom = 8.dp
                )
            )

            CharacterSheetTabs(
                sections = sections,
                selectedSection = selectedSection,
                onSelectSection = selectSection
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollStates.getValue(selectedSection))
                    .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedSection) {
                    CharacterSheetSection.OVERVIEW -> {
                        CharacterPlayOverviewPanel(
                            character = character,
                            derived = derived
                        )
                    }

                    CharacterSheetSection.COMBAT -> {
                        CharacterCombatRollsPanel(
                            character = character,
                            derived = derived
                        )
                        CharacterSessionCard(
                            character = character,
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
                    }

                    CharacterSheetSection.ABILITIES -> {
                        CharacterAbilitiesBeyondPanel(
                            character = character,
                            derived = derived
                        )
                    }

                    CharacterSheetSection.FEATURES -> {
                        CharacterFeaturesOverviewPanel(
                            character = character,
                            derived = derived
                        )
                        LedgerDisclosureSection(
                            title = "Class Feature Controls",
                            summary = "Use, restore, and end tracked class resources.",
                            initiallyExpanded = true
                        ) {
                            CharacterFeaturesCard(
                                character = character,
                                isSaving = isSessionSaving,
                                onUseFeature = vm::useFeature,
                                onRestoreFeature = vm::restoreFeature,
                                onEndFeature = vm::endFeature
                            )
                        }
                        LedgerDisclosureSection(
                            title = "Subclass",
                            summary = if (character.subclass.isBlank()) {
                                "No subclass selected."
                            } else {
                                "Subclass identity, choices, and unlocked features."
                            }
                        ) {
                            CharacterSubclassCard(character = character)
                        }
                        LedgerDisclosureSection(
                            title = "Advancement",
                            summary = "Level progression, feats, and advancement choices."
                        ) {
                            CharacterAdvancementCard(character = character)
                        }
                    }

                    CharacterSheetSection.INVENTORY -> {
                        CharacterInventoryItemCards(character = character)
                    }

                    CharacterSheetSection.STORY -> {
                        CharacterStoryOverviewPanel(character = character)
                        LedgerDisclosureSection(
                            title = "Ancestry & Traits",
                            summary = "Ancestry, heritage, languages, and racial traits."
                        ) {
                            CharacterAncestryCard(character = character)
                        }
                        LedgerDisclosureSection(
                            title = "Background",
                            summary = "Background feature, proficiencies, and origin details."
                        ) {
                            CharacterBackgroundCard(character = character)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterSheetLoading() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Opening character ledger…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CharacterSheetError(message: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            LedgerPanel(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Character unavailable",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Use the back arrow and try opening the character again.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
