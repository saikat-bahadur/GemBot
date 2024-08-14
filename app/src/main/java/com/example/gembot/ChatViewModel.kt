package com.example.gembot
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch


class ChatViewModel : ViewModel(){
    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }



private val generativeModel : GenerativeModel = GenerativeModel (
         modelName = "gemini-1.5-flash-001",
         apiKey = Constants.apikey,
    )


    fun sendMessage(question : String){
       viewModelScope.launch {
           val chat =generativeModel.startChat()
           messageList.add(MessageModel(question,"user"))
           val response = chat.sendMessage(question)
           messageList.add(MessageModel(response.text.toString(),"model"))
       }
   }
}


