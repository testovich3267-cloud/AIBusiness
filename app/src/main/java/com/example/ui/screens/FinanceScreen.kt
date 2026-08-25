package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.FinanceCategory
import com.example.data.local.entity.FinanceEntity
import com.example.data.local.entity.TransactionType
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BusinessViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val records by viewModel.financeRecords.collectAsState()
    val summary by viewModel.financialSummary.collectAsState()
    val isGeneratingAudit by viewModel.isGeneratingFinancialAudit.collectAsState()
    val auditReport by viewModel.financialAuditReport.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showAuditDialog by remember { mutableStateOf(false) }
    var selectedFilterType by remember { mutableStateOf<TransactionType?>(null) }

    val context = LocalContext.current

    val filteredRecords = remember(records, selectedFilterType) {
        if (selectedFilterType == null) records else records.filter { it.type == selectedFilterType }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Запись", fontWeight = FontWeight.Bold) },
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
            // Main Metrics
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Выручка (Доходы)",
                            value = "${"%,.0f".format(summary.totalIncome)} ₽",
                            subtitle = "Всего поступлений",
                            icon = Icons.Filled.ArrowUpward,
                            accentColor = EmeraldProfit,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Расходы (Burn)",
                            value = "${"%,.0f".format(summary.totalExpense)} ₽",
                            subtitle = "Главное: ${summary.topExpenseCategory}",
                            icon = Icons.Filled.ArrowDownward,
                            accentColor = RoseExpense,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Чистая прибыль",
                            value = "${"%,.0f".format(summary.netProfit)} ₽",
                            subtitle = "Маржинальность: ${"%.1f".format(summary.marginPercent)}%",
                            icon = Icons.Filled.AccountBalanceWallet,
                            accentColor = if (summary.netProfit >= 0) EmeraldProfit else RoseExpense,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Runway (Подушка)",
                            value = "${"%.1f".format(summary.runwayMonths)} мес.",
                            subtitle = "Запас ликвидности",
                            icon = Icons.Filled.Shield,
                            accentColor = AmberWarning,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // AI CFO Audit Action Banner
            item {
                AiActionBanner(
                    title = "ИИ-Аудит Финдиректора (CFO)",
                    subtitle = "Анализ юнит-экономики, структуры затрат и точки безубыточности",
                    buttonText = "Провести аудит",
                    icon = Icons.Filled.QueryStats,
                    isLoading = isGeneratingAudit,
                    onClick = {
                        viewModel.generateFinancialAudit()
                        showAuditDialog = true
                    }
                )
            }

            // Expense Breakdown Visualizer
            if (summary.expenseByCategory.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Структура расходов компании",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val totalExp = summary.totalExpense.coerceAtLeast(1.0)
                            summary.expenseByCategory.entries.sortedByDescending { it.value }.take(5).forEach { entry ->
                                val percent = ((entry.value / totalExp) * 100.0).toFloat()
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(entry.key.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                        Text("${"%,.0f".format(entry.value)} ₽ (${"%.0f".format(percent.toDouble())}%)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { percent / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = BluePrimary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Transaction Filter Chips
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilterType == null,
                        onClick = { selectedFilterType = null },
                        label = { Text("Все операции (${records.size})") }
                    )
                    FilterChip(
                        selected = selectedFilterType == TransactionType.INCOME,
                        onClick = { selectedFilterType = TransactionType.INCOME },
                        label = { Text("🟢 Доходы", color = EmeraldProfit) }
                    )
                    FilterChip(
                        selected = selectedFilterType == TransactionType.EXPENSE,
                        onClick = { selectedFilterType = TransactionType.EXPENSE },
                        label = { Text("🔴 Расходы", color = RoseExpense) }
                    )
                }
            }

            // Transactions Ledger
            if (filteredRecords.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Outlined.ReceiptLong,
                        title = "Транзакций пока нет",
                        description = "Внесите доходы или расходы компании для расчета ключевых показателей",
                        actionButtonText = "Добавить запись",
                        onActionClick = { showAddDialog = true }
                    )
                }
            } else {
                items(filteredRecords, key = { it.id }) { record ->
                    FinanceRecordCard(
                        record = record,
                        onDelete = { viewModel.deleteFinanceRecord(record.id) }
                    )
                }
            }
        }
    }

    // --- Dialog: Add Finance Record ---
    if (showAddDialog) {
        AddFinanceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, amount, type, category, note ->
                viewModel.addFinanceRecord(title, amount, type, category, note)
                showAddDialog = false
            }
        )
    }

    // --- Dialog: CFO Audit Report ---
    if (showAuditDialog && (auditReport != null || isGeneratingAudit)) {
        AlertDialog(
            onDismissRequest = {
                showAuditDialog = false
                viewModel.clearFinancialAuditReport()
            },
            icon = { Icon(Icons.Filled.QueryStats, contentDescription = null, tint = PurpleAI) },
            title = { Text("Управленческий отчет CFO") },
            text = {
                if (isGeneratingAudit) {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BluePrimary)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = auditReport ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp
                        )
                    }
                }
            },
            confirmButton = {
                Row {
                    if (auditReport != null) {
                        TextButton(onClick = { copyToClipboard(context, auditReport ?: "") }) {
                            Text("Копировать")
                        }
                    }
                    Button(onClick = {
                        showAuditDialog = false
                        viewModel.clearFinancialAuditReport()
                    }) {
                        Text("Принять")
                    }
                }
            }
        )
    }
}

@Composable
fun FinanceRecordCard(
    record: FinanceEntity,
    onDelete: () -> Unit
) {
    val isIncome = record.type == TransactionType.INCOME
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale("ru")) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isIncome) EmeraldProfit.copy(alpha = 0.15f) else RoseExpense.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        contentDescription = null,
                        tint = if (isIncome) EmeraldProfit else RoseExpense,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = record.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${record.category.displayName} • ${dateFormat.format(Date(record.date))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (isIncome) "+" else "-"}${"%,.0f".format(record.amount)} ₽",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isIncome) EmeraldProfit else RoseExpense
                    )
                )

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFinanceDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, type: TransactionType, category: FinanceCategory, note: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf(FinanceCategory.PAYROLL) }
    var note by remember { mutableStateOf("") }

    val categories = remember(selectedType) {
        FinanceCategory.values().filter { it.isIncome == (selectedType == TransactionType.INCOME) }
    }

    LaunchedEffect(selectedType) {
        selectedCategory = categories.firstOrNull() ?: FinanceCategory.OTHER_EXPENSE
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Финансовая запись") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = { selectedType = TransactionType.INCOME },
                        label = { Text("🟢 Доход") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = { selectedType = TransactionType.EXPENSE },
                        label = { Text("🔴 Расход") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Назначение платежа *") },
                    placeholder = { Text("Например: Оплата поставщику или счет от клиента") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Сумма (₽) *") },
                    placeholder = { Text("50000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Категория:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Примечание (необязательно)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amount > 0) {
                        onConfirm(title, amount, selectedType, selectedCategory, note)
                    }
                },
                enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
