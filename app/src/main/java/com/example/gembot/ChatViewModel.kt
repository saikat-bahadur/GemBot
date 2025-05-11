package com.example.gembot

import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.regex.Pattern
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson
import java.time.ZonedDateTime
import java.time.ZoneId
import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import java.util.Locale


class ChatViewModel (application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {
    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }
    private var tts: TextToSpeech? = null
    var isSpeaking = mutableStateOf(false)

    init {
        // Initialize TextToSpeech with Application context
        tts = TextToSpeech(application.applicationContext, this)
    }

    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-001",
        apiKey = Constants.apikey
    )
    // TextToSpeech initialization
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US // Set the language for speech synthesis
        }
    }

    // Speak the last message in messageList
    fun speakLastMessage() {
        val lastMessage = messageList.lastOrNull()?.message ?: return
        isSpeaking.value = true
        tts?.speak(lastMessage, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    // Stop speaking
    fun stopSpeaking() {
        tts?.stop()
        isSpeaking.value = false
    }
    // Clean up TTS resources when ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
  
    // Function to check if the query is related to date/time
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun handleDateQueries(question: String): String? {
        val lowerCaseQuestion = question.lowercase()

        // Handle age-based query for birth year (e.g., "I am 20 years old, when was I born?")
        val agePattern = Pattern.compile("i am (\\d+) year[s]? old", Pattern.CASE_INSENSITIVE)
        val matcher = agePattern.matcher(lowerCaseQuestion)
        if (matcher.find()) {
            val age = matcher.group(1)?.toIntOrNull()
            if (age != null) {
                val birthYear = LocalDate.now().year - age
                return "If you're $age years old, you were likely born in $birthYear."
            }
        }


        //Handling Time.
        val timeZoneMap = mapOf(
            "new delhi" to "Asia/Kolkata",
            "new york" to "America/New_York",
            "london" to "Europe/London",
            "tokyo" to "Asia/Tokyo",
            "paris" to "Europe/Paris",
            "sydney" to "Australia/Sydney",

        )

        // Handle general "time now" query
        if (lowerCaseQuestion.contains("time now")) {
            return "The current time is: ${
                LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))
            }"
        }

        // Handle specific city time query
        val timeCityRegex = Regex("time in ([a-zA-Z ]+)", RegexOption.IGNORE_CASE)
        val matchResult = timeCityRegex.find(lowerCaseQuestion)

        if (matchResult != null) {
            val city = matchResult.groupValues[1].trim().lowercase(Locale.ROOT)
            val timeZoneId = timeZoneMap[city]

            if (timeZoneId != null) {
                val zonedDateTime = ZonedDateTime.now(ZoneId.of(timeZoneId))
                val formattedTime = zonedDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                return "The current time in $city is $formattedTime."
            } else {
                return "Sorry, I don't have time information for $city."
            }
        }


        //HANDLE YEAR
        if (lowerCaseQuestion.contains("previous year")) {
            return "The previous year was: ${LocalDate.now().minusYears(1).year}"
        }
        if (lowerCaseQuestion.contains("next year",)) {
            return "The previous year was: ${LocalDate.now().plusYears(1).year}"
        }

        if (lowerCaseQuestion.contains("year")) {
            return "The current year is: ${LocalDate.now().year}"
        }


        // Handle weather queries with location
        if (lowerCaseQuestion.contains("weather")) {
            val location = extractLocation(lowerCaseQuestion)
            if (location != null) {
                return getWeather(location) // Call API for specific location
            } else {
                return "Please specify a location for the weather information."
            }
        }


        // Handle today's time
        if (lowerCaseQuestion.contains("time now")) {
            return "The current time is: ${
                LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))
            }"
        }

        // Handle today's date
        if (lowerCaseQuestion.contains("today")) {
            return "Today is: ${LocalDate.now().dayOfWeek}, ${
                LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
            }"
        }

        // Handle tomorrow's day and date

        if (lowerCaseQuestion.contains("day after tomorrow")) {
            return "Day after Tomorrow will be: ${
                LocalDate.now().plusDays(2).dayOfWeek
            }, ${LocalDate.now().plusDays(2).format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))}"
        }
        if (lowerCaseQuestion.contains("tomorrow")) {
            return "Tomorrow will be: ${LocalDate.now().plusDays(1).dayOfWeek}, ${
                LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
            }"
        }

        // Handle yesterday's day and date
        if (lowerCaseQuestion.contains("day before yesterday")) {
            return "Day before Yesterday was: ${
                LocalDate.now().minusDays(2).dayOfWeek
            }, ${LocalDate.now().minusDays(2).format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))}"
        }

        if (lowerCaseQuestion.contains("yesterday")) {
            return "Yesterday was: ${LocalDate.now().minusDays(1).dayOfWeek}, ${
                LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
            }"
        }


        // Handle specific statement like "Tomorrow is Wednesday"
        val dayOfWeekMapping = mapOf(
            "monday" to DayOfWeek.MONDAY,
            "tuesday" to DayOfWeek.TUESDAY,
            "wednesday" to DayOfWeek.WEDNESDAY,
            "thursday" to DayOfWeek.THURSDAY,
            "friday" to DayOfWeek.FRIDAY,
            "saturday" to DayOfWeek.SATURDAY,
            "sunday" to DayOfWeek.SUNDAY
        )

        dayOfWeekMapping.forEach { (dayName, dayOfWeek) ->
            if (lowerCaseQuestion.contains("tomorrow is $dayName")) {
                val tomorrowDay = LocalDate.now().plusDays(1).dayOfWeek
                return if (tomorrowDay == dayOfWeek) {
                    "You're correct! Tomorrow is indeed $dayName."
                } else {
                    "Actually, tomorrow is ${tomorrowDay}."
                }
            }

            if (lowerCaseQuestion.contains("today is $dayName")) {
                val todayDay = LocalDate.now().dayOfWeek
                return if (todayDay == dayOfWeek) {
                    "Yes, today is $dayName."
                } else {
                    "Actually, today is ${todayDay}."
                }
            }
        }

        // Return null if no date-related query is found
        return null
    }


    //WEATHER QUERY
    private fun extractLocation(question: String): String? {

        val locationKeywords = listOf("in", "at","of")

        locationKeywords.forEach { keyword ->
            if (question.contains(keyword)) {
                val words = question.split(" ")
                val locationIndex = words.indexOf(keyword) + 1
                if (locationIndex < words.size) {
                    return words[locationIndex]
                }
            }
        }
        return null
    }


    data class WeatherResponse(
        val main: Main,
        val weather: List<Weather>,
        val name: String
    )

    data class Main(
        val temp: Float,
        val feels_like: Float,
        val humidity: Int
    )

    data class Weather(
        val description: String
    )


    suspend fun getWeather(location: String): String {
        val apiKey = "7e2d0a94800d2752e63d9741cd256d49"
        val urlString =
            "https://api.openweathermap.org/data/2.5/weather?q=$location&appid=$apiKey&units=metric"

        return withContext(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }

                    val gson = Gson()
                    val weatherResponse = gson.fromJson(response, WeatherResponse::class.java)


                    val temperature = weatherResponse.main.temp
                    val description = weatherResponse.weather[0].description
                    val cityName = weatherResponse.name

                    return@withContext "The weather in $cityName is $description with a temperature of $temperature°C, feels like ${weatherResponse.main.feels_like}°C, and humidity is ${weatherResponse.main.humidity}%."

                } else {
                    return@withContext "Unable to fetch weather data for $location."
                }
            } catch (e: Exception) {
                return@withContext "Error fetching weather data: ${e.message}"
            }
        }
    }




    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage(question: String) {
        viewModelScope.launch {
            try {
                tts?.stop()
                // Check if the question is related to date or time
                val dateResponse = handleDateQueries(question)
                if (dateResponse != null) {
                    messageList.add(MessageModel(question, role = "user"))
                    messageList.add(MessageModel(message = dateResponse, role = "model"))
                    return@launch
                }

                // If not a date-related question, proceed with Gemini AI conversation
                val chat = generativeModel.startChat(
                    history = messageList.map {
                        content(it.role) { text(it.message) }
                    }.toList()
                )

                messageList.add(MessageModel(question, "user"))
                messageList.add(MessageModel("Typing...", "model"))

                val response = chat.sendMessage(question)
                messageList.removeLast()
                messageList.add(MessageModel(response.text.toString(), "model"))

            } catch (e: Exception) {
                messageList.removeLast()
                messageList.add(MessageModel("Error: " + e.message.toString(), "model"))

            }

        }
    }

}






