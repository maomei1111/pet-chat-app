package com.maomei.petchatapp.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maomei.petchatapp.data.model.ChatMessage
import com.maomei.petchatapp.data.model.PetPhoto
import com.maomei.petchatapp.data.model.Sender
import com.maomei.petchatapp.di.AppViewModelFactory
import com.maomei.petchatapp.ui.components.PetMessageIcon

/** 9. メイン会話画面（5章） */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    factory: AppViewModelFactory,
    onOpenSettings: () -> Unit
) {
    val viewModel: ChatViewModel = viewModel(factory = factory)
    val pet by viewModel.pet.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    val mainPhoto = photos.find { it.isMain } ?: photos.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pet?.name ?: "うちの子") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(imageVector = Icons.Filled.Settings, contentDescription = "設定")
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                text = inputText,
                enabled = !isSending,
                onTextChange = viewModel::updateInput,
                onSend = viewModel::send
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                ChatBubble(
                    message = message,
                    photos = photos,
                    mainPhoto = mainPhoto
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage, photos: List<PetPhoto>, mainPhoto: PetPhoto?) {
    val isUser = message.sender == Sender.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            val photo = photos.find { it.id == message.photoId }
            PetMessageIcon(
                photoPath = photo?.localPath,
                mainPhotoPath = mainPhoto?.localPath,
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 6.dp)
            )
        }
        Card(
            modifier = Modifier.widthIn(max = 260.dp),
            shape = RoundedCornerShape(16.dp),
            colors = if (isUser) {
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            } else {
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            }
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    enabled: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("メッセージを入力") },
                enabled = enabled,
                singleLine = true
            )
            IconButton(
                onClick = onSend,
                enabled = enabled && text.isNotBlank()
            ) {
                Icon(imageVector = Icons.Filled.Send, contentDescription = "送信")
            }
        }
    }
}
