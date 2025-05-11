package com.example.gembot

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gembot.ui.theme.GemBotTheme
import androidx.navigation.compose.NavHost


class MainActivity : ComponentActivity() {

    @SuppressLint("UnrememberedMutableState")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val ChatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }
            var selectedLanguage by rememberSaveable { mutableStateOf("English") } // Track selected language
            GemBotTheme(darkTheme = isDarkTheme) {

                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "chat_page"
                ) {
                    // Composable for Chat Page
                    composable("chat_page") {
                        ChatPage(
                            modifier = Modifier,
                            viewModel = ChatViewModel,
                            isDarkTheme = isDarkTheme,
                            onThemeToggle = { isDarkTheme = it },
                            onSettingsClick = {
                                navController.navigate("settings_page")
                            },
                            onVoiceClick = { shouldSpeak ->
                                // Toggle TTS based on current state
                                if (shouldSpeak) {
                                    ChatViewModel.speakLastMessage()
                                } else {
                                    ChatViewModel.stopSpeaking()
                                }
                            },
                            isSpeaking = ChatViewModel.isSpeaking.value
                        )
                    }
                    composable("settings_page") {
                        SettingsPage(
                            isDarkTheme = isDarkTheme,
                            selectedLanguage = selectedLanguage,  // Pass selected language
                            onLanguageChange = { selectedLanguage = it },  // Handle language change
                            onThemeToggle = { isDarkTheme = it },
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                }
            }
        }
    }
}

