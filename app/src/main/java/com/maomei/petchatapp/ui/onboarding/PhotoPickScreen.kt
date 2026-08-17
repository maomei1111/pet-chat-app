package com.maomei.petchatapp.ui.onboarding

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maomei.petchatapp.data.model.PetPhoto
import com.maomei.petchatapp.data.photo.SystemPhotoPickerService
import kotlinx.coroutines.flow.StateFlow

/**
 * 5. Googleフォト写真選択画面への遷移
 *
 * [isGooglePhotosAvailable] が true（Google Cloud Console 側のOAuthクライアント登録済み、
 * `google.photos.enabled=true`）なら Google Photos Picker API の正式フローを使う（仕様 7.3）。
 * false の間は Android 標準 System Photo Picker（暫定実装）のみを使う。
 * 正式フローが失敗した場合も標準Photo Pickerへ切り替えられるようにする（仕様 7.7, 10）。
 */
@Composable
fun PhotoPickScreen(
    photosFlow: StateFlow<List<PetPhoto>>,
    isGooglePhotosAvailable: Boolean,
    googlePickerState: StateFlow<GooglePickerUiState>,
    onStartGooglePicker: () -> Unit,
    onAuthorizationResolved: (Intent?) -> Unit,
    onPollForSelection: () -> Unit,
    onDismissGoogleError: () -> Unit,
    onPhotosPicked: (List<Uri>) -> Unit,
    onNext: () -> Unit
) {
    val photos by photosFlow.collectAsState()
    val pickerState by googlePickerState.collectAsState()
    val context = LocalContext.current

    val systemPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(SystemPhotoPickerService.MAX_PHOTOS)
    ) { uris ->
        if (uris.isNotEmpty()) onPhotosPicked(uris)
    }

    val consentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        onAuthorizationResolved(result.data)
    }

    // 同意画面が必要になったら自動的に起動する。
    LaunchedEffect(pickerState) {
        val state = pickerState
        if (state is GooglePickerUiState.NeedsUserConsent) {
            val request = IntentSenderRequest.Builder(state.pendingIntent.intentSender).build()
            consentLauncher.launch(request)
        }
        if (state is GooglePickerUiState.WaitingForSelection) {
            // Googleフォト（またはブラウザ）でpickerUriを開き、その後ポーリングを開始する（仕様 7.3 手順4-5）。
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(state.session.pickerUri)))
            }
            onPollForSelection()
        }
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

        when (val state = pickerState) {
            is GooglePickerUiState.RequestingAuthorization, is GooglePickerUiState.NeedsUserConsent -> {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                Text(
                    text = "Googleアカウントの確認をしています…",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            is GooglePickerUiState.WaitingForSelection -> {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                Text(
                    text = "Googleフォトを開いて、うちの子の写真を選択してください。\n選択した写真だけをアプリ内に保存します。",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            is GooglePickerUiState.Importing -> {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                Text(
                    text = "写真を取り込んでいます…",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            is GooglePickerUiState.Failed -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Button(onClick = onDismissGoogleError, modifier = Modifier.fillMaxWidth()) {
                    Text("端末の写真から選ぶ")
                }
            }

            GooglePickerUiState.Idle -> {
                if (isGooglePhotosAvailable) {
                    Button(onClick = onStartGooglePicker, modifier = Modifier.fillMaxWidth()) {
                        Text("Googleフォトから写真を選ぶ")
                    }
                    TextButton(
                        onClick = {
                            systemPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("端末の写真から選ぶ")
                    }
                } else {
                    Button(
                        onClick = {
                            systemPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Googleフォトから写真を選ぶ")
                    }
                }
            }
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
