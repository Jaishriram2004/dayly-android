package com.example.dayly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// --------------------
// Data model
// --------------------
@kotlinx.serialization.Serializable
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

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activities by remember { mutableStateOf<List<ActivityItem>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }

    // 🔹 LOAD SAVED DATA ON APP START
    LaunchedEffect(Unit) {
        val saved = DaylyDataStore.loadActivities(context)
        activities = if (saved.isNotEmpty()) {
            saved
        } else {
            listOf(
                ActivityItem("06:00 – 07:00", "Morning Gym", false),
                ActivityItem("09:30 – 10:00", "Team Standup", true),
                ActivityItem("14:00 – 15:00", "Project Work", false),
                ActivityItem("21:00 – 21:30", "Reading", false)
            )
        }
    }

    val completedCount = activities.count { it.completed }
    val progress =
        if (activities.isNotEmpty())
            completedCount.toFloat() / activities.size
        else 0f

    val drawerState = rememberDrawerState(DrawerValue.Closed)

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

                Text("Today's Progress", style = MaterialTheme.typography.labelMedium)

                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(8.dp))

                val emoji = when {
                    progress >= 1f -> "🎉"
                    progress >= 0.66f -> "😄"
                    progress >= 0.33f -> "🙂"
                    else -> "😐"
                }

                // Progress bar with emoji at true end
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    val barWidth = maxWidth
                    val safeProgress = progress.coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(barWidth * safeProgress)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFB39DDB),
                                        Color(0xFF7E57C2)
                                    )
                                )
                            )
                    )

                    Text(
                        text = emoji,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(
                                x = (barWidth * safeProgress)
                                    .coerceAtMost(barWidth - 20.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                activities.forEachIndexed { index, activity ->
                    ActivityRow(
                        item = activity,
                        onCheckedChange = { checked ->
                            activities = activities.toMutableList().also {
                                it[index] = it[index].copy(completed = checked)
                            }

                            // 🔹 SAVE ON EVERY CHANGE
                            scope.launch {
                                DaylyDataStore.saveActivities(context, activities)
                            }
                        }
                    )
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

            Text("Dayly", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(24.dp))

            NavigationDrawerItem(
                label = { Text("Add Item") },
                selected = false,
                onClick = onAddItemClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            NavigationDrawerItem(
                label = { Text("Settings", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                selected = false,
                onClick = {}
            )

            NavigationDrawerItem(
                label = { Text("Account", color = MaterialTheme.colorScheme.onSurfaceVariant) },
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
fun ActivityRow(
    item: ActivityItem,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(item.time, modifier = Modifier.width(100.dp))
        Text(item.title, modifier = Modifier.weight(1f).padding(horizontal = 8.dp))

        Checkbox(
            checked = item.completed,
            onCheckedChange = onCheckedChange
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
        text = { Text("Add Item logic comes in next phase") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
