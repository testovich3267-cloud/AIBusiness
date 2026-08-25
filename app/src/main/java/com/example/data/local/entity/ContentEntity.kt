package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ContentType(val displayName: String, val promptHint: String) {
    SMM_POST("Пост для соцсетей / Telegram", "Тема поста, инфоповод или акция..."),
    COMMERCIAL_OFFER("Коммерческое предложение (КП)", "Продукт, целевой B2B клиент, ключевая выгода..."),
    COLD_EMAIL("Холодное письмо клиенту", "Кому пишем, какую проблему решаем..."),
    OBJECTION_HANDLING("Скрипт отработки возражений", "Возражение клиента (например: 'дорого', 'подумаем')..."),
    SUPPORT_REPLY("Ответ клиенту / на отзыв", "Ситуация или отзыв покупателя..."),
    AD_COPY("Рекламный текст (Яндекс / VK / Ozon)", "Товар/услуга, УТП, оффер...")
}

enum class ContentTone(val displayName: String) {
    EXPERT("Экспертный & Уверенный"),
    BUSINESS("Строгий Деловой"),
    FRIENDLY("Дружелюбный & Теплый"),
    PERSUASIVE("Продающий & Энергичный"),
    LUXURY("Премиальный & Статусный")
}

@Entity(tableName = "contents")
data class ContentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: ContentType = ContentType.SMM_POST,
    val tone: ContentTone = ContentTone.PERSUASIVE,
    val targetAudience: String = "Владельцы бизнеса и клиенты",
    val promptInput: String,
    val generatedResult: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
