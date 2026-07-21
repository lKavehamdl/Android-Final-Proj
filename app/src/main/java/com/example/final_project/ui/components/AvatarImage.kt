package com.example.final_project.ui.components

import android.util.Base64
import java.nio.ByteBuffer

/**
 * Converts a stored avatar value into something Coil's `AsyncImage` can load.
 *
 * Avatars are stored directly in `profiles.avatar_url` (no Supabase Storage
 * bucket needed) as a `data:image/...;base64,...` URI. Coil can't fetch a data
 * URI string directly, but it *can* load a [ByteBuffer], so we decode the
 * base64 payload here. Plain `http(s)` URLs (e.g. seed data) pass straight
 * through.
 *
 * Returns `null` for blank input so callers can keep their `if (x != null)`
 * placeholder logic.
 */
fun avatarModel(raw: String?): Any? {
    if (raw.isNullOrBlank()) return null
    if (!raw.startsWith("data:")) return raw
    val marker = "base64,"
    val idx = raw.indexOf(marker)
    if (idx == -1) return null
    return try {
        val bytes = Base64.decode(raw.substring(idx + marker.length), Base64.DEFAULT)
        ByteBuffer.wrap(bytes)
    } catch (e: Exception) {
        null
    }
}
