package com.example.final_project.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.final_project.R
import com.example.final_project.ui.theme.MelodifySpacing
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    onNavigateToFollows: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showEditUsername by remember { mutableStateOf(false) }

    // Refresh follower/following counts each time the Profile tab is opened.
    LaunchedEffect(Unit) { viewModel.refresh() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.onAvatarChanged(uri) }

    if (showEditUsername) {
        EditUsernameDialog(
            currentUsername = state.username,
            isSaving = state.isSavingUsername,
            onDismiss = { showEditUsername = false },
            onConfirm = { newName ->
                viewModel.onUsernameChanged(newName)
                showEditUsername = false
            },
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(MelodifySpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(MelodifySpacing.xl))

            // Avatar Section — camera affordance only, no default-person glyph underneath.
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(enabled = !state.isUploadingAvatar) { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (state.avatarUri != null) {
                    AsyncImage(
                        model = com.example.final_project.ui.components.avatarModel(state.avatarUri),
                        contentDescription = stringResource(R.string.cd_profile_picture),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                if (state.isUploadingAvatar) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    }
                } else {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = stringResource(R.string.profile_change_avatar),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(MelodifySpacing.md))

            // Name & Username
            Text(
                text = state.displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showEditUsername = true },
            ) {
                Text(
                    text = "@${state.username}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(MelodifySpacing.xs))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.profile_edit_username),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
            }

            Spacer(modifier = Modifier.height(MelodifySpacing.md))

            // Premium Badge or Free status
            AnimatedVisibility(visible = state.isPremium) {
                val premiumColor = MaterialTheme.colorScheme.tertiary
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = premiumColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, premiumColor),
                    modifier = Modifier
                        .padding(bottom = MelodifySpacing.md)
                        .clickable { viewModel.onDowngrade() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = MelodifySpacing.md, vertical = MelodifySpacing.xs)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = premiumColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(MelodifySpacing.xs))
                        Text(
                            text = stringResource(R.string.profile_premium_badge),
                            style = MaterialTheme.typography.labelLarge,
                            color = premiumColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (!state.isPremium) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(bottom = MelodifySpacing.md)
                ) {
                    Text(
                        text = stringResource(R.string.profile_free_badge),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = MelodifySpacing.md, vertical = MelodifySpacing.xs)
                    )
                }
            }

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(count = state.followersCount, label = stringResource(R.string.profile_followers))
                StatItem(count = state.followingCount, label = stringResource(R.string.profile_following), onClick = onNavigateToFollows)
            }

            Spacer(modifier = Modifier.height(MelodifySpacing.xl))

            // Upgrade Button (if free)
            AnimatedVisibility(visible = !state.isPremium) {
                var pressed by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (pressed) 0.95f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "ButtonScale"
                )

                Button(
                    onClick = viewModel::onUpgrade,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MelodifySpacing.xl)
                        .scale(scale)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    awaitFirstDown(requireUnconsumed = false)
                                    pressed = true
                                    waitForUpOrCancellation()
                                    pressed = false
                                }
                            }
                        },
                    enabled = !state.isUpgrading
                ) {
                    if (state.isUpgrading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(MelodifySpacing.sm))
                        Text(stringResource(R.string.profile_upgrade_button))
                    }
                }
            }

            Spacer(modifier = Modifier.height(MelodifySpacing.xl))
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun EditUsernameDialog(
    currentUsername: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember { mutableStateOf(currentUsername) }
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(R.string.profile_edit_username)) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                enabled = !isSaving,
                leadingIcon = { Text("@") },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(value) },
                enabled = !isSaving && value.isNotBlank(),
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun StatItem(count: Int, label: String, onClick: (() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick).padding(MelodifySpacing.xs) else Modifier)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
