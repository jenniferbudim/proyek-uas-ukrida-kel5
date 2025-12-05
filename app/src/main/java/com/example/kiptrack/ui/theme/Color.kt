package com.example.kiptrack.ui.theme

import androidx.compose.ui.graphics.Color

// --- Standard Material 3 Defaults (Keep these if your Theme.kt uses them) ---
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// --- UNIFIED APP PALETTE ---

/**
 * 50: Backgrounds, Inputs, Very Light Cards
 * Merges: LightPurple, RefBgLightPurple, RefInputBackground, PurpleLightBg
 */
val Purple50 = Color(0xFFF3E5F5)

/**
 * 100: Dropdowns, Back Buttons, Header Gradients (Start)
 * Merges: RefDropdownBg, RefButtonBack, PurpleHeaderStart
 */
val Purple100 = Color(0xFFE1BEE7)

/**
 * 200: Secondary Buttons, Header Gradients (End)
 * Merges: MediumPurple, RefButtonSave, PurpleHeaderEnd
 */
val Purple200 = Color(0xFFCE93D8)

/**
 * 300: Labels, Deep Accents
 * Merges: DeepPurple, RefLabelColor, TextLabelColor
 */
val Purple300 = Color(0xFF9575CD)

/**
 * 500: MAIN BRAND COLOR (Primary)
 * Merges: PurplePrimary, RefAvatarIcon, RefHeaderPurple
 */
val PurplePrimary = Color(0xFF9C27B0)

/**
 * 700: Dark Accents, Strong Text
 * Merges: PurpleDark, RefTextPurple
 */
val PurpleDark = Color(0xFF7B1FA2)

/**
 * 900: High Contrast Text (Black-Purple)
 * Merges: TextPurple
 */
val PurpleTextDeep = Color(0xFF4A148C)

// --- Special Purpose Colors ---

// Used for specific gradients or avatars that need to differ slightly from the main palette
val AvatarBackground = Color(0xFFD1C4E9)
val BackgroundGradientEnd = Color(0xFFEDE7F6)

val CardBg = Color.White

// --- Chart Colors ---
val PieGreen = Color(0xFF009688)
val PieOrange = Color(0xFFE64A19)
val PieRed = Color(0xFFD32F2F)

val SuccessGreen = Color(0xFF388E3C)