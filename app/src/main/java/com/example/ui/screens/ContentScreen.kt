package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
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
import com.example.data.local.entity.ContentEntity
import com.example.data.local.entity.ContentTone
import com.example.data.local.entity.ContentType
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BusinessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentScreen(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val contents by viewModel.contents.collectAsState()
    val isGenerating by viewModel.isGeneratingContent.collectAsState()
    val currentGeneratedCopy by viewModel.currentGeneratedCopy.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Генератор, 1: Библиотека

    // Generator inputs
    var selectedType by remember { mutableStateOf(ContentType.SMM_POST) }
    var selectedTone by remember { mutableStateOf(ContentTone.PERSUASIVE) }
    var titleInput by remember { mutableStateOf("") }
    var audienceInput by remember { mutableStateOf("Владельцы бизнеса и клиенты") }
    var detailsInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Screen Header & Tab switcher
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = BluePrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("ИИ-Генератор", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Сохраненное (${contents.size})", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Outlined.Folder, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            if (selectedTab == 0) {
                // Generator Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Type Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Тип бизнес-контента",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ContentType.values().forEach { type ->
                                FilterChip(
                                    selected = selectedType == type,
                                    onClick = { selectedType = type },
                                    label = { Text(type.displayName, fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    // Title
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Тема / Название продукта или акции") },
                        placeholder = { Text("Например: Весенняя скидка 15% на сервисный пакет") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Tone selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Тональность (Tone of Voice)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ContentTone.values().forEach { tone ->
                                FilterChip(
                                    selected = selectedTone == tone,
                                    onClick = { selectedTone = tone },
                                    label = { Text(tone.displayName, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Audience
                    OutlinedTextField(
                        value = audienceInput,
                        onValueChange = { audienceInput = it },
                        label = { Text("Целевая аудитория (ЦА)") },
                        placeholder = { Text("Например: B2B директора, розничные покупатели, владельцы кофеен...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Details
                    OutlinedTextField(
                        value = detailsInput,
                        onValueChange = { detailsInput = it },
                        label = { Text("УТП, ключевые выгоды или детали оффера") },
                        placeholder = { Text(selectedType.promptHint) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    // Generate Button
                    Button(
                        onClick = {
                            val title = titleInput.ifBlank { "Контент: ${selectedType.displayName}" }
                            viewModel.generateContent(
                                title = title,
                                type = selectedType,
                                tone = selectedTone,
                                audience = audienceInput,
                                details = detailsInput.ifBlank { "Создай убедительный, конверсионный контент." }
                            )
                        },
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        if (isGenerating) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ИИ пишет текст...", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Сгенерировать контент", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }

                    // Output Result Card
                    if (currentGeneratedCopy != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BlueAccent.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AiBadge(text = "Готовый результат")
                                    Row {
                                        IconButton(
                                            onClick = { copyToClipboard(context, currentGeneratedCopy ?: "") }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.ContentCopy,
                                                contentDescription = "Копировать",
                                                tint = BluePrimary
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                val sendIntent: Intent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, currentGeneratedCopy)
                                                    type = "text/plain"
                                                }
                                                val shareIntent = Intent.createChooser(sendIntent, "Поделиться контентом")
                                                context.startActivity(shareIntent)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Share,
                                                contentDescription = "Поделиться",
                                                tint = BluePrimary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                SelectionContainer {
                                    Text(
                                        text = currentGeneratedCopy ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Saved Library Tab
                if (contents.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Outlined.Article,
                        title = "Библиотека пуста",
                        description = "Сгенерированные материалы автоматически сохраняются здесь для быстрого копирования",
                        actionButtonText = "Сгенерировать текст",
                        onActionClick = { selectedTab = 0 }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(contents, key = { it.id }) { item ->
                            SavedContentCard(
                                item = item,
                                onCopy = { copyToClipboard(context, item.generatedResult) },
                                onShare = {
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, item.generatedResult)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Поделиться")
                                    context.startActivity(shareIntent)
                                },
                                onDelete = { viewModel.deleteContent(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedContentCard(
    item: ContentEntity,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.type.displayName} • ${item.tone.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Копировать", tint = BluePrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Share, contentDescription = "Поделиться", tint = BluePrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.generatedResult,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            if (item.generatedResult.length > 150) {
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (expanded) "Свернуть" else "Показать полностью...",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BluePrimary)
                    )
                }
            }
        }
    }
}
