package com.maomei.petchatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.maomei.petchatapp.di.AppViewModelFactory
import com.maomei.petchatapp.ui.navigation.PetChatNavGraph
import com.maomei.petchatapp.ui.theme.PetChatAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as PetChatApplication).container
        val factory = AppViewModelFactory(container)

        setContent {
            PetChatAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PetChatNavGraph(factory = factory)
                }
            }
        }
    }
}
