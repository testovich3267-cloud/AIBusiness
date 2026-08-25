package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.CrmLeadEntity
import com.example.data.local.entity.DealStage
import kotlinx.coroutines.flow.Flow

@Dao
interface CrmDao {
    @Query("SELECT * FROM crm_leads ORDER BY lastContactDate DESC")
    fun getAllLeads(): Flow<List<CrmLeadEntity>>

    @Query("SELECT * FROM crm_leads WHERE stage = :stage ORDER BY dealValue DESC")
    fun getLeadsByStage(stage: DealStage): Flow<List<CrmLeadEntity>>

    @Query("SELECT * FROM crm_leads WHERE id = :id")
    suspend fun getLeadById(id: Long): CrmLeadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: CrmLeadEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeads(leads: List<CrmLeadEntity>)

    @Update
    suspend fun updateLead(lead: CrmLeadEntity)

    @Query("DELETE FROM crm_leads WHERE id = :id")
    suspend fun deleteLeadById(id: Long)
}
