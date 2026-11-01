package com.spendsight.app.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

enum class TimePeriod(val label: String) {
    TODAY("Today"),
    WEEK("7 Days"),
    MONTH("30 Days"),
    CUSTOM("Custom")
}

data class TimeFilter(
    val period: TimePeriod = TimePeriod.WEEK,
    val customStartEpochMs: Long? = null,
    val customEndEpochMs: Long? = null
) {
    fun toStartEpochMs(): Long {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)

        return when (period) {
            TimePeriod.TODAY -> {
                now.with(LocalTime.MIN).atZone(zone).toInstant().toEpochMilli()
            }
            TimePeriod.WEEK -> {
                now.minusDays(6).with(LocalTime.MIN).atZone(zone).toInstant().toEpochMilli()
            }
            TimePeriod.MONTH -> {
                now.minusDays(29).with(LocalTime.MIN).atZone(zone).toInstant().toEpochMilli()
            }
            TimePeriod.CUSTOM -> {
                customStartEpochMs ?: now.minusDays(6).with(LocalTime.MIN).atZone(zone).toInstant().toEpochMilli()
            }
        }
    }

    fun toEndEpochMs(): Long {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)

        return when (period) {
            TimePeriod.CUSTOM -> {
                customEndEpochMs ?: now.with(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
            }
            else -> {
                now.with(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
            }
        }
    }
}

