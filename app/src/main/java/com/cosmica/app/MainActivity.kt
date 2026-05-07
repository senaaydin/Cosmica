package com.cosmica.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cosmica.app.presentation.navigation.CosmicaNavGraph
import com.cosmica.app.presentation.theme.CosmikaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CosmikaTheme {
                CosmicaNavGraph()
            }
        }
    }
}
