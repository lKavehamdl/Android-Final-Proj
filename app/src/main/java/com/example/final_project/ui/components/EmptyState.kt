package com.example.final_project.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.final_project.R
import com.example.final_project.ui.theme.MelodifySpacing

/**
 * Empty / no-results / offline placeholder for feature screens.
 *
 * **Locked signature — do not change without team agreement.**
 */
@Composable
fun EmptyState(
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Inbox,
    @StringRes actionLabelRes: Int? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MelodifySpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(MelodifySpacing.xxl),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(MelodifySpacing.md))
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(MelodifySpacing.sm))
        Text(
            text = stringResource(subtitleRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabelRes != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(MelodifySpacing.md))
            TextButton(onClick = onActionClick) {
                Text(text = stringResource(actionLabelRes))
            }
        }
    }
}

/** Convenience overload for the generic empty copy. */
@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    onActionClick: (() -> Unit)? = null,
) {
    EmptyState(
        titleRes = R.string.empty_state_generic_title,
        subtitleRes = R.string.empty_state_generic_subtitle,
        modifier = modifier,
        actionLabelRes = if (onActionClick != null) R.string.retry else null,
        onActionClick = onActionClick,
    )
}
