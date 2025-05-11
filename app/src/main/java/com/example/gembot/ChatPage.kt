package com.example.gembot


import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.noties.markwon.Markwon
import java.util.Locale
import android.Manifest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border


//Chat Page
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatPage(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    onVoiceClick: (Boolean) -> Unit,
    isSpeaking: Boolean  // Track if TTS is currently speaking

) {
    // Status bar color
    val systemUiController = rememberSystemUiController()
    // Change status bar icons based on dark/light mode
    SideEffect {
        if (isDarkTheme) {
            systemUiController.setStatusBarColor(
                color = Color.Transparent, // Keeps the status bar transparent
                darkIcons = false // White icons for dark mode
            )
        } else {
            systemUiController.setStatusBarColor(
                color = Color.Transparent, // Keeps the status bar transparent
                darkIcons = true // Black icons for light mode
            )
        }
    }
       Column(
            modifier = modifier
                .background(if (isDarkTheme) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .imePadding()
                .systemBarsPadding()//avoid gesture input box above the gesture
        ) {

            AppHeader(isDarkTheme = isDarkTheme, onThemeToggle = onThemeToggle, onSettingsClick = onSettingsClick, onVoiceClick = onVoiceClick,
                isSpeaking = isSpeaking)


            MessageList(
                modifier = Modifier.weight(.1f),
                messageList = viewModel.messageList
            )

            MessageInput(onMessageSend = {
                viewModel.sendMessage(it)
            })
        }
    }



    //Message List Or Model
    @Composable
    fun MessageList(modifier: Modifier = Modifier, messageList: List<MessageModel>) {
        val textColor = MaterialTheme.colorScheme.onBackground  // Text color  background
        val iconColor = MaterialTheme.colorScheme.primary       // Icon color  theme
        if (messageList.isEmpty()) {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(

                    painterResource(id = R.drawable.baseline_question_answer_24),
                    contentDescription = "Icon",
                    tint = iconColor, // icon color
//                    modifier = Modifier.size(60.dp)
                )
                Text(text = "Ask me anything",
                    fontSize = 22.sp,
                    color = textColor

                )

            }

        } else {
            LazyColumn(
                modifier = modifier,
                reverseLayout = true

            ) {
                items(messageList.reversed()) {
                    MessageRow(messageModel = it)
                }
            }

        }
    }



    @Composable
    fun MarkdownText(markdown: String, modifier: Modifier = Modifier) {
        val context = LocalContext.current
        val markwon = remember { Markwon.create(context) }


        val markdownParsed = remember(markdown) {
            markwon.toMarkdown(markdown)
        }


        val annotatedString = remember(markdownParsed) {
            buildAnnotatedString {
                append(markdownParsed.toString())
            }
        }
        val textColor = MaterialTheme.colorScheme.onSurface


        Text(
            text = annotatedString,
            color = textColor,  // Apply the text color from the theme
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .padding(16.dp)
        )
    }

   // Displaying the message
    @Composable
    fun MessageRow(messageModel: MessageModel) {
        val isModel = messageModel.role == "model"

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val messageBubbleColor = if (isModel) {
                    MaterialTheme.colorScheme.primaryContainer //message bubble color
                } else {
                    MaterialTheme.colorScheme.surface // Other user's message bubble color
                }
                Box(
                    modifier = Modifier
                        .align(if (isModel) Alignment.BottomStart else Alignment.BottomEnd)
                        .padding(
                            start = if (isModel) 8.dp else 70.dp,
                            end = if (isModel) 70.dp else 8.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(messageBubbleColor)
                        .padding(16.dp)
                ) {
                    MarkdownText(
                        markdown = messageModel.message,


                    )
                }
            }
        }
    }


    //Message Input -- Text Box And send Symbol initial
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MessageInput(onMessageSend: (String) -> Unit) {
        var message by remember {
            mutableStateOf("")
        }
        val context = LocalContext.current
        val activity = context as? Activity
        if (activity == null) {
            // Error: No valid Activity context found
            Text("Error: Activity context is required")
            return
        }

        // Permission request launcher for RECORD_AUDIO
        val requestPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (!isGranted) {
                    // Handle if permission is not granted
                    Toast.makeText(
                        context,
                        "Permission denied. Voice input is not available.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
        // Check permission dynamically
        val hasAudioPermission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                // Handle errors during speech recognition
                Toast.makeText(context, "Speech recognition error", Toast.LENGTH_SHORT).show()
            }
            override fun onResults(results: Bundle?) {
                val spokenText = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (spokenText != null) {
                    message = spokenText // Update the input field with spoken text
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                OutlinedTextField(
                    modifier = Modifier.weight(1f)
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary),  // Adjust thickness and color
                            shape = RoundedCornerShape(8.dp)  // Adjust the corner shape as needed
                        ),
                    placeholder = {
                        Text(
                            "Type a message",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = MaterialTheme.colorScheme.surface, //  background for text input
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    value = message,
                    onValueChange = {
                        message = it
                    })

                IconButton(onClick = {
                    if (message.isNotEmpty()) {
                        onMessageSend(message)
                        message = ""
                    }

                }) {

                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "send",
                        tint = MaterialTheme.colorScheme.primary
                    )

                }

                IconButton(
                    onClick = {
                        if (hasAudioPermission) {
                            Toast.makeText(context, "Microphone clicked", Toast.LENGTH_SHORT).show()
                            // Start listening
                            speechRecognizer.startListening(speechRecognizerIntent)
                        } else {
                            Toast.makeText(context, "Requesting permission...", Toast.LENGTH_SHORT).show()
                            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    enabled = true
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.microphone),
                        contentDescription = "Voice Input",
                        modifier = Modifier
                            .size(36.dp)
                            .padding(end = 8.dp)
                    )
                }
                }


            }





//new for setting button
@Composable
fun AppHeader(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    onVoiceClick: (Boolean) -> Unit,  // Pass in the TTS toggle function
    isSpeaking: Boolean  // Track if TTS is currently speaking
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.gembot_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 8.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                fontWeight = FontWeight.Bold,
                text = "GemBot",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 30.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onVoiceClick(!isSpeaking) }) { // Pass toggle state to onVoiceClick
                Icon(
                    painter = painterResource(id = if (isSpeaking) R.drawable.mute else R.drawable.volume), // Change icon based on TTS state
                    contentDescription = if (isSpeaking) "Stop Speaking" else "Start Speaking",
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            IconButton(onClick = { onSettingsClick() }) {
                Image(
                    painter = painterResource(id = R.drawable.gear),
                    contentDescription = "Settings",
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 8.dp)

                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    isDarkTheme: Boolean,
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    onThemeToggle: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }


    val systemUiController = rememberSystemUiController()
    SideEffect {
        if (isDarkTheme) {
            systemUiController.setStatusBarColor(
                color = Color.Transparent, // Transparent status bar
                darkIcons = false
            )
        } else {
            systemUiController.setStatusBarColor(
                color = Color.Transparent, // Transparent status bar
                darkIcons = true
            )
        }
    }

    //UI for setting
    Scaffold(
        topBar = {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .statusBarsPadding(),
//

                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackClick) {
                    Image(
                        painter = painterResource(id = R.drawable.arrow), // Back arrow
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(36.dp)
                            .padding(end = 8.dp)
//
                    )
                }

                Text(
                    fontWeight = FontWeight.Bold,
                    text = "Settings",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            // Language Selection Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text("Select Language")
//

                    // Dropdown Menu for Language
                    Box {
                        TextButton(onClick = { isDropdownExpanded = true }) {
                            Text(selectedLanguage)
                        }
                        DropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(onClick = {
                                onLanguageChange("English")
                                isDropdownExpanded = false
                            }) {
                                Text("English")
                            }
//                            DropdownMenuItem(onClick = {
//                                onLanguageChange("Spanish")
//                                isDropdownExpanded = false
//                            }) {
//                                Text("Spanish")
//                            }
//                            DropdownMenuItem(onClick = {
//                                onLanguageChange("French")
//                                isDropdownExpanded = false
//                            }) {
//                                Text("French")
//                            }
//                            DropdownMenuItem(onClick = {
//                                onLanguageChange("German")
//                                isDropdownExpanded = false
//                            }) {
//                                Text("German")
//                            }
                        }
                    }
                }
            }


            // Dark Mode Toggle Section
            item {

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dark Mode")
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onThemeToggle(it) }
                    )
                }
            }


            // Help Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { showHelp = !showHelp }) {
                    Text("Help")
                }
                if (showHelp) {
                    Text(
                        """
                        Welcome to the Chatbot Help Section! This guide will assist you in interacting with the chatbot effectively and help you get the most out of its features.
                        
                        1. **Starting a Conversation**: Simply type your question or statement into the text box and press **Send**.
                        2. **Asking Questions**: The chatbot can handle a wide range of topics like general knowledge, assistance, and recommendations.
                        3. **Contextual Conversations**: Continue a conversation naturally, but use full questions for new topics.
                        4. **Supported Features**: Answering basic knowledge, task assistance, and recommendations.
                        5. **Error Handling**: If the chatbot doesn’t understand, try rephrasing the question.
                        6. **Tips for Effective Use**: Be specific, ask one question at a time, and avoid vague queries.
                        7. **Limitations**: The chatbot cannot access personal data or perform highly complex tasks.
                        8. **Reporting Issues**: Use the Feedback option to report any issues you encounter.
                    """.trimIndent()
                    )
                }
            }


            // Privacy Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { showPrivacy = !showPrivacy }) {
                    Text("Privacy")
                }
                if (showPrivacy) {
                    Text("We ensure that your data is secure and confidential. This chatbot adheres to the highest standards of privacy and does not store personal data without your consent.")
                }
            }


            // About Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { showAbout = !showAbout }) {
                    Text("About")
                }
                if (showAbout) {
                    Text("This chatbot application is designed to assist users with their queries using advanced natural language processing. Developed using modern technologies, it aims to enhance user interactions with accurate and helpful responses.")
                }
            }
        }
    }
}
