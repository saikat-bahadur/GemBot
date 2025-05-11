package com.example.gembot.ui.theme


import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


private val DarkColorScheme = darkColorScheme(

    primary = Color(0xFF128C7E), // Lighter green header to stand out
    background = Color(0xFF121B22), // WhatsApp dark grey background
    surface = Color(0xFF202C33), // Dark message bubbles (for received messages)
    onSurface = Color(0xFFE9EBEB), // Light text color for dark mode (message text)
    primaryContainer = Color(0xFF056162), // Sent message bubble color (dark green)
    onPrimary = Color(0xFFFFFFFF), // Text on primary surfaces (e.g., header text)
    onBackground = Color(0xFFEDEDED), // Lighter text for dark backgrounds (general chat text)
    secondary = Color(0xFF2A3942) // Lighter shade for elements like input fields
)

private val LightColorScheme = lightColorScheme(

    primary = Color(0xFF075E54), // WhatsApp green for header
    background = Color(0xFFECE5DD), // WhatsApp light grey for background

    surface = Color(0xFFFFFFFF), // White message bubbles (for received messages)
    onSurface = Color(0xFF000000), // Black text for light mode
    primaryContainer = Color(0xFFDCF8C6), // Sent message bubble (light green)
    onPrimary = Color(0xFFFFFFFF) // White text on the header
//    Other default colors to override
//    background = Color(0xFFFFFBFE),
//    surface = Color(0xFFFFFBFE),
//    onPrimary = Color.White,
//    onSecondary = Color.White,
//    onTertiary = Color.White,
//    onBackground = Color(0xFF1C1B1F),
//    onSurface = Color(0xFF1C1B1F),

)

@Composable
fun GemBotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),

    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}