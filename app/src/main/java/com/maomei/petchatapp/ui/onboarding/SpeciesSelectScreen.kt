package com.maomei.petchatapp.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maomei.petchatapp.data.model.PetSpecies

/** 4.2 ペット種類選択画面 */
@Composable
fun SpeciesSelectScreen(
    selected: PetSpecies,
    onSelect: (PetSpecies) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "どちらを登録しますか？", style = MaterialTheme.typography.titleLarge)

        Column(modifier = Modifier.padding(top = 24.dp)) {
            listOf(PetSpecies.DOG to "犬", PetSpecies.CAT to "猫").forEach { (species, label) ->
                val isSelected = species == selected
                if (isSelected) {
                    Button(
                        onClick = { onSelect(species) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(vertical = 6.dp)
                    ) { Text(label, style = MaterialTheme.typography.titleMedium) }
                } else {
                    OutlinedButton(
                        onClick = { onSelect(species) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(vertical = 6.dp)
                    ) { Text(label, style = MaterialTheme.typography.titleMedium) }
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("次へ")
        }
    }
}
