package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.local.entity.*

class Converters {
    @TypeConverter
    fun fromTaskCategory(value: TaskCategory): String = value.name

    @TypeConverter
    fun toTaskCategory(value: String): TaskCategory = runCatching { TaskCategory.valueOf(value) }.getOrDefault(TaskCategory.OPERATIONS)

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): String = value.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = runCatching { TaskPriority.valueOf(value) }.getOrDefault(TaskPriority.IMPORTANT_NOT_URGENT)

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = runCatching { TaskStatus.valueOf(value) }.getOrDefault(TaskStatus.TODO)

    @TypeConverter
    fun fromContentType(value: ContentType): String = value.name

    @TypeConverter
    fun toContentType(value: String): ContentType = runCatching { ContentType.valueOf(value) }.getOrDefault(ContentType.SMM_POST)

    @TypeConverter
    fun fromContentTone(value: ContentTone): String = value.name

    @TypeConverter
    fun toContentTone(value: String): ContentTone = runCatching { ContentTone.valueOf(value) }.getOrDefault(ContentTone.PERSUASIVE)

    @TypeConverter
    fun fromDealStage(value: DealStage): String = value.name

    @TypeConverter
    fun toDealStage(value: String): DealStage = runCatching { DealStage.valueOf(value) }.getOrDefault(DealStage.NEW_LEAD)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = runCatching { TransactionType.valueOf(value) }.getOrDefault(TransactionType.EXPENSE)

    @TypeConverter
    fun fromFinanceCategory(value: FinanceCategory): String = value.name

    @TypeConverter
    fun toFinanceCategory(value: String): FinanceCategory = runCatching { FinanceCategory.valueOf(value) }.getOrDefault(FinanceCategory.OTHER_EXPENSE)

    @TypeConverter
    fun fromMessageSender(value: MessageSender): String = value.name

    @TypeConverter
    fun toMessageSender(value: String): MessageSender = runCatching { MessageSender.valueOf(value) }.getOrDefault(MessageSender.SYSTEM)
}
