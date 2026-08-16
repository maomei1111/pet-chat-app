package com.maomei.petchatapp.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maomei.petchatapp.data.model.PetSpecies
import com.maomei.petchatapp.data.model.Personality
import com.maomei.petchatapp.data.model.ReplyLength

/** 4.6 性格・口調設定画面 */
@Composable
fun PersonalitySettingsScreen(
    species: PetSpecies,
    personality: Personality,
    replyLength: ReplyLength,
    useEmoji: Boolean,
    ownerCallName: String,
    firstPerson: String,
    onPersonalityChange: (Personality) -> Unit,
    onReplyLengthChange: (ReplyLength) -> Unit,
    onUseEmojiChange: (Boolean) -> Unit,
    onOwnerCallNameChange: (String) -> Unit,
    onFirstPersonChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(text = "性格・口調を設定してください", style = MaterialTheme.typography.titleLarge)

        Text(
            text = "性格",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Personality.candidatesFor(species).forEach { candidate ->
                FilterChip(
                    selected = candidate == personality,
                    onClick = { onPersonalityChange(candidate) },
                    label = { Text(candidate.displayName) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

        Text(text = "返信の長さ", style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.padding(top = 8.dp)) {
            ReplyLength.entries.forEach { option ->
                FilterChip(
                    selected = option == replyLength,
                    onClick = { onReplyLengthChange(option) },
                    label = { Text(option.displayName) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "絵文字を使用する", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Switch(checked = useEmoji, onCheckedChange = onUseEmojiChange)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

        OutlinedTextField(
            value = ownerCallName,
            onValueChange = onOwnerCallNameChange,
            label = { Text("飼い主の呼び方（未入力: きみ）") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = firstPerson,
            onValueChange = onFirstPersonChange,
            label = { Text("ペットからの一人称") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            singleLine = true
        )

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("次へ")
        }
    }
}
