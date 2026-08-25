package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.FinanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    @Query("SELECT * FROM finance_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<FinanceEntity>>

    @Query("SELECT * FROM finance_records WHERE type = 'INCOME' ORDER BY date DESC")
    fun getIncomeRecords(): Flow<List<FinanceEntity>>

    @Query("SELECT * FROM finance_records WHERE type = 'EXPENSE' ORDER BY date DESC")
    fun getExpenseRecords(): Flow<List<FinanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: FinanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<FinanceEntity>)

    @Query("DELETE FROM finance_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)
}
