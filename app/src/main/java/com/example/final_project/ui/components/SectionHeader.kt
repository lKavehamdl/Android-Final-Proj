package com.example.final_project.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.final_project.R
import com.example.final_project.ui.theme.MelodifySpacing

/**
 * Section title row used above LazyRows / grids ("Trending", "New Releases", …).
 *
 * **Locked signature — do not change without team agreement.**
 */
@Composable
fun SectionHeader(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    onSeeAllClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MelodifySpacing.md, vertical = MelodifySpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (onSeeAllClick != null) {
            TextButton(onClick = onSeeAllClick) {
                Text(text = stringResource(R.string.section_see_all))
            }
        }
    }
}
