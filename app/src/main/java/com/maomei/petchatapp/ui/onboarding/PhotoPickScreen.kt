package com.maomei.petchatapp.ui.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maomei.petchatapp.data.model.PetPhoto
import com.maomei.petchatapp.data.photo.SystemPhotoPickerService
import kotlinx.coroutines.flow.StateFlow

/**
 * 5. Googleフォト写真選択画面への遷移
 *
 * 本来は Google Photos Picker API を起動する画面だが、実装上の注意（4.4）により、
 * Android 標準 System Photo Picker（PickMultipleVisualMedia）を起動する。
 * ボタン文言は仕様書通り「Googleフォトから写真を選ぶ」のままとする。
 */
@Composable
fun PhotoPickScreen(
    photosFlow: StateFlow<List<PetPhoto>>,
    onPhotosPicked: (List<android.net.Uri>) -> Unit,
    onNext: () -> Unit
) {
    val photos by photosFlow.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(SystemPhotoPickerService.MAX_PHOTOS)
    ) { uris ->
        if (uris.isNotEmpty()) onPhotosPicked(uris)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PhotoLibrary,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "うちの子の写真を選んでください",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "3枚以上がおすすめです（最大50枚）",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = {
                launcher.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Googleフォトから写真を選ぶ")
        }

        Text(
            text = "選択済み: ${photos.size}枚",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp)
        )

        if (photos.isNotEmpty()) {
            TextButton(onClick = onNext, modifier = Modifier.padding(top = 8.dp)) {
                Text("次へ")
            }
        }
    }
}
