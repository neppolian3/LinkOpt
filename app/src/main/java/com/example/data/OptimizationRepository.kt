package com.example.data

import kotlinx.coroutines.flow.Flow

class OptimizationRepository(private val dao: OptimizationDao) {
    val allOptimizations: Flow<List<ProfileOptimizationEntity>> = dao.getAllOptimizations()
    val allAudits: Flow<List<LinkedInAuditEntity>> = dao.getAllAudits()

    suspend fun saveOptimization(optimization: ProfileOptimizationEntity): Long {
        return dao.insertOptimization(optimization)
    }

    suspend fun deleteOptimization(id: Int) {
        dao.deleteOptimizationById(id)
    }

    suspend fun saveAudit(audit: LinkedInAuditEntity): Long {
        return dao.insertAudit(audit)
    }

    suspend fun deleteAudit(id: Int) {
        dao.deleteAuditById(id)
    }
}
