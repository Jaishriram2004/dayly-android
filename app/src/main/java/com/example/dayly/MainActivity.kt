package com.example.dayly

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import java.util.concurrent.TimeUnit

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
    val viewModel: DaylyViewModel = viewModel(
        factory = DaylyViewModelFactory(context)
    )

    val activities by viewModel.activities.collectAsState()
    val progress by viewModel.progress.collectAsState()

    LaunchedEffect(Unit) {
        scheduleDailySummary(context)
    }

    var showAddDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent {
                scope.launch { drawerState.close() }
                showAddDialog = true
            }
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
                                    listOf(Color(0xFFB39DDB), Color(0xFF7E57C2))
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

    if (showAddDialog) {
        AddItemDialog(
            activities = activities,
            onAdd = { viewModel.addActivity(it) },
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

        Text(
            "%02d:%02d – %02d:%02d".format(
                item.startHour,
                item.startMinute,
                item.endHour,
                item.endMinute
            ),
            modifier = Modifier.width(120.dp)
        )

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
// Add Item Dialog (WITH OVERLAP VALIDATION)
// --------------------
@Composable
fun AddItemDialog(
    activities: List<ActivityItem>,
    onAdd: (ActivityItem) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }

    var sh by remember { mutableStateOf(9) }
    var sm by remember { mutableStateOf(0) }
    var eh by remember { mutableStateOf(10) }
    var em by remember { mutableStateOf(0) }

    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Activity") },
        text = {
            Column {

                Text("Start Time")
                TimeRow(sh, sm, hasError) {
                    sh = it.first
                    sm = it.second
                }

                Spacer(Modifier.height(12.dp))

                Text("End Time")
                TimeRow(eh, em, hasError) {
                    eh = it.first
                    em = it.second
                }

                if (hasError) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Time should not overlap",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity title") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val overlap = isOverlapping(sh, sm, eh, em, activities)

                if (title.isBlank() || overlap) {
                    hasError = overlap
                    return@TextButton
                }

                onAdd(
                    ActivityItem(
                        title = title,
                        completed = false,
                        startHour = sh,
                        startMinute = sm,
                        endHour = eh,
                        endMinute = em
                    )
                )
                onDismiss()
            }) {
                Text("Add")
            }
        }
    )
}

// --------------------
// Time Row with Error Border
// --------------------
@Composable
fun TimeRow(
    hour: Int,
    minute: Int,
    hasError: Boolean,
    onChange: (Pair<Int, Int>) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (hasError) Color.Red else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(4.dp)
    ) {
        NumberDropdown("Hour", 0..23, hour) { onChange(it to minute) }
        NumberDropdown("Min", 0..59, minute) { onChange(hour to it) }
    }
}

// --------------------
// Overlap Logic
// --------------------
fun isOverlapping(
    sh: Int,
    sm: Int,
    eh: Int,
    em: Int,
    existing: List<ActivityItem>
): Boolean {
    val newStart = sh * 60 + sm
    val newEnd = eh * 60 + em

    return existing.any {
        val start = it.startHour * 60 + it.startMinute
        val end = it.endHour * 60 + it.endMinute
        newStart < end && newEnd > start
    }
}

// --------------------
// Dropdown Component
// --------------------
@Composable
fun NumberDropdown(
    label: String,
    range: IntRange,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedButton(onClick = { expanded = true }) {
            Text("$label: ${"%02d".format(selected)}")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            range.forEach { value ->
                DropdownMenuItem(
                    text = { Text("%02d".format(value)) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

// --------------------
// Notification Scheduler
// --------------------
fun scheduleDailySummary(context: Context) {
    val workRequest =
        androidx.work.PeriodicWorkRequestBuilder<DailySummaryWorker>(
            1, TimeUnit.DAYS
        ).build()

    androidx.work.WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(
            "daily_summary",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
}
