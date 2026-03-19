package com.example.aicompanion.core.domain.model

sealed interface ParsedIntent {
    data class HomeControl(
        val entityAlias: String,
        val action: HomeAction,
        val params: Map<String, Any> = emptyMap(),
    ) : ParsedIntent

    data class Routine(val routineAlias: String) : ParsedIntent

    data class CreateReminder(
        val description: String,
        val triggerTimeMs: Long,
    ) : ParsedIntent

    data class LocalQuery(val queryType: QueryType) : ParsedIntent
    data object Unknown : ParsedIntent
}

enum class HomeAction { TURN_ON, TURN_OFF, SET_BRIGHTNESS, SET_TEMPERATURE }
enum class QueryType { CURRENT_TIME, LIST_REMINDERS }
