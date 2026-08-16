package com.maomei.petchatapp.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maomei.petchatapp.data.model.Personality
import com.maomei.petchatapp.data.model.ReplyLength
import com.maomei.petchatapp.di.AppViewModelFactory
import com.maomei.petchatapp.ui.components.LocalPhotoBox

/** 10. ペット設定画面 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    factory: AppViewModelFactory,
    onBack: () -> Unit,
    onResetComplete: () -> Unit
) {
    val viewModel: SettingsViewModel = viewModel(factory = factory)
    val pet by viewModel.pet.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val photoErrorMessage by viewModel.photoErrorMessage.collectAsState()
    val resetDone by viewModel.resetDone.collectAsState()

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(resetDone) {
        if (resetDone) onResetComplete()
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(50)
    ) { uris -> if (uris.isNotEmpty()) viewModel.addPhotos(uris) }

    val currentPet = pet ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ペット設定") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("ペット名", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = currentPet.name,
                onValueChange = viewModel::updateName,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp),
                singleLine = true
            )

            Text("性格", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)) {
                Personality.candidatesFor(currentPet.species).forEach { candidate ->
                    FilterChip(
                        selected = candidate == currentPet.personality,
                        onClick = { viewModel.updatePersonality(candidate) },
                        label = { Text(candidate.displayName) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Text("飼い主の呼び方", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = currentPet.ownerCallName,
                onValueChange = viewModel::updateOwnerCallName,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp),
                singleLine = true
            )

            Text("一人称", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = currentPet.firstPerson,
                onValueChange = viewModel::updateFirstPerson,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp),
                singleLine = true
            )

            Text("返信の長さ", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)) {
                ReplyLength.entries.forEach { option ->
                    FilterChip(
                        selected = option == currentPet.replyLength,
                        onClick = { viewModel.updateReplyLength(option) },
                        label = { Text(option.displayName) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("絵文字を使用する", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(checked = currentPet.useEmoji, onCheckedChange = viewModel::updateUseEmoji)
            }

            HorizontalDivider(modifier = Modifier.padding(bottom = 20.dp))

            Text("写真", style = MaterialTheme.typography.titleMedium)
            if (photoErrorMessage != null) {
                Text(
                    text = photoErrorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(photos, key = { it.id }) { photo ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .border(
                                width = if (photo.id == currentPet.mainPhotoId) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.setMainPhoto(photo) }
                    ) {
                        LocalPhotoBox(
                            path = photo.localPath,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        if (photo.id == currentPet.mainPhotoId) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "メイン写真",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                                    .size(18.dp)
                                    .align(Alignment.TopStart)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.deletePhoto(photo) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "削除",
                                tint = Color.White,
                                modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
                            )
                        }
                    }
                }
            }
            OutlinedButton(
                onClick = {
                    photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp)
            ) {
                Text("写真を追加する")
            }

            HorizontalDivider(modifier = Modifier.padding(bottom = 20.dp))

            OutlinedButton(
                onClick = { showClearHistoryDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text("会話履歴を削除する")
            }

            Button(
                onClick = { showResetDialog = true },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ペット情報を初期化する")
            }
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("会話履歴を削除しますか？") },
            text = { Text("この操作は取り消せません。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearHistoryDialog = false
                }) { Text("削除する") }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) { Text("キャンセル") }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("ペット情報を初期化しますか？") },
            text = { Text("ペット情報・写真・会話履歴がすべて削除され、最初の登録画面に戻ります。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetPet()
                    showResetDialog = false
                }) { Text("初期化する") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("キャンセル") }
            }
        )
    }
}
