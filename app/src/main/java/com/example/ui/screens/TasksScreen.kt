package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TaskCategory
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TaskPriority
import com.example.data.local.entity.TaskStatus
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BusinessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val categoryFilter by viewModel.taskCategoryFilter.collectAsState()
    val isDecomposing by viewModel.isDecomposingGoal.collectAsState()
    val decompositionResult by viewModel.decompositionResult.collectAsState()
    val isAnalyzingBottlenecks by viewModel.isAnalyzingBottlenecks.collectAsState()
    val bottleneckText by viewModel.bottleneckText.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showDecomposeDialog by remember { mutableStateOf(false) }
    var showBottleneckDialog by remember { mutableStateOf(false) }

    var priorityFilter by remember { mutableStateOf<TaskPriority?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredTasks = remember(tasks, categoryFilter, priorityFilter, searchQuery) {
        tasks.filter { task ->
            (categoryFilter == null || task.category == categoryFilter) &&
            (priorityFilter == null || task.priority == priorityFilter) &&
            (searchQuery.isBlank() || task.title.contains(searchQuery, ignoreCase = true) || task.description.contains(searchQuery, ignoreCase = true))
        }
    }

    val totalHours = remember(tasks) {
        tasks.filter { it.status != TaskStatus.DONE }.sumOf { it.estimatedHours }
    }
    val urgentCount = remember(tasks) {
        tasks.count { it.priority == TaskPriority.URGENT_IMPORTANT && it.status != TaskStatus.DONE }
    }
    val inProgressCount = remember(tasks) {
        tasks.count { it.status == TaskStatus.IN_PROGRESS }
    }
    val doneCount = remember(tasks) {
        tasks.count { it.status == TaskStatus.DONE }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddTaskDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Новая задача", fontWeight = FontWeight.Bold) },
                containerColor = BluePrimary,
                contentColor = Color.White
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
        ) {
            // Metrics row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "В работе",
                        value = inProgressCount.toString(),
                        subtitle = "Всего: ${tasks.size}",
                        icon = Icons.Filled.PlayArrow,
                        accentColor = BluePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Срочные (Q1)",
                        value = urgentCount.toString(),
                        subtitle = "Фокус дня",
                        icon = Icons.Filled.PriorityHigh,
                        accentColor = RoseExpense,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "В плане",
                        value = "${"%.1f".format(totalHours)} ч.",
                        subtitle = "Готово: $doneCount",
                        icon = Icons.Filled.Schedule,
                        accentColor = EmeraldProfit,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // AI Goal Decomposer banner
            item {
                AiActionBanner(
                    title = "ИИ-Декомпозиция целей",
                    subtitle = "Превратите абстрактную бизнес-цель в пошаговый план",
                    buttonText = "Декомпозировать",
                    icon = Icons.Filled.AccountTree,
                    isLoading = isDecomposing,
                    onClick = { showDecomposeDialog = true }
                )
            }

            // Bottleneck Detection button
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.analyzeBottlenecks()
                            showBottleneckDialog = true
                        },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Insights,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Анализ узких мест бизнеса (ИИ-аудит задач)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Category Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Отделы компании",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = categoryFilter == null,
                                onClick = { viewModel.setCategoryFilter(null) },
                                label = { Text("Все (${tasks.size})") },
                                leadingIcon = if (categoryFilter == null) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                        items(TaskCategory.values()) { category ->
                            val count = tasks.count { it.category == category }
                            FilterChip(
                                selected = categoryFilter == category,
                                onClick = {
                                    viewModel.setCategoryFilter(if (categoryFilter == category) null else category)
                                },
                                label = { Text("${category.displayName} ($count)") }
                            )
                        }
                    }
                }
            }

            // Priority Matrix Switcher
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(
                        onClick = { priorityFilter = null },
                        label = { Text("Все приоритеты", fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (priorityFilter == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    )
                    SuggestionChip(
                        onClick = { priorityFilter = TaskPriority.URGENT_IMPORTANT },
                        label = { Text("🔥 Срочно & Важно", fontSize = 12.sp, color = RoseExpense) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (priorityFilter == TaskPriority.URGENT_IMPORTANT) RoseExpense.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                        )
                    )
                    SuggestionChip(
                        onClick = { priorityFilter = TaskPriority.IMPORTANT_NOT_URGENT },
                        label = { Text("🎯 Стратегическое (Q2)", fontSize = 12.sp, color = BluePrimary) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (priorityFilter == TaskPriority.IMPORTANT_NOT_URGENT) BluePrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                        )
                    )
                    SuggestionChip(
                        onClick = { priorityFilter = TaskPriority.URGENT_NOT_IMPORTANT },
                        label = { Text("👥 Делегировать (Q3)", fontSize = 12.sp, color = AmberWarning) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (priorityFilter == TaskPriority.URGENT_NOT_IMPORTANT) AmberWarning.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            // Tasks List
            if (filteredTasks.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Outlined.Assignment,
                        title = "Задач пока нет",
                        description = "Создайте новую задачу или воспользуйтесь ИИ-декомпозицией цели",
                        actionButtonText = "Добавить задачу",
                        onActionClick = { showAddTaskDialog = true }
                    )
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskCardItem(
                        task = task,
                        onStatusChange = { newStatus -> viewModel.updateTaskStatus(task, newStatus) },
                        onDelete = { viewModel.deleteTask(task.id) }
                    )
                }
            }
        }
    }

    // --- Dialog: Add Task ---
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, desc, cat, prio, hours ->
                viewModel.addTask(title, desc, cat, prio, hours)
                showAddTaskDialog = false
            }
        )
    }

    // --- Dialog: AI Goal Decomposition ---
    if (showDecomposeDialog) {
        AiGoalDecomposeDialog(
            isGenerating = isDecomposing,
            result = decompositionResult,
            onDismiss = {
                showDecomposeDialog = false
                viewModel.clearDecompositionResult()
            },
            onGenerate = { goal, cat, prio ->
                viewModel.decomposeGoalWithAi(goal, cat, prio)
            },
            onApply = { title, cat, prio, text ->
                viewModel.applyDecompositionAsTask(title, cat, prio, text)
                showDecomposeDialog = false
            }
        )
    }

    // --- Dialog: Bottleneck Analysis ---
    if (showBottleneckDialog && (bottleneckText != null || isAnalyzingBottlenecks)) {
        AlertDialog(
            onDismissRequest = {
                showBottleneckDialog = false
                viewModel.clearBottleneckAnalysis()
            },
            icon = { Icon(Icons.Filled.Insights, contentDescription = null, tint = AmberWarning) },
            title = { Text("ИИ-Аудит узких мест бизнеса") },
            text = {
                if (isAnalyzingBottlenecks) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BluePrimary)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = bottleneckText ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showBottleneckDialog = false
                    viewModel.clearBottleneckAnalysis()
                }) {
                    Text("Понятно, принять к сведению")
                }
            }
        )
    }
}

