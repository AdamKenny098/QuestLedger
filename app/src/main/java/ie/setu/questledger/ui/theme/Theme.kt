package ie.setu.questledger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ArcaneLedgerScheme = darkColorScheme(
    primary = QL_Primary,
    onPrimary = QL_OnPrimary,
    primaryContainer = ColorTokens.GoldContainer,
    onPrimaryContainer = LedgerGoldBright,
    secondary = QL_Secondary,
    onSecondary = QL_OnSecondary,
    secondaryContainer = ColorTokens.VioletContainer,
    onSecondaryContainer = ColorTokens.VioletOnContainer,
    tertiary = QL_Tertiary,
    onTertiary = QL_OnTertiary,
    tertiaryContainer = ColorTokens.TealContainer,
    onTertiaryContainer = ColorTokens.TealOnContainer,
    background = QL_Background,
    onBackground = QL_OnBackground,
    surface = QL_Surface,
    onSurface = QL_OnSurface,
    surfaceVariant = QL_SurfaceVariant,
    onSurfaceVariant = QL_OnSurfaceVariant,
    surfaceContainer = LedgerSurface,
    surfaceContainerHigh = LedgerSurfaceRaised,
    surfaceContainerHighest = LedgerSurfaceMuted,
    outline = LedgerOutline,
    outlineVariant = ColorTokens.OutlineSoft,
    error = QL_Error,
    onError = QL_OnError,
    errorContainer = ColorTokens.DangerContainer,
    onErrorContainer = ColorTokens.DangerOnContainer
)

private object ColorTokens {
    val GoldContainer = androidx.compose.ui.graphics.Color(0xFF3A2A13)
    val VioletContainer = androidx.compose.ui.graphics.Color(0xFF2B2257)
    val VioletOnContainer = androidx.compose.ui.graphics.Color(0xFFDED7FF)
    val TealContainer = androidx.compose.ui.graphics.Color(0xFF123D36)
    val TealOnContainer = androidx.compose.ui.graphics.Color(0xFFC7F5EC)
    val OutlineSoft = androidx.compose.ui.graphics.Color(0xFF37342F)
    val DangerContainer = androidx.compose.ui.graphics.Color(0xFF4B1D20)
    val DangerOnContainer = androidx.compose.ui.graphics.Color(0xFFFFDADB)
}

@Composable
fun QuestLedgerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ArcaneLedgerScheme,
        typography = Typography,
        shapes = QuestLedgerShapes,
        content = content
    )
}
