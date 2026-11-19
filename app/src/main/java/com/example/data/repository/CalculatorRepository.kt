package com.example.data.repository

import com.example.data.local.CalculationHistoryDao
import com.example.data.local.CalculationHistoryEntity
import kotlinx.coroutines.flow.Flow

class CalculatorRepository(
    private val historyDao: CalculationHistoryDao
) {
    val history: Flow<List<CalculationHistoryEntity>> = historyDao.getAllHistory()

    suspend fun saveCalculation(expression: String, result: String, isScientific: Boolean) {
        historyDao.insertHistory(
            CalculationHistoryEntity(
                expression = expression,
                result = result,
                isScientific = isScientific
            )
        )
    }

    suspend fun deleteEntry(id: Long) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }
}
