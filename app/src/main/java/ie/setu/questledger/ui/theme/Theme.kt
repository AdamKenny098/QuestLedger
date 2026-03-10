package ie.setu.questledger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ArcaneDarkScheme = darkColorScheme(
    primary = QL_Primary,
    onPrimary = QL_OnPrimary,
    secondary = QL_Secondary,
    onSecondary = QL_OnSecondary,
    tertiary = QL_Tertiary,
    onTertiary = QL_OnTertiary,
    background = QL_Background,
    onBackground = QL_OnBackground,
    surface = QL_Surface,
    onSurface = QL_OnSurface,
    surfaceVariant = QL_SurfaceVariant,
    onSurfaceVariant = QL_OnSurfaceVariant,
    error = QL_Error,
    onError = QL_OnError
)

@Composable
fun QuestLedgerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ArcaneDarkScheme,
        typography = Typography,
        content = content
    )
}