package com.maomei.petchatapp.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maomei.petchatapp.data.model.PetSpecies
import com.maomei.petchatapp.data.model.Personality
import com.maomei.petchatapp.data.model.ReplyLength
import com.maomei.petchatapp.ui.components.PetRoundIcon

/** 4.7 登録内容確認画面 */
@Composable
fun ConfirmationScreen(
    mainPhotoPath: String?,
    name: String,
    species: PetSpecies,
    personality: Personality,
    replyLength: ReplyLength,
    useEmoji: Boolean,
    ownerCallName: String,
    firstPerson: String,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "登録内容を確認してください", style = MaterialTheme.typography.titleLarge)

        Row(
            modifier = Modifier.padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PetRoundIcon(path = mainPhotoPath, modifier = Modifier.size(72.dp))
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(text = name.ifBlank { "うちの子" }, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = if (species == PetSpecies.DOG) "犬" else "猫",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        HorizontalDivider()

        ConfirmRow(label = "性格", value = personality.displayName)
        ConfirmRow(label = "返信の長さ", value = replyLength.displayName)
        ConfirmRow(label = "絵文字", value = if (useEmoji) "使用する" else "使用しない")
        ConfirmRow(label = "飼い主の呼び方", value = ownerCallName.ifBlank { "きみ" })
        ConfirmRow(label = "一人称", value = firstPerson.ifBlank { OnboardingViewModel.defaultFirstPerson(species) })

        Column(modifier = Modifier.padding(top = 32.dp)) {
            Button(
                onClick = onConfirm,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSaving) "登録中..." else "この設定で始める")
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text("戻る")
            }
        }
    }
}

@Composable
private fun ConfirmRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.size(width = 120.dp, height = 20.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
