package com.example.dayly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DaylyApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun DaylyApp() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            Text(
                text = "Today's Progress",
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = 0.0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
