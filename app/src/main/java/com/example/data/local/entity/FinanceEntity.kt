package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val displayName: String) {
    INCOME("Доход"),
    EXPENSE("Расход")
}

enum class FinanceCategory(val displayName: String, val isIncome: Boolean) {
    SALES("Продажи клиентам", true),
    SERVICES("Оказание услуг", true),
    INVESTMENT("Инвестиции / Займы", true),
    OTHER_INCOME("Прочий доход", true),
    
    PAYROLL("Зарплаты и подрядчики", false),
    MARKETING_ADS("Маркетинг и реклама", false),
    RENT_OFFICE("Аренда и офис", false),
    SOFTWARE_SAAS("Софт, связь и сервисы", false),
    INVENTORY("Закупка товаров/сырья", false),
    TAXES("Налоги и сборы", false),
    OTHER_EXPENSE("Прочие расходы", false)
}

@Entity(tableName = "finance_records")
data class FinanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: FinanceCategory,
    val date: Long = System.currentTimeMillis(),
    val note: String = ""
)
