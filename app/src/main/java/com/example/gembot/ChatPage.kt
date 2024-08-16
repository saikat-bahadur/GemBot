package com.example.gembot

import android.telephony.TelephonyCallback.MessageWaitingIndicatorListener
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gembot.ui.theme.colorModelMessage
import com.example.gembot.ui.theme.colorUserMessage


//Chat Page
@Composable
fun ChatPage(modifier: Modifier =Modifier,viewModel: ChatViewModel){
    Column(
        modifier=modifier
    ){
        AppHeader()

        MessageList(modifier=Modifier.weight(.1f),
                    messageList=viewModel.messageList)

        MessageInput(onMessageSend = {
            viewModel.sendMessage(it)
        })

    }
}

//Message List Or Model
@Composable
fun MessageList(modifier:Modifier=Modifier,messageList:List<MessageModel>){
    LazyColumn(
        modifier=modifier,
        reverseLayout = true

        ) {
        items(messageList.reversed()){
            MessageRow(messageModel = it)
        }
    }
}



//interface is updated
@Composable
fun MessageRow(messageModel: MessageModel){
    val isModel=messageModel.role=="model"
    Row(
        verticalAlignment = Alignment.CenterVertically
    ){
        Box(
           modifier = Modifier.fillMaxWidth()
        ){
            Box(
                modifier = Modifier.align(if(isModel) Alignment.BottomStart else Alignment.BottomEnd)
                    .padding(
                        start=if(isModel) 8.dp else 70.dp,
                        end=if(isModel) 70.dp else 8.dp,
                        top=8.dp,
                        bottom = 8.dp,

                    )
                    .clip(RoundedCornerShape(48f))
                    .background(if(isModel) colorUserMessage else colorModelMessage)
                    .padding(16.dp)

            ){
                Text(
                    text = messageModel.message,
                    color=Color.White,
                    fontWeight = FontWeight.W500 )
            }
        }
    }
}



//Message Input -- Text Box And send Symbol
@Composable
fun MessageInput(onMessageSend : (String)-> Unit){
    var message by remember{
        mutableStateOf("")
    }

    Row(
        modifier=Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,

    ){
        OutlinedTextField(
            modifier=Modifier.weight(1f),
            value = message,
            onValueChange ={
                message=it
            } )

        IconButton(onClick={
            onMessageSend(message)
            message=""
        }){

            Icon(
                imageVector= Icons.Default.Send,
                contentDescription="send"
            )

        }
    }

}



//Header
@Composable
fun AppHeader(){
    Box(modifier= Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.tertiary)
    ){
        Text(
            modifier=Modifier.padding(16.dp),
            text="GemBot",
            color= Color.White,
            fontSize=22.sp
        )

    }
}


