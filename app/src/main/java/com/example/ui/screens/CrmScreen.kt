package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CrmLeadEntity
import com.example.data.local.entity.DealStage
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BusinessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val leads by viewModel.leads.collectAsState()
    val crmSummary by viewModel.crmSummary.collectAsState()
    val stageFilter by viewModel.stageFilter.collectAsState()
    val isAnalyzingLead by viewModel.isAnalyzingLead.collectAsState()
    val leadAdviceResult by viewModel.leadAdviceResult.collectAsState()

    var showAddLeadDialog by remember { mutableStateOf(false) }
    var showIntegrationDialog by remember { mutableStateOf(false) }
    var selectedLeadForAi by remember { mutableStateOf<CrmLeadEntity?>(null) }

    val context = LocalContext.current

    val filteredLeads = remember(leads, stageFilter) {
        if (stageFilter == null) leads else leads.filter { it.stage == stageFilter }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddLeadDialog = true },
                icon = { Icon(Icons.Filled.PersonAdd, contentDescription = null) },
                text = { Text("Новая сделка", fontWeight = FontWeight.Bold) },
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
            // CRM Summary Metrics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "В пайплайне",
                        value = "${"%,.0f".format(crmSummary.totalPipelineValue)} ₽",
                        subtitle = "${crmSummary.activeDealsCount} активных сделок",
                        icon = Icons.Filled.TrendingUp,
                        accentColor = BluePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Выиграно",
                        value = "${"%,.0f".format(crmSummary.wonDealsValue)} ₽",
                        subtitle = "Ср. конверсия: ${crmSummary.averageProbability}%",
                        icon = Icons.Filled.EmojiEvents,
                        accentColor = EmeraldProfit,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Integration & Export Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showIntegrationDialog = true },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BlueAccent.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BluePrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Sync,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Интеграция с CRM & Экспорт",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Синхронизация Bitrix24, amoCRM, 1С, Webhooks",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Stage Filters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Воронка продаж (Этапы)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = stageFilter == null,
                                onClick = { viewModel.setStageFilter(null) },
                                label = { Text("Все (${leads.size})") }
                            )
                        }
                        items(DealStage.values()) { stage ->
                            val count = leads.count { it.stage == stage }
                            FilterChip(
                                selected = stageFilter == stage,
                                onClick = {
                                    viewModel.setStageFilter(if (stageFilter == stage) null else stage)
                                },
                                label = { Text("${stage.displayName} ($count)") }
                            )
                        }
                    }
                }
            }

            // Deals List
            if (filteredLeads.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Outlined.ContactPage,
                        title = "Сделок на этом этапе нет",
                        description = "Добавьте первого клиента или измените фильтр воронки",
                        actionButtonText = "Добавить сделку",
                        onActionClick = { showAddLeadDialog = true }
                    )
                }
            } else {
                items(filteredLeads, key = { it.id }) { lead ->
                    CrmLeadCard(
                        lead = lead,
                        onStageChange = { newStage -> viewModel.updateLeadStage(lead, newStage) },
                        onAiAnalyze = {
                            selectedLeadForAi = lead
                            viewModel.analyzeLeadWithAi(lead)
                        },
                        onDelete = { viewModel.deleteLead(lead.id) },
                        onCall = {
                            if (lead.phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${lead.phone}"))
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }
        }
    }

    // --- Dialog: Add Lead ---
    if (showAddLeadDialog) {
        AddLeadDialog(
            onDismiss = { showAddLeadDialog = false },
            onConfirm = { name, company, phone, tg, value, stage, notes ->
                viewModel.addLead(name, company, phone, tg, value, stage, notes)
                showAddLeadDialog = false
            }
        )
    }

    // --- Dialog: AI Deal Scoring & Advice ---
    if (selectedLeadForAi != null) {
        AlertDialog(
            onDismissRequest = {
                selectedLeadForAi = null
                viewModel.clearLeadAdvice()
            },
            icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = PurpleAI) },
            title = { Text("ИИ-Скоринг: ${selectedLeadForAi?.clientName}") },
            text = {
                if (isAnalyzingLead) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
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
                            text = leadAdviceResult ?: selectedLeadForAi?.aiRecommendation ?: "Анализ формируется...",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp
                        )
                    }
                }
            },
            confirmButton = {
                Row {
                    if (leadAdviceResult != null) {
                        TextButton(onClick = {
                            copyToClipboard(context, leadAdviceResult ?: "")
                        }) {
                            Text("Копировать скрипт")
                        }
                    }
                    Button(onClick = {
                        selectedLeadForAi = null
                        viewModel.clearLeadAdvice()
                    }) {
                        Text("Закрыть")
                    }
                }
            }
        )
    }

    // --- Dialog: CRM Integration & Export ---
    if (showIntegrationDialog) {
        CrmIntegrationDialog(
            leadsCount = leads.size,
            onDismiss = { showIntegrationDialog = false },
            onExportCsv = {
                val csvContent = buildString {
                    append("Имя;Компания;Телефон;Telegram;Сумма;Этап;Вероятность;Заметки\n")
                    leads.forEach {
                        append("${it.clientName};${it.companyName};${it.phone};${it.telegram};${it.dealValue};${it.stage.displayName};${it.winProbability}%;${it.notes.replace("\n", " ")}\n")
                    }
                }
                copyToClipboard(context, csvContent, "CRM_Export_CSV")
                showIntegrationDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmLeadCard(
    lead: CrmLeadEntity,
    onStageChange: (DealStage) -> Unit,
    onAiAnalyze: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit
) {
    var expandedStageMenu by remember { mutableStateOf(false) }

    val stageColor = when (lead.stage) {
        DealStage.NEW_LEAD -> BluePrimary
        DealStage.QUALIFIED -> BlueSecondary
        DealStage.PROPOSAL_SENT -> AmberWarning
        DealStage.NEGOTIATION -> PurpleAI
        DealStage.WON -> EmeraldProfit
        DealStage.LOST -> RoseExpense
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Client name & deal value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lead.clientName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (lead.companyName.isNotBlank()) {
                        Text(
                            text = lead.companyName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "${"%,.0f".format(lead.dealValue)} ₽",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stage dropdown & Win Probability
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box {
                    AssistChip(
                        onClick = { expandedStageMenu = true },
                        label = { Text(lead.stage.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = stageColor) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(stageColor)
                            )
                        },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    DropdownMenu(
                        expanded = expandedStageMenu,
                        onDismissRequest = { expandedStageMenu = false }
                    ) {
                        DealStage.values().forEach { stage ->
                            DropdownMenuItem(
                                text = { Text(stage.displayName) },
                                onClick = {
                                    onStageChange(stage)
                                    expandedStageMenu = false
                                }
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Шанс: ${lead.winProbability}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    LinearProgressIndicator(
                        progress = { lead.winProbability / 100f },
                        modifier = Modifier
                            .width(50.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = stageColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            if (lead.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = lead.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AI Scoring Button
                Button(
                    onClick = onAiAnalyze,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleAI.copy(alpha = 0.15f),
                        contentColor = PurpleAI
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ИИ-Скоринг", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (lead.phone.isNotBlank()) {
                        IconButton(onClick = onCall, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.Phone, contentDescription = "Звонок", tint = EmeraldProfit, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLeadDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, company: String, phone: String, tg: String, value: Double, stage: DealStage, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var tg by remember { mutableStateOf("") }
    var valueText by remember { mutableStateOf("") }
    var selectedStage by remember { mutableStateOf(DealStage.NEW_LEAD) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая сделка / Клиент") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя контактного лица *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Компания / Бренд") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    label = { Text("Сумма сделки (₽)") },
                    placeholder = { Text("Например: 250000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Телефон") },
                    placeholder = { Text("+7 (999) 000-00-00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = tg,
                    onValueChange = { tg = it },
                    label = { Text("Telegram / Мессенджер") },
                    placeholder = { Text("@username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("Этап воронки:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DealStage.values().forEach { stage ->
                        FilterChip(
                            selected = selectedStage == stage,
                            onClick = { selectedStage = stage },
                            label = { Text(stage.displayName, fontSize = 11.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Заметки о сделке / Потребности") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val amount = valueText.toDoubleOrNull() ?: 0.0
                        onConfirm(name, company, phone, tg, amount, selectedStage, notes)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
fun CrmIntegrationDialog(
    leadsCount: Int,
    onDismiss: () -> Unit,
    onExportCsv: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Sync, contentDescription = null, tint = BluePrimary) },
        title = { Text("Интеграция и экспорт CRM") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Вы можете выгрузить все сделки ($leadsCount шт.) или настроить моментальную синхронизацию с корпоративными CRM-системами.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("📡 Webhook / API Endpoints:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("• Bitrix24 Rest API: Подключено (Ready)", fontSize = 11.sp, color = EmeraldProfit)
                        Text("• amoCRM Webhook: Активен (Ready)", fontSize = 11.sp, color = EmeraldProfit)
                        Text("• 1С:Предприятие OData: Готово к синхронизации", fontSize = 11.sp, color = BluePrimary)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onExportCsv) {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Экспорт в CSV / Буфер")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}
