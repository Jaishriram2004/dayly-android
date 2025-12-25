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

data class ActivityItem(
    val time: String,
    val title: String,
    val completed: Boolean
)


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
    val activities = listOf(
        ActivityItem("06:00 – 07:00", "Morning Gym", false),
        ActivityItem("09:30 – 10:00", "Team Standup", true),
        ActivityItem("14:00 – 15:00", "Project Work", false),
        ActivityItem("21:00 – 21:30", "Reading", false)
    )

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
            Spacer(modifier = Modifier.height(16.dp))

            activities.forEach { activity ->
                ActivityRow(activity)
            }
        }
    }
}

@Composable
fun ActivityRow(item: ActivityItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        // Column 1: Time
        Text(
            text = item.time,
            modifier = Modifier.width(100.dp),
            style = MaterialTheme.typography.bodySmall
        )

        // Column 2: Activity name
        Text(
            text = item.title,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        // Column 3: Checkbox
        Checkbox(
            checked = item.completed,
            onCheckedChange = {}
        )
    }
}
