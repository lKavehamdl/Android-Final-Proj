package com.example.final_project.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.final_project.R
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val absoluteDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d").withZone(ZoneId.systemDefault())

/**
 * Compact, localized "time ago" label: just now / 5m ago / 2h ago / 3d ago,
 * falling back to an absolute date (MMM d) beyond a week.
 */
@Composable
fun relativeTimeLabel(iso: String): String {
    val seconds = elapsedSeconds(iso) ?: return ""
    return when {
        seconds < 60 -> stringResource(R.string.time_just_now)
        seconds < 3600 -> stringResource(R.string.time_minutes_ago, (seconds / 60).toInt())
        seconds < 86_400 -> stringResource(R.string.time_hours_ago, (seconds / 3600).toInt())
        seconds < 604_800 -> stringResource(R.string.time_days_ago, (seconds / 86_400).toInt())
        else -> absoluteDateFormatter.format(Instant.parse(iso))
    }
}

/** Seconds since [iso], or null if it can't be parsed. Kept out of the composable so parsing can use try/catch. */
private fun elapsedSeconds(iso: String): Long? = try {
    Duration.between(Instant.parse(iso), Instant.now()).seconds.coerceAtLeast(0)
} catch (e: Exception) {
    null
}
