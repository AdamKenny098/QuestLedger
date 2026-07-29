package ie.setu.questledger.ui.theme

import androidx.compose.ui.graphics.Color

// Arcane Ledger palette
val LedgerInk = Color(0xFF0C0D10)
val LedgerInkSoft = Color(0xFF121419)
val LedgerSurface = Color(0xFF181A20)
val LedgerSurfaceRaised = Color(0xFF22252D)
val LedgerSurfaceMuted = Color(0xFF2B2E37)

val LedgerParchment = Color(0xFFF0E7D2)
val LedgerParchmentMuted = Color(0xFFC9C0AD)
val LedgerGold = Color(0xFFC89C52)
val LedgerGoldBright = Color(0xFFE1BD78)
val LedgerViolet = Color(0xFF8B74E8)
val LedgerTeal = Color(0xFF55B8A8)
val LedgerDanger = Color(0xFFE06B70)
val LedgerOutline = Color(0xFF5D5548)

// Compatibility aliases for any older UI files that still import the QL names.
val QL_Primary = LedgerGold
val QL_OnPrimary = Color(0xFF251807)
val QL_Secondary = LedgerViolet
val QL_OnSecondary = Color(0xFF160F34)
val QL_Tertiary = LedgerTeal
val QL_OnTertiary = Color(0xFF071F1B)
val QL_Background = LedgerInk
val QL_OnBackground = LedgerParchment
val QL_Surface = LedgerSurface
val QL_OnSurface = LedgerParchment
val QL_SurfaceVariant = LedgerSurfaceRaised
val QL_OnSurfaceVariant = LedgerParchmentMuted
val QL_Error = LedgerDanger
val QL_OnError = Color(0xFF2B090B)
