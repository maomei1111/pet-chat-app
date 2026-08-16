package com.maomei.petchatapp.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** 4.1 ペット登録開始画面 */
@Composable
fun OnboardingStartScreen(
    onStartRegistration: () -> Unit,
    onSkipForNow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ペットチャット",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "うちの子とお話ししてみよう",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, bottom = 32.dp)
        )
        Button(
            onClick = onStartRegistration,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("うちの子を登録する")
        }
        OutlinedButton(
            onClick = onSkipForNow,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text("あとで設定する")
        }
    }
}
