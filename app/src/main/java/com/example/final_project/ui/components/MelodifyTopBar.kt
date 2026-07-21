package com.example.final_project.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.final_project.R
import com.example.final_project.ui.theme.MelodifySpacing

/**
 * Shared top bar for all main tabs.
 *
 * Spec (Project.md §5): logo + app name on the **right**, profile / bell / gear on the **left**.
 * Forced LTR so this physical placement stays correct in both FA (RTL) and EN.
 */
@Composable
fun MelodifyTopBar(
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    profileImageUrl: String? = null,
    notificationCount: Int = 0,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal = MelodifySpacing.md,
                    vertical = MelodifySpacing.sm,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Left: profile + notifications + settings
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(
                    imageUrl = profileImageUrl,
                    onClick = onProfileClick,
                )
                IconButton(onClick = onNotificationsClick) {
                    BadgedBox(
                        badge = {
                            if (notificationCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError,
                                ) {
                                    Text(if (notificationCount > 99) "99+" else notificationCount.toString())
                                }
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = stringResource(R.string.notifications_title),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.cd_settings),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Right: logo + brand name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = stringResource(R.string.cd_logo),
                    // onBackground = black in light mode, near-white in dark —
                    // the old white-only launcher glyph vanished on light bg.
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(MelodifySpacing.lg),
                )
                Spacer(modifier = Modifier.width(MelodifySpacing.xs))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * Shared back-navigation top bar for secondary/detail screens.
 *
 * Custom-laid-out (not [androidx.compose.material3.CenterAlignedTopAppBar])
 * so the leading and trailing slots are always the same minimum width —
 * that's what keeps a truly-centered title even when there are no actions,
 * instead of the default's fixed-72dp nav-icon slot dwarfing an empty
 * actions slot and dragging the title visibly off-center. Sits flush with
 * the screen background (no elevation seam) and is a little taller than
 * the M3 default for more presence.
 */
@Composable
fun MelodifyBackTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    title: @Composable () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = modifier.fillMaxWidth()) {
        Column {
            Spacer(modifier = Modifier.statusBarsPadding())
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = MelodifySpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.widthIn(min = 56.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .padding(start = MelodifySpacing.sm)
                            .size(40.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = MelodifySpacing.xs),
                    contentAlignment = Alignment.Center,
                ) {
                    title()
                }

                Row(
                    modifier = Modifier.widthIn(min = 56.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions,
                )
            }
        }
    }
}

@Composable
fun MelodifyBackTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    MelodifyBackTopBar(onBack = onBack, modifier = modifier, actions = actions) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ProfileAvatar(
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val size = MelodifySpacing.topBarAvatar
    if (imageUrl.isNullOrBlank()) {
        IconButton(onClick = onClick, modifier = modifier) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = stringResource(R.string.cd_profile_picture),
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(MelodifySpacing.xs),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    } else {
        AsyncImage(
            model = avatarModel(imageUrl),
            contentDescription = stringResource(R.string.cd_profile_picture),
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .clickable(onClick = onClick),
        )
    }
}
