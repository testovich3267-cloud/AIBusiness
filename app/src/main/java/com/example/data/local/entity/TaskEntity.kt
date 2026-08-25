package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskCategory(val displayName: String, val iconName: String) {
    MARKETING("Маркетинг", "Campaign"),
    SALES("Продажи", "TrendingUp"),
    OPERATIONS("Операции", "Settings"),
    FINANCE("Финансы", "AccountBalance"),
    LEGAL("Документы & Юр.", "Description"),
    HR("Команда & Найм", "Group")
}

enum class TaskPriority(val displayName: String, val level: Int) {
    URGENT_IMPORTANT("Срочно & Важно (Q1)", 1),
    IMPORTANT_NOT_URGENT("Важно, не срочно (Q2)", 2),
    URGENT_NOT_IMPORTANT("Срочно, не важно (Q3)", 3),
    NOT_URGENT_NOT_IMPORTANT("Рутина / Делегировать (Q4)", 4)
}

enum class TaskStatus(val displayName: String) {
    TODO("К выполнению"),
    IN_PROGRESS("В работе"),
    REVIEW("На проверке"),
    DONE("Завершено")
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: TaskCategory = TaskCategory.OPERATIONS,
    val priority: TaskPriority = TaskPriority.IMPORTANT_NOT_URGENT,
    val status: TaskStatus = TaskStatus.TODO,
    val estimatedHours: Double = 1.0,
    val subtasksJson: String = "[]", // JSON array of subtask strings
    val completedSubtasksCount: Int = 0,
    val totalSubtasksCount: Int = 0,
    val isAiGenerated: Boolean = false,
    val aiRationale: String = "",
    val dueDate: Long = System.currentTimeMillis() + 86400000L * 3,
    val createdAt: Long = System.currentTimeMillis()
)
