package com.example.aicompanion.core.ai.parser

import com.example.aicompanion.core.domain.model.HomeAction
import com.example.aicompanion.core.domain.model.ParsedIntent
import com.example.aicompanion.core.domain.model.QueryType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandParser @Inject constructor() {

    // Home control patterns
    private val TURN_ON = Regex(
        """(?:turn|switch|put)\s+(?:on\s+)?(?:the\s+)?(.+?)(?:\s+on)?$""",
        RegexOption.IGNORE_CASE
    )
    private val TURN_OFF = Regex(
        """(?:turn|switch|shut)\s+off\s+(?:the\s+)?(.+)$""",
        RegexOption.IGNORE_CASE
    )
    private val BRIGHTNESS = Regex(
        """(?:dim|set|change)\s+(?:the\s+)?(.+?)\s+(?:to|at)\s+(\d+)\s*(?:percent|%)?""",
        RegexOption.IGNORE_CASE
    )
    private val TEMPERATURE = Regex(
        """(?:set|change)\s+(?:the\s+)?(.+?)\s+(?:temperature\s+)?(?:to|at)\s+(\d+)\s*(?:degrees?|°)?""",
        RegexOption.IGNORE_CASE
    )

    // Routine patterns
    private val ROUTINE = Regex(
        """(?:run|start|execute|activate|trigger)\s+(?:the\s+)?(.+?)\s+(?:routine|scene|automation)""",
        RegexOption.IGNORE_CASE
    )

    // Reminder patterns
    private val REMINDER = Regex(
        """remind\s+me\s+(?:to\s+)?(.+?)\s+(?:at|in)\s+(.+)$""",
        RegexOption.IGNORE_CASE
    )

    // Local query patterns
    private val TIME_QUERY = Regex(
        """what(?:'s|\s+is)\s+(?:the\s+)?(?:current\s+)?time""",
        RegexOption.IGNORE_CASE
    )
    private val REMINDERS_QUERY = Regex(
        """(?:what\s+reminders?\s+do\s+I\s+have|list\s+(?:my\s+)?reminders?|show\s+(?:my\s+)?reminders?)""",
        RegexOption.IGNORE_CASE
    )

    // Time parsing patterns
    private val TIME_ABSOLUTE = Regex("""(\d{1,2})(?::(\d{2}))?\s*(am|pm)""", RegexOption.IGNORE_CASE)
    private val TIME_RELATIVE = Regex("""in\s+(\d+)\s*(minutes?|hours?|mins?)""", RegexOption.IGNORE_CASE)

    fun parse(transcript: String): ParsedIntent {
        val input = transcript.trim()

        // 1. Check local queries first (shortest patterns)
        TIME_QUERY.find(input)?.let { return ParsedIntent.LocalQuery(QueryType.CURRENT_TIME) }
        REMINDERS_QUERY.find(input)?.let { return ParsedIntent.LocalQuery(QueryType.LIST_REMINDERS) }

        // 2. Reminder creation
        REMINDER.find(input)?.let { match ->
            val description = match.groupValues[1].trim()
            val timeStr = match.groupValues[2].trim()
            val triggerTimeMs = parseTime(timeStr) ?: return ParsedIntent.Unknown
            return ParsedIntent.CreateReminder(description, triggerTimeMs)
        }

        // 3. Home control — brightness
        BRIGHTNESS.find(input)?.let { match ->
            val device = match.groupValues[1].trim()
            val level = match.groupValues[2].toIntOrNull() ?: return ParsedIntent.Unknown
            // Convert percentage (0-100) to HA brightness (0-255)
            val brightness = (level * 255 / 100).coerceIn(0, 255)
            return ParsedIntent.HomeControl(device, HomeAction.SET_BRIGHTNESS, mapOf("brightness" to brightness))
        }

        // 4. Home control — temperature
        TEMPERATURE.find(input)?.let { match ->
            val device = match.groupValues[1].trim()
            val temp = match.groupValues[2].toFloatOrNull() ?: return ParsedIntent.Unknown
            return ParsedIntent.HomeControl(device, HomeAction.SET_TEMPERATURE, mapOf("temperature" to temp))
        }

        // 5. Home control — turn off (check before turn on to avoid "turn off X" matching "turn ... on")
        TURN_OFF.find(input)?.let { match ->
            val device = match.groupValues[1].trim()
            return ParsedIntent.HomeControl(device, HomeAction.TURN_OFF)
        }

        // 6. Home control — turn on
        TURN_ON.find(input)?.let { match ->
            val device = match.groupValues[1].trim()
            return ParsedIntent.HomeControl(device, HomeAction.TURN_ON)
        }

        // 7. Routine (specific pattern with keyword suffix)
        ROUTINE.find(input)?.let { match ->
            val routine = match.groupValues[1].trim()
            return ParsedIntent.Routine(routine)
        }

        // 8. Unknown — pass to LLM fallback (Phase 3+)
        return ParsedIntent.Unknown
    }

    private fun parseTime(timeStr: String): Long? {
        // Relative time: "in 30 minutes", "in 2 hours"
        TIME_RELATIVE.find(timeStr)?.let { match ->
            val amount = match.groupValues[1].toLongOrNull() ?: return null
            val unit = match.groupValues[2].lowercase()
            val durationMs = when {
                unit.startsWith("min") -> amount * 60_000L
                unit.startsWith("hour") -> amount * 3_600_000L
                else -> return null
            }
            return System.currentTimeMillis() + durationMs
        }

        // Absolute time: "8 PM", "8:30 AM"
        TIME_ABSOLUTE.find(timeStr)?.let { match ->
            val hour = match.groupValues[1].toIntOrNull() ?: return null
            val minute = match.groupValues[2].toIntOrNull() ?: 0
            val amPm = match.groupValues[3].uppercase()

            val hour24 = when {
                amPm == "PM" && hour != 12 -> hour + 12
                amPm == "AM" && hour == 12 -> 0
                else -> hour
            }

            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour24)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            // If the target time is in the past, schedule for tomorrow
            if (target.before(now)) {
                target.add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
            return target.timeInMillis
        }

        return null
    }
}
