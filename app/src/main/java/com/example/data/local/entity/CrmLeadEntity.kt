package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DealStage(val displayName: String, val colorHex: Long) {
    NEW_LEAD("Новый лид", 0xFF60A5FA),
    QUALIFIED("Квалифицирован", 0xFF38BDF8),
    PROPOSAL_SENT("КП отправлено", 0xFFF59E0B),
    NEGOTIATION("Переговоры", 0xFF8B5CF6),
    WON("Успешно закрыто", 0xFF10B981),
    LOST("Отказ", 0xFFF43F5E)
}

@Entity(tableName = "crm_leads")
data class CrmLeadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientName: String,
    val companyName: String = "",
    val phone: String = "",
    val email: String = "",
    val telegram: String = "",
    val dealValue: Double = 0.0,
    val stage: DealStage = DealStage.NEW_LEAD,
    val winProbability: Int = 30, // 0 - 100%
    val notes: String = "",
    val aiRecommendation: String = "",
    val nextStepAction: String = "",
    val lastContactDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
