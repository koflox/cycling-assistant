package com.koflox.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Standardized spacing values for consistent UI throughout the app.
 */
object Spacing {
    /** 4.dp - Fine spacing between closely related elements */
    val Tiny = 4.dp

    /** 8.dp - Small spacing for button gaps, minor padding */
    val Small = 8.dp

    /** 12.dp - Medium spacing for internal card/component padding */
    val Medium = 12.dp

    /** 16.dp - Standard padding for screen edges, cards, containers */
    val Large = 16.dp

    /** 24.dp - Large spacing for horizontal dividers, stat item gaps */
    val ExtraLarge = 24.dp

    /** 32.dp - Extra large padding for empty/loading states */
    val Huge = 32.dp
}

/**
 * Standardized elevation values for cards and surfaces.
 */
object Elevation {
    /** 2.dp - Subtle elevation for list items */
    val Subtle = 2.dp

    /** 4.dp - Prominent elevation for cards and overlays */
    val Prominent = 4.dp
}

/**
 * Standardized corner radius values.
 * Prefer using MaterialTheme.shapes when possible.
 */
object CornerRadius {
    /** 8.dp - Small corners for info windows, chips */
    val Small = 8.dp

    /** 12.dp - Medium corners for buttons, cards */
    val Medium = 12.dp
}

/**
 * Common alpha values for surface overlays.
 */
object SurfaceAlpha {
    /** 0.9f - Light overlay transparency */
    const val Light = 0.9f

    /** 0.95f - Standard overlay transparency */
    const val Standard = 0.95f
}