@Composable
fun TaskCardItem(
    task: TaskEntity,
    onStatusChange: (TaskStatus) -> Unit,
    onDelete: () -> Unit
) {
    val isDone = task.status == TaskStatus.DONE
    val priorityColor = when (task.priority) {
        TaskPriority.URGENT_IMPORTANT -> RoseExpense
        TaskPriority.IMPORTANT_NOT_URGENT -> BluePrimary
        TaskPriority.URGENT_NOT_IMPORTANT -> AmberWarning
        TaskPriority.NOT_URGENT_NOT_IMPORTANT -> Slate400
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isDone) 0.dp else 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (task.priority == TaskPriority.URGENT_IMPORTANT && !isDone) RoseExpense.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Checkbox / Status toggle
                IconButton(
                    onClick = {
                        val nextStatus = if (isDone) TaskStatus.TODO else TaskStatus.DONE
                        onStatusChange(nextStatus)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isDone) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = "Статус",
                        tint = if (isDone) EmeraldProfit else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Удалить",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tags & Metadata
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Category Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = task.category.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }

                        // Priority Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(priorityColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = task.priority.displayName.substringBefore(" ("),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = priorityColor,
                                fontSize = 10.sp
                            )
                        }

                        // Hours
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${task.estimatedHours} ч.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }

                        if (task.isAiGenerated) {
                            AiBadge(text = "ИИ-план", modifier = Modifier.padding(start = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, category: TaskCategory, priority: TaskPriority, hours: Double) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.SALES) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.IMPORTANT_NOT_URGENT) }
    var hoursText by remember { mutableStateOf("1.5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая бизнес-задача") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Что нужно сделать") },
                    placeholder = { Text("Например: Подготовить презентацию для инвестора") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Подробности и ожидаемый результат") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Text("Отдел компании:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskCategory.values().forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Приоритет (Эйзенхауэр):", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TaskPriority.values().forEach { prio ->
                        FilterChip(
                            selected = selectedPriority == prio,
                            onClick = { selectedPriority = prio },
                            label = { Text(prio.displayName.substringBefore(" ("), fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = hoursText,
                    onValueChange = { hoursText = it },
                    label = { Text("Оценка времени (часов)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val hours = hoursText.toDoubleOrNull() ?: 1.0
                        onConfirm(title, description, selectedCategory, selectedPriority, hours)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
fun AiGoalDecomposeDialog(
    isGenerating: Boolean,
    result: String?,
    onDismiss: () -> Unit,
    onGenerate: (goal: String, category: TaskCategory, priority: TaskPriority) -> Unit,
    onApply: (title: String, category: TaskCategory, priority: TaskPriority, text: String) -> Unit
) {
    var goalTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.MARKETING) }
    var selectedPriority by remember { mutableStateOf(TaskPriority.URGENT_IMPORTANT) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.AccountTree, contentDescription = null, tint = PurpleAI) },
        title = { Text("ИИ-Декомпозиция целей") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (result == null) {
                    Text(
                        text = "Опишите крупную стратегическую цель. ИИ разобьет её на пошаговые задачи, определит узкие места и сроки.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        label = { Text("Цель или проект") },
                        placeholder = { Text("Например: Открыть филиал в новом районе или запустить онлайн-продажи") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Text("Отдел:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TaskCategory.values().forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "План декомпозиции готов:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(
                            onClick = { copyToClipboard(context, result) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Копировать", modifier = Modifier.size(18.dp))
                        }
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (result == null) {
                Button(
                    onClick = {
                        if (goalTitle.isNotBlank()) {
                            onGenerate(goalTitle, selectedCategory, selectedPriority)
                        }
                    },
                    enabled = goalTitle.isNotBlank() && !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Сформировать план")
                    }
                }
            } else {
                Button(
                    onClick = {
                        onApply(goalTitle.ifBlank { "Реализация проекта" }, selectedCategory, selectedPriority, result)
                    }
                ) {
                    Text("Создать задачу в списке")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}
