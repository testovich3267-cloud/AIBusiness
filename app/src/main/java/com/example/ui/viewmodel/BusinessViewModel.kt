package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.repository.BusinessRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FinancialSummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netProfit: Double = 0.0,
    val marginPercent: Double = 0.0,
    val runwayMonths: Double = 3.5,
    val topExpenseCategory: String = "ФОТ и подрядчики",
    val incomeByCategory: Map<FinanceCategory, Double> = emptyMap(),
    val expenseByCategory: Map<FinanceCategory, Double> = emptyMap()
)

data class CrmSummary(
    val totalPipelineValue: Double = 0.0,
    val activeDealsCount: Int = 0,
    val wonDealsValue: Double = 0.0,
    val averageProbability: Int = 0
)

class BusinessViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BusinessRepository

    val tasks: StateFlow<List<TaskEntity>>
    val contents: StateFlow<List<ContentEntity>>
    val leads: StateFlow<List<CrmLeadEntity>>
    val financeRecords: StateFlow<List<FinanceEntity>>
    val chatMessages: StateFlow<List<ChatMessageEntity>>

    // Task state
    private val _taskCategoryFilter = MutableStateFlow<TaskCategory?>(null)
    val taskCategoryFilter: StateFlow<TaskCategory?> = _taskCategoryFilter.asStateFlow()

    private val _isDecomposingGoal = MutableStateFlow(false)
    val isDecomposingGoal: StateFlow<Boolean> = _isDecomposingGoal.asStateFlow()

    private val _decompositionResult = MutableStateFlow<String?>(null)
    val decompositionResult: StateFlow<String?> = _decompositionResult.asStateFlow()

    private val _bottleneckAnalysis = MutableStateFlow<String?>(null)
    val bottleneckAnalysis: StateFlow<Boolean> = MutableStateFlow(false) // placeholder flag
    private val _isAnalyzingBottlenecks = MutableStateFlow(false)
    val isAnalyzingBottlenecks: StateFlow<Boolean> = _isAnalyzingBottlenecks.asStateFlow()
    val bottleneckText: StateFlow<String?> = _bottleneckAnalysis.asStateFlow()

    // Content state
    private val _isGeneratingContent = MutableStateFlow(false)
    val isGeneratingContent: StateFlow<Boolean> = _isGeneratingContent.asStateFlow()

    private val _currentGeneratedCopy = MutableStateFlow<String?>(null)
    val currentGeneratedCopy: StateFlow<String?> = _currentGeneratedCopy.asStateFlow()

    // CRM state
    private val _stageFilter = MutableStateFlow<DealStage?>(null)
    val stageFilter: StateFlow<DealStage?> = _stageFilter.asStateFlow()

    private val _isAnalyzingLead = MutableStateFlow(false)
    val isAnalyzingLead: StateFlow<Boolean> = _isAnalyzingLead.asStateFlow()

    private val _leadAdviceResult = MutableStateFlow<String?>(null)
    val leadAdviceResult: StateFlow<String?> = _leadAdviceResult.asStateFlow()

    // Finance state
    private val _isGeneratingFinancialAudit = MutableStateFlow(false)
    val isGeneratingFinancialAudit: StateFlow<Boolean> = _isGeneratingFinancialAudit.asStateFlow()

    private val _financialAuditReport = MutableStateFlow<String?>(null)
    val financialAuditReport: StateFlow<String?> = _financialAuditReport.asStateFlow()

    // Chat state
    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Financial calculations
    val financialSummary: StateFlow<FinancialSummary>

    // CRM calculations
    val crmSummary: StateFlow<CrmSummary>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BusinessRepository(db)

        tasks = repository.allTasks.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        contents = repository.allContents.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        leads = repository.allLeads.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        financeRecords = repository.allFinanceRecords.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        chatMessages = repository.chatMessages.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        financialSummary = financeRecords.map { list ->
            val income = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val profit = income - expense
            val margin = if (income > 0) (profit / income) * 100 else 0.0
            val runway = if (expense > 0) (income / (expense / 3.0)).coerceAtLeast(1.0) else 6.0
            
            val expenseCats = list.filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
            
            val topCategory = expenseCats.maxByOrNull { it.value }?.key?.displayName ?: "Общие расходы"

            val incomeCats = list.filter { it.type == TransactionType.INCOME }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            FinancialSummary(
                totalIncome = income,
                totalExpense = expense,
                netProfit = profit,
                marginPercent = margin,
                runwayMonths = runway,
                topExpenseCategory = topCategory,
                incomeByCategory = incomeCats,
                expenseByCategory = expenseCats
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

        crmSummary = leads.map { list ->
            val active = list.filter { it.stage != DealStage.WON && it.stage != DealStage.LOST }
            val totalPipe = active.sumOf { it.dealValue }
            val won = list.filter { it.stage == DealStage.WON }.sumOf { it.dealValue }
            val avgProb = if (active.isNotEmpty()) active.sumOf { it.winProbability } / active.size else 0

            CrmSummary(
                totalPipelineValue = totalPipe,
                activeDealsCount = active.size,
                wonDealsValue = won,
                averageProbability = avgProb
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CrmSummary())

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    // --- Task Actions ---
    fun setCategoryFilter(category: TaskCategory?) {
        _taskCategoryFilter.value = category
    }

    fun addTask(
        title: String,
        description: String,
        category: TaskCategory,
        priority: TaskPriority,
        estimatedHours: Double
    ) {
        viewModelScope.launch {
            val newTask = TaskEntity(
                title = title.trim(),
                description = description.trim(),
                category = category,
                priority = priority,
                estimatedHours = estimatedHours,
                dueDate = System.currentTimeMillis() + 86400000L * 3
            )
            repository.insertTask(newTask)
        }
    }

    fun updateTaskStatus(task: TaskEntity, newStatus: TaskStatus) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = newStatus))
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    fun decomposeGoalWithAi(goalTitle: String, category: TaskCategory, priority: TaskPriority) {
        viewModelScope.launch {
            _isDecomposingGoal.value = true
            _decompositionResult.value = null
            val result = repository.decomposeGoalWithAi(goalTitle, category, priority)
            _decompositionResult.value = result
            _isDecomposingGoal.value = false
        }
    }

    fun applyDecompositionAsTask(title: String, category: TaskCategory, priority: TaskPriority, rationale: String) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                description = rationale,
                category = category,
                priority = priority,
                status = TaskStatus.TODO,
                isAiGenerated = true,
                aiRationale = "ИИ-декомпозиция цели"
            )
            repository.insertTask(task)
            _decompositionResult.value = null
        }
    }

    fun clearDecompositionResult() {
        _decompositionResult.value = null
    }

    fun analyzeBottlenecks() {
        viewModelScope.launch {
            _isAnalyzingBottlenecks.value = true
            val currentTasks = tasks.value
            val result = repository.analyzeBottlenecks(currentTasks)
            _bottleneckAnalysis.value = result
            _isAnalyzingBottlenecks.value = false
        }
    }

    fun clearBottleneckAnalysis() {
        _bottleneckAnalysis.value = null
    }

    // --- Content Actions ---
    fun generateContent(
        title: String,
        type: ContentType,
        tone: ContentTone,
        audience: String,
        details: String
    ) {
        viewModelScope.launch {
            _isGeneratingContent.value = true
            _currentGeneratedCopy.value = null
            val result = repository.generateContentWithAi(title, type, tone, audience, details)
            _currentGeneratedCopy.value = result
            
            // Auto save to database
            val contentEntity = ContentEntity(
                title = title,
                type = type,
                tone = tone,
                targetAudience = audience,
                promptInput = details,
                generatedResult = result
            )
            repository.saveContent(contentEntity)
            _isGeneratingContent.value = false
        }
    }

    fun deleteContent(id: Long) {
        viewModelScope.launch {
            repository.deleteContent(id)
        }
    }

    // --- CRM Actions ---
    fun setStageFilter(stage: DealStage?) {
        _stageFilter.value = stage
    }

    fun addLead(
        name: String,
        company: String,
        phone: String,
        telegram: String,
        dealValue: Double,
        stage: DealStage,
        notes: String
    ) {
        viewModelScope.launch {
            val lead = CrmLeadEntity(
                clientName = name.trim(),
                companyName = company.trim(),
                phone = phone.trim(),
                telegram = telegram.trim(),
                dealValue = dealValue,
                stage = stage,
                winProbability = when(stage) {
                    DealStage.NEW_LEAD -> 20
                    DealStage.QUALIFIED -> 40
                    DealStage.PROPOSAL_SENT -> 65
                    DealStage.NEGOTIATION -> 80
                    DealStage.WON -> 100
                    DealStage.LOST -> 0
                },
                notes = notes.trim()
            )
            repository.insertLead(lead)
        }
    }

    fun updateLeadStage(lead: CrmLeadEntity, newStage: DealStage) {
        viewModelScope.launch {
            val newProb = when(newStage) {
                DealStage.NEW_LEAD -> 20
                DealStage.QUALIFIED -> 40
                DealStage.PROPOSAL_SENT -> 65
                DealStage.NEGOTIATION -> 80
                DealStage.WON -> 100
                DealStage.LOST -> 0
            }
            repository.updateLead(lead.copy(stage = newStage, winProbability = newProb, lastContactDate = System.currentTimeMillis()))
        }
    }

    fun deleteLead(id: Long) {
        viewModelScope.launch {
            repository.deleteLead(id)
        }
    }

    fun analyzeLeadWithAi(lead: CrmLeadEntity) {
        viewModelScope.launch {
            _isAnalyzingLead.value = true
            _leadAdviceResult.value = null
            val advice = repository.analyzeLeadWithAi(lead)
            _leadAdviceResult.value = advice
            repository.updateLead(lead.copy(aiRecommendation = advice))
            _isAnalyzingLead.value = false
        }
    }

    fun clearLeadAdvice() {
        _leadAdviceResult.value = null
    }

    // --- Finance Actions ---
    fun addFinanceRecord(
        title: String,
        amount: Double,
        type: TransactionType,
        category: FinanceCategory,
        note: String
    ) {
        viewModelScope.launch {
            val record = FinanceEntity(
                title = title.trim(),
                amount = amount,
                type = type,
                category = category,
                note = note.trim()
            )
            repository.insertFinanceRecord(record)
        }
    }

    fun deleteFinanceRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteFinanceRecord(id)
        }
    }

    fun generateFinancialAudit() {
        viewModelScope.launch {
            _isGeneratingFinancialAudit.value = true
            _financialAuditReport.value = null
            val summary = financialSummary.value
            val report = repository.generateFinancialAuditWithAi(
                totalIncome = summary.totalIncome,
                totalExpense = summary.totalExpense,
                netProfit = summary.netProfit,
                marginPercent = summary.marginPercent,
                runwayMonths = summary.runwayMonths,
                topExpenseCategory = summary.topExpenseCategory
            )
            _financialAuditReport.value = report
            _isGeneratingFinancialAudit.value = false
        }
    }

    fun clearFinancialAuditReport() {
        _financialAuditReport.value = null
    }

    // --- Chat Copilot Actions ---
    fun sendChatMessage(text: String, playbookTag: String? = null) {
        viewModelScope.launch {
            _isChatLoading.value = true
            repository.sendChatMessage(text, playbookTag)
            _isChatLoading.value = false
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }
}
