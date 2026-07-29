package ie.setu.questledger.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ie.setu.questledger.ui.components.ledger.LedgerBadge
import ie.setu.questledger.ui.components.ledger.LedgerPanel
import ie.setu.questledger.ui.components.ledger.LedgerScreenIntro

@Composable
fun ScreenCharacterCreate(
    onOpenQuickSetup: () -> Unit = {},
    onOpenPremade: () -> Unit = {},
    onOpenFullSetup: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LedgerScreenIntro(
                eyebrow = "Begin a new chronicle",
                title = "Create a Character",
                body = "Choose how much control you want. Every path produces a complete, editable character."
            )

            Spacer(Modifier.height(2.dp))

            LedgerPanel(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f),
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.62f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Three paths. One living character sheet.",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "You can change the details later as your campaign grows.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            CreationPathCard(
                eyebrow = "Fastest",
                title = "Quick Forge",
                description = "Pick a class, ancestry, background and level. QuestLedger handles the sensible defaults.",
                duration = "About 2 minutes",
                icon = Icons.Filled.Bolt,
                accent = MaterialTheme.colorScheme.secondary,
                recommended = true,
                onClick = onOpenQuickSetup
            )

            CreationPathCard(
                eyebrow = "Ready to play",
                title = "Premade Heroes",
                description = "Start from a prepared adventurer, then rename and customise them for your table.",
                duration = "Instant",
                icon = Icons.Filled.Groups,
                accent = MaterialTheme.colorScheme.tertiary,
                onClick = onOpenPremade
            )

            CreationPathCard(
                eyebrow = "Most control",
                title = "Full Ledger",
                description = "Build step by step with complete control over abilities, equipment, spells and choices.",
                duration = "10–15 minutes",
                icon = Icons.Filled.AutoStories,
                accent = MaterialTheme.colorScheme.primary,
                onClick = onOpenFullSetup
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CreationPathCard(
    eyebrow: String,
    title: String,
    description: String,
    duration: String,
    icon: ImageVector,
    accent: Color,
    recommended: Boolean = false,
    onClick: () -> Unit
) {
    LedgerPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        borderColor = accent.copy(alpha = 0.72f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = accent.copy(alpha = 0.16f)
            ) {
                Box(
                    modifier = Modifier.size(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = eyebrow.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = accent
                    )
                    if (recommended) {
                        LedgerBadge(
                            text = "Recommended",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = duration,
                    style = MaterialTheme.typography.labelMedium,
                    color = accent
                )
            }

            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "Open $title",
                tint = accent
            )
        }
    }
}
