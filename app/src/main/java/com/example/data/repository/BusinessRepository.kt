package com.example.data.repository

import com.example.data.ai.GeminiService
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class BusinessRepository(private val db: AppDatabase) {

    // --- Tasks ---
    val allTasks: Flow<List<TaskEntity>> = db.taskDao().getAllTasks()

    suspend fun insertTask(task: TaskEntity): Long = db.taskDao().insertTask(task)

    suspend fun updateTask(task: TaskEntity) = db.taskDao().updateTask(task)

    suspend fun deleteTask(id: Long) = db.taskDao().deleteTaskById(id)

    suspend fun decomposeGoalWithAi(goalTitle: String, category: TaskCategory, priority: TaskPriority): String {
        val prompt = """
        Декомпозируй следующую бизнес-задачу для малого бизнеса:
        "$goalTitle"
        Категория: ${category.displayName}
        Приоритет: ${priority.displayName}
        
        Напиши:
        1) 4-6 конкретных пошаговых подзадач с оценкой времени в часах и ответственной ролью.
        2) Главное бутылочное горлышко (риск), на которое обратить внимание.
        3) Рекомендуемый первый шаг на сегодня.
        """.trimIndent()

        return GeminiService.generateBusinessText(prompt).getOrDefault("Не удалось получить ответ от ИИ.")
    }

    suspend fun analyzeBottlenecks(tasks: List<TaskEntity>): String {
        val tasksSummary = tasks.filter { it.status != TaskStatus.DONE }
            .take(8)
            .joinToString("\n") { "• [${it.priority.displayName}] ${it.title} (${it.category.displayName})" }

        val prompt = """
        Проанализируй список текущих незавершенных бизнес-задач компании:
        $tasksSummary
        
        Определи:
        1. Главное узкое место (бутылочное горлышко) в бизнесе прямо сейчас.
        2. Какую задачу собственник должен решить лично в первую очередь, а что немедленно делегировать.
        3. Экспресс-совет по ускорению исполнения.
        """.trimIndent()

        return GeminiService.generateBusinessText(prompt).getOrDefault("Фокусируйтесь на задачах из категории Продажи и Маркетинг для поддержания денежного потока.")
    }

    // --- Content Generator ---
    val allContents: Flow<List<ContentEntity>> = db.contentDao().getAllContents()

    suspend fun saveContent(content: ContentEntity): Long = db.contentDao().insertContent(content)

    suspend fun deleteContent(id: Long) = db.contentDao().deleteContentById(id)

    suspend fun generateContentWithAi(
        title: String,
        type: ContentType,
        tone: ContentTone,
        audience: String,
        promptInput: String
    ): String {
        val prompt = """
        Создай готовый бизнес-контент:
        Тип: ${type.displayName}
        Тема/Название: $title
        Тон коммуникации: ${tone.displayName}
        Целевая аудитория: $audience
        Вводные данные / Оффер / Детали: $promptInput
        
        Требования:
        - Текст должен быть полностью готов к отправке / публикации.
        - Добавь цепляющий заголовок, четкие выгоды для клиента, призыв к действию (CTA).
        - Для постов добавь 3-5 актуальных хэштегов.
        - Без воды, с упором на конверсию и доверие.
        """.trimIndent()

        return GeminiService.generateBusinessText(prompt).getOrDefault("Ошибка генерации контента.")
    }

    // --- CRM ---
    val allLeads: Flow<List<CrmLeadEntity>> = db.crmDao().getAllLeads()

    suspend fun insertLead(lead: CrmLeadEntity): Long = db.crmDao().insertLead(lead)

    suspend fun updateLead(lead: CrmLeadEntity) = db.crmDao().updateLead(lead)

    suspend fun deleteLead(id: Long) = db.crmDao().deleteLeadById(id)

    suspend fun analyzeLeadWithAi(lead: CrmLeadEntity): String {
        val prompt = """
        Проведи ИИ-скоринг и подготовь план действий по сделке в CRM:
        Клиент: ${lead.clientName}
        Компания: ${lead.companyName}
        Сумма сделки: ${lead.dealValue} ₽
        Текущий этап: ${lead.stage.displayName}
        Заметки о клиенте: ${lead.notes}
        
        Дай ответ в формате:
        1. 🎯 Оценка вероятности закрытия сделки (0-100%) и обоснование
        2. ⚠️ Главные риски срыва сделки
        3. 💬 Персональный скрипт сообщения клиенту (для WhatsApp / Telegram / звонка) для перевода на следующий этап
        """.trimIndent()

        return GeminiService.generateBusinessText(prompt).getOrDefault("Рекомендуется связаться с клиентом для уточнения деталей КП.")
    }

    // --- Finance ---
    val allFinanceRecords: Flow<List<FinanceEntity>> = db.financeDao().getAllRecords()

    suspend fun insertFinanceRecord(record: FinanceEntity): Long = db.financeDao().insertRecord(record)

    suspend fun deleteFinanceRecord(id: Long) = db.financeDao().deleteRecordById(id)

    suspend fun generateFinancialAuditWithAi(
        totalIncome: Double,
        totalExpense: Double,
        netProfit: Double,
        marginPercent: Double,
        runwayMonths: Double,
        topExpenseCategory: String
    ): String {
        val prompt = """
        Ты — финансовый директор (CFO) малого бизнеса. Проведи аудит следующих метрик компании за текущий период:
        • Выручка (Доходы): $totalIncome ₽
        • Общие расходы: $totalExpense ₽
        • Чистая прибыль: $netProfit ₽
        • Рентабельность: ${"%.1f".format(marginPercent)}%
        • Запас прочности (Runway): ${"%.1f".format(runwayMonths)} мес.
        • Главная статья затрат: $topExpenseCategory
        
        Сформируй управленческий отчет:
        1. 🚦 Оценка финансового здоровья компании (Зеленая / Желтая / Красная зона).
        2. ✂️ 3 конкретных шага по оптимизации расходов без ущерба продажам.
        3. 🚀 2 рычага роста маржинальности и среднего чека.
        4. 🛡️ Рекомендация по формированию резервного фонда.
        """.trimIndent()

        return GeminiService.generateBusinessText(prompt).getOrDefault("Финансовые показатели стабильны. Рекомендуется оптимизировать переменные расходы.")
    }

    // --- Chat Copilot ---
    val chatMessages: Flow<List<ChatMessageEntity>> = db.chatDao().getAllMessages()

    suspend fun sendChatMessage(userText: String, playbookTag: String? = null): String {
        val userMsg = ChatMessageEntity(
            sender = MessageSender.USER,
            text = userText,
            playbookTag = playbookTag
        )
        db.chatDao().insertMessage(userMsg)

        val systemPrompt = """
        Ты — персональный ИИ-бизнес-консультант, трекер и финансово-маркетинговый советник для владельца малого бизнеса.
        Отвечай конкретно, по делу, с четкими пунктами, цифрами, скриптами и практическими советами.
        """.trimIndent()

        val aiResponseText = GeminiService.generateBusinessText(userText, systemPrompt)
            .getOrDefault("Извините, произошла ошибка связи. Попробуйте еще раз.")

        val aiMsg = ChatMessageEntity(
            sender = MessageSender.AI,
            text = aiResponseText,
            playbookTag = playbookTag
        )
        db.chatDao().insertMessage(aiMsg)
        return aiResponseText
    }

    suspend fun clearChatHistory() = db.chatDao().clearHistory()

    // --- Seed Initial Mock/Template Data for Small Business ---
    suspend fun seedInitialDataIfEmpty() {
        val taskCount = db.taskDao().getAllTasks()
        // We check on the first run
        // Seed helpful initial business entities
        val initialTasks = listOf(
            TaskEntity(
                title = "Запустить таргетированную рекламу для B2B клиентов",
                description = "Протестировать 3 гипотезы в Яндекс.Директ и VK Реклама с бюджетом 30 000 руб.",
                category = TaskCategory.MARKETING,
                priority = TaskPriority.URGENT_IMPORTANT,
                status = TaskStatus.IN_PROGRESS,
                estimatedHours = 4.0,
                totalSubtasksCount = 4,
                completedSubtasksCount = 2,
                isAiGenerated = true,
                aiRationale = "Критично для генерации новых лидов в текущем месяце."
            ),
            TaskEntity(
                title = "Подготовить и отправить КП для ООО «Альфа-Трейд»",
                description = "Согласовать спецификацию и приложить расчет окупаемости для гендиректора.",
                category = TaskCategory.SALES,
                priority = TaskPriority.URGENT_IMPORTANT,
                status = TaskStatus.TODO,
                estimatedHours = 2.0,
                totalSubtasksCount = 3,
                completedSubtasksCount = 0,
                isAiGenerated = false
            ),
            TaskEntity(
                title = "Провести аудит юнит-экономики и расходов на софт",
                description = "Отключить неиспользуемые подписки и оптимизировать стоимость эквайринга.",
                category = TaskCategory.FINANCE,
                priority = TaskPriority.IMPORTANT_NOT_URGENT,
                status = TaskStatus.TODO,
                estimatedHours = 3.0,
                totalSubtasksCount = 3,
                completedSubtasksCount = 1,
                isAiGenerated = true
            ),
            TaskEntity(
                title = "Собеседование на позицию Senior менеджера по продажам",
                description = "Отобрать 3 финальных кандидата, провести тестовый созвон с ролевой игрой.",
                category = TaskCategory.HR,
                priority = TaskPriority.IMPORTANT_NOT_URGENT,
                status = TaskStatus.TODO,
                estimatedHours = 2.5
            ),
            TaskEntity(
                title = "Обновить типовой договор поставки и NDA",
                description = "Добавить пункт об электронном документообороте и штрафах за задержку оплаты.",
                category = TaskCategory.LEGAL,
                priority = TaskPriority.NOT_URGENT_NOT_IMPORTANT,
                status = TaskStatus.DONE,
                estimatedHours = 1.5
            )
        )
        db.taskDao().insertTasks(initialTasks)

        val initialLeads = listOf(
            CrmLeadEntity(
                clientName = "Алексей Смирнов",
                companyName = "ООО «ТехноПром»",
                phone = "+7 (999) 234-56-78",
                email = "smirnov@technoprom.ru",
                telegram = "@alex_techno",
                dealValue = 450000.0,
                stage = DealStage.PROPOSAL_SENT,
                winProbability = 75,
                notes = "Заинтересованы во внедрении автоматизации. Ждут коммерческое предложение со скидкой 5%.",
                aiRecommendation = "Предложить 5% скидку при оплате 100% предоплаты в течение 3 дней.",
                nextStepAction = "Созвон-презентация КП в четверг 14:00"
            ),
            CrmLeadEntity(
                clientName = "Елена Васильева",
                companyName = "Сеть кофеен «Утро»",
                phone = "+7 (916) 789-01-23",
                email = "elena@utro-coffee.ru",
                telegram = "@elena_utro",
                dealValue = 180000.0,
                stage = DealStage.NEGOTIATION,
                winProbability = 60,
                notes = "Сравнивают с конкурентом. Беспокоит срок внедрения и простота обучения бариста.",
                aiRecommendation = "Акцентировать внимание на бесплатном 2-дневном обучении команды и круглосуточной поддержке.",
                nextStepAction = "Отправить видео-отзыв аналогичной сети кофеен"
            ),
            CrmLeadEntity(
                clientName = "Дмитрий Ковалев",
                companyName = "ИП Ковалев Д.М.",
                phone = "+7 (903) 456-78-90",
                email = "dmitry@kovalev-logistics.ru",
                telegram = "@dmitry_logistics",
                dealValue = 320000.0,
                stage = DealStage.WON,
                winProbability = 100,
                notes = "Договор подписан, счет оплачен. Передано в отдел аккаунтинга.",
                aiRecommendation = "Через 14 дней запросить обратную связь и предложить кросс-селл модуль аналитики."
            )
        )
        db.crmDao().insertLeads(initialLeads)

        val initialFinance = listOf(
            FinanceEntity(
                title = "Оплата контракта (ООО ТехноПром)",
                amount = 450000.0,
                type = TransactionType.INCOME,
                category = FinanceCategory.SALES,
                date = System.currentTimeMillis() - 86400000L * 2
            ),
            FinanceEntity(
                title = "Абонентская плата за сервис (ИП Ковалев)",
                amount = 120000.0,
                type = TransactionType.INCOME,
                category = FinanceCategory.SERVICES,
                date = System.currentTimeMillis() - 86400000L * 4
            ),
            FinanceEntity(
                title = "ФОТ сотрудников и подрядчиков",
                amount = 210000.0,
                type = TransactionType.EXPENSE,
                category = FinanceCategory.PAYROLL,
                date = System.currentTimeMillis() - 86400000L * 1
            ),
            FinanceEntity(
                title = "Рекламный бюджет Яндекс.Директ",
                amount = 65000.0,
                type = TransactionType.EXPENSE,
                category = FinanceCategory.MARKETING_ADS,
                date = System.currentTimeMillis() - 86400000L * 3
            ),
            FinanceEntity(
                title = "Аренда офиса и коммунальные услуги",
                amount = 75000.0,
                type = TransactionType.EXPENSE,
                category = FinanceCategory.RENT_OFFICE,
                date = System.currentTimeMillis() - 86400000L * 5
            ),
            FinanceEntity(
                title = "Подписки на CRM, телефонию и облако",
                amount = 18000.0,
                type = TransactionType.EXPENSE,
                category = FinanceCategory.SOFTWARE_SAAS,
                date = System.currentTimeMillis() - 86400000L * 6
            )
        )
        db.financeDao().insertRecords(initialFinance)

        val initialContent = ContentEntity(
            title = "Анонс весенней акции для B2B партнеров",
            type = ContentType.SMM_POST,
            tone = ContentTone.PERSUASIVE,
            targetAudience = "Владельцы бизнеса и директора по закупкам",
            promptInput = "Весенняя скидка 15% на годовое обслуживание при заключении договора до конца месяца.",
            generatedResult = """
            🚀 **Как зафиксировать максимальную выгоду на весь 2026 год?**
            
            Весна — время масштабирования и запуска новых направлений. Чтобы операционные затраты не съедали вашу маржу, мы открываем закрытый весенний оффер для партнеров:
            
            ⚡ **Скидка 15% на годовое сервисное сопровождение** при оформлении договора до 31 числа!
            
            Что вы получаете:
            • Персонального проектного менеджера 24/7
            • Бесплатную настройку интеграций с вашей CRM и 1С
            • Гарантированный SLA реакции до 15 минут
            
            📩 Напишите нам в Direct или оставьте заявку по ссылке в профиле, чтобы забронировать спецусловия.
            
            #бизнес #b2b #партнерство #акция #автоматизация
            """.trimIndent(),
            isFavorite = true
        )
        db.contentDao().insertContent(initialContent)

        val initialChat = listOf(
            ChatMessageEntity(
                sender = MessageSender.AI,
                text = "Приветствую! Я ваш персональный ИИ-бизнес ассистент. Я помогу декомпозировать стратегические цели в задачи, сгенерировать продающие тексты и КП, рассчитать вероятность закрытия сделок в CRM и провести финансовый аудит компании. С чего начнем?"
            )
        )
        db.chatDao().insertMessages(initialChat)
    }
}
