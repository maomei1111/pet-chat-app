package com.maomei.petchatapp.ui.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.maomei.petchatapp.data.model.PetPhoto
import com.maomei.petchatapp.data.photo.SystemPhotoPickerService
import com.maomei.petchatapp.ui.components.LocalPhotoBox

/** 4.5 選択写真確認画面 */
@Composable
fun PhotoConfirmScreen(
    photos: List<PetPhoto>,
    mainPhotoId: String?,
    errorMessage: String?,
    onAddPhotos: (List<android.net.Uri>) -> Unit,
    onDeletePhoto: (PetPhoto) -> Unit,
    onSetMainPhoto: (PetPhoto) -> Unit,
    onNext: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(SystemPhotoPickerService.MAX_PHOTOS)
    ) { uris -> if (uris.isNotEmpty()) onAddPhotos(uris) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "写真を確認してください", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "メイン写真の設定・削除・追加ができます",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(photos, key = { it.id }) { photo ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .border(
                            width = if (photo.id == mainPhotoId) 3.dp else 0.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSetMainPhoto(photo) }
                ) {
                    LocalPhotoBox(
                        path = photo.localPath,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (photo.id == mainPhotoId) {
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
                        onClick = { onDeletePhoto(photo) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "削除",
                            tint = Color.White,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text("写真を追加する")
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("次へ")
        }
    }
}
