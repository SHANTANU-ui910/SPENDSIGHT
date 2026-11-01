package com.spendsight.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * SpendSight "Chroma-Morphism" Palette
 * Distinct, non-generic cyber-financial aesthetic with deep void depth,
 * dual-lit specular edges, and chromatic holographic accents.
 */

// Void & Obsidian Substrates
val VoidBackground = Color(0xFF06080E)           // Deepest void black-blue
val SurfaceObsidian = Color(0xFF0D111C)          // Primary cockpit surface
val SurfaceElevated = Color(0xFF141A29)          // Floating card surface
val SurfaceHighlight = Color(0xFF1D263B)         // Interactive raised elements

// Specular & Occlusion Morphism Borders (Dual-lighting for physical glass depth)
val SpecularHighlightTop = Color(0x38FFFFFF)      // 22% white top-left light catch
val SpecularHighlightSubtle = Color(0x1AFFFFFF)   // 10% white mid light
val OcclusionShadowBottom = Color(0x99000000)     // 60% black bottom-right ambient occlusion
val ChromaBorderTeal = Color(0x6600F5D4)         // Phosphor teal edge illumination
val ChromaBorderViolet = Color(0x669D4EDD)       // Cyber violet edge illumination

// Chromatic Luminescence Accents (Bespoke Neon Tones)
val PhosphorMint = Color(0xFF00FF9D)             // Primary telemetry / positive cashflow
val PhosphorMintGlow = Color(0x2E00FF9D)

val HyperTeal = Color(0xFF00F5D4)                // Active buttons, primary CTA
val HyperTealGlow = Color(0x3300F5D4)

val CyberAmethyst = Color(0xFF9D4EDD)            // Secondary telemetry, recurring spend
val CyberAmethystGlow = Color(0x339D4EDD)

val SolarAmber = Color(0xFFFFB300)               // Burn alerts, warnings, budget threshold
val SolarAmberGlow = Color(0x33FFB300)

val CrimsonLaser = Color(0xFFFF2A6D)             // Debit / Expense / Danger indicator
val CrimsonLaserGlow = Color(0x33FF2A6D)

val CobaltFlux = Color(0xFF2D7FF9)               // UPI / System telemetry accents
val CobaltFluxGlow = Color(0x332D7FF9)

// High-Legibility Typography Tones (WCAG AAA on VoidBackground)
val TextPureWhite = Color(0xFFFFFFFF)            // Hero amounts, primary titles
val TextTechnicalCyan = Color(0xFF80FFF4)        // Metadata headers, code labels
val TextSlateBody = Color(0xFFCBD5E1)            // Body text, transaction notes
val TextMutedSubtext = Color(0xFF64748B)         // Timestamps, subtle account digits
val TextDarkInverse = Color(0xFF06080E)          // On neon primary buttons

// Category Chroma-Identity (Each category has unique spectral identity)
val CategoryFoodChroma = Color(0xFFFF8A00)        // Amber Flare
val CategoryTravelChroma = Color(0xFF00C2FF)      // Azure Neon
val CategoryBillsChroma = Color(0xFFFF2A6D)       // Crimson Laser
val CategoryShoppingChroma = Color(0xFFA259FF)    // Ultraviolet
val CategoryHealthChroma = Color(0xFF00FF9D)      // Phosphor Mint
val CategoryRentChroma = Color(0xFFFF5252)        // Vermillion
val CategoryGroceriesChroma = Color(0xFFFFD166)   // Goldenrod
val CategoryInvestmentsChroma = Color(0xFF06D6A0) // Emerald Vault
val CategoryOtherChroma = Color(0xFF8E9AAF)       // Muted Steel

// Morphism Glass Gradients
val GlassSurfaceBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xCC141A29), // Top surface catching light
        Color(0xEE0D111C)  // Bottom surface fading into obsidian
    )
)

val GlassOverlayBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xF0101624),
        Color(0xFA080B14)
    )
)

val KeypadDishBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF161E30), // Inset dish shadow top
        Color(0xFF0E1320)  // Inset dish light bottom
    )
)

val KeypadPressedBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF090D16),
        Color(0xFF141A29)
    )
)

// Ambient Refraction Glows
val HeroGlowBrush = Brush.radialGradient(
    colors = listOf(
        Color(0x2600F5D4),
        Color(0x0A9D4EDD),
        Color.Transparent
    )
)
