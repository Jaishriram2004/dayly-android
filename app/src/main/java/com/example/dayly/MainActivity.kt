package com.example.dayly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// --------------------
// Data model
// --------------------
data class ActivityItem(
    val time: String,
    val title: String,
    val completed: Boolean
)

// --------------------
// Activity
// --------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DaylyApp()
        }
    }
}

// --------------------
// Main App
// --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaylyApp() {

    val activities = listOf(
        ActivityItem("06:00 – 07:00", "Morning Gym", false),
        ActivityItem("09:30 – 10:00", "Team Standup", true),
        ActivityItem("14:00 – 15:00", "Project Work", false),
        ActivityItem("21:00 – 21:30", "Reading", false)
    )

    var showAddDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onAddItemClick = {
                    scope.launch { drawerState.close() }
                    showAddDialog = true
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Today") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
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
                    progress = { 0.0f },
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

    if (showAddDialog) {
        AddItemDialog(onDismiss = { showAddDialog = false })
    }
}

// --------------------
// Drawer
// --------------------
@Composable
fun DrawerContent(onAddItemClick: () -> Unit) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {

            Text(
                text = "Dayly",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            NavigationDrawerItem(
                label = { Text("Add Item") },
                selected = false,
                onClick = onAddItemClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            NavigationDrawerItem(
                label = {
                    Text(
                        "Settings",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                selected = false,
                onClick = {}
            )

            NavigationDrawerItem(
                label = {
                    Text(
                        "Account",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                selected = false,
                onClick = {}
            )
        }
    }
}


// --------------------
// Activity Row
// --------------------
@Composable
fun ActivityRow(item: ActivityItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = item.time,
            modifier = Modifier.width(100.dp),
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = item.title,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        Checkbox(
            checked = item.completed,
            onCheckedChange = {}
        )
    }
}

// --------------------
// Add Item Dialog
// --------------------
@Composable
fun AddItemDialog(onDismiss: () -> Unit) {

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Activity") },
        text = {
            Column {

                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Activity description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Time")

                Row {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Start") },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("End") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Days")

                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

                days.forEach { day ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = false, onCheckedChange = {})
                        Text(day)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Add Activity")
            }
        }
    )
}
