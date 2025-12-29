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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

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
// Main App (UI ONLY)
// --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaylyApp() {

    val context = LocalContext.current
    val viewModel: DaylyViewModel = viewModel(
        factory = DaylyViewModelFactory(context)
    )

    val activities by viewModel.activities.collectAsState()
    val progress by viewModel.progress.collectAsState()

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

                Text("Today's Progress", style = MaterialTheme.typography.labelMedium)
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(8.dp))

                val emoji = when {
                    progress >= 1f -> "🎉"
                    progress >= 0.66f -> "😄"
                    progress >= 0.33f -> "🙂"
                    else -> "😐"
                }

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
                            viewModel.toggleActivity(index, checked)
                        }
                    )
                }
            }
        }
    }

    // ✅ FIXED: Dialog now adds item via ViewModel
    if (showAddDialog) {
        AddItemDialog(
            onAdd = { newItem ->
                viewModel.addActivity(newItem)
            },
            onDismiss = { showAddDialog = false }
        )
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
        Text(
            item.title,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )

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
fun AddItemDialog(
    onAdd: (ActivityItem) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Activity") },
        text = {
            Column {
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (e.g. 18:00 – 19:00)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity title") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && time.isNotBlank()) {
                        onAdd(
                            ActivityItem(
                                time = time,
                                title = title,
                                completed = false
                            )
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
