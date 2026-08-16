package com.maomei.petchatapp.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 4.3 ペット情報入力画面 */
@Composable
fun PetInfoInputScreen(
    name: String,
    ownerCallName: String,
    firstPerson: String,
    onNameChange: (String) -> Unit,
    onOwnerCallNameChange: (String) -> Unit,
    onFirstPersonChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "ペットの情報を教えてください", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "すべて任意項目です。未入力の場合はデフォルト値が使われます。",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("ペット名（未入力: うちの子）") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

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
                .padding(bottom = 16.dp),
            singleLine = true
        )

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
