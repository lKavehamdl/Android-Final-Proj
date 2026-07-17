package com.example.final_project.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.final_project.R
import com.example.final_project.ui.theme.MelodifySpacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AuthUiEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(context.getString(effect.messageRes))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(MelodifySpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(MelodifySpacing.sm))

            Text(
                text = stringResource(
                    if (state.isSignUpMode) R.string.auth_subtitle_signup
                    else R.string.auth_subtitle_signin,
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(MelodifySpacing.xxl))

            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text(stringResource(R.string.auth_email)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading,
                isError = state.emailErrorRes != null,
                supportingText = {
                    state.emailErrorRes?.let {
                        Text(stringResource(it), color = MaterialTheme.colorScheme.error)
                    }
                },
            )

            Spacer(modifier = Modifier.height(MelodifySpacing.sm))

            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text(stringResource(R.string.auth_password)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading,
                visualTransformation = PasswordVisualTransformation(),
                isError = state.passwordErrorRes != null,
                supportingText = {
                    state.passwordErrorRes?.let {
                        Text(stringResource(it), color = MaterialTheme.colorScheme.error)
                    }
                },
            )

            if (state.isSignUpMode) {
                Spacer(modifier = Modifier.height(MelodifySpacing.sm))
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    label = { Text(stringResource(R.string.auth_confirm_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isLoading,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = state.confirmPasswordErrorRes != null,
                    supportingText = {
                        state.confirmPasswordErrorRes?.let {
                            Text(stringResource(it), color = MaterialTheme.colorScheme.error)
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(MelodifySpacing.md))

            Button(
                onClick = viewModel::submit,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MelodifySpacing.xxl),
                shape = MaterialTheme.shapes.medium,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(MelodifySpacing.md),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(
                            if (state.isSignUpMode) R.string.auth_action_signup
                            else R.string.auth_action_signin,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            state.formErrorRes?.let { res ->
                Spacer(modifier = Modifier.height(MelodifySpacing.md))
                Text(
                    text = stringResource(res),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(MelodifySpacing.sm))

            TextButton(
                onClick = viewModel::toggleMode,
                enabled = !state.isLoading,
            ) {
                Text(
                    text = stringResource(
                        if (state.isSignUpMode) R.string.auth_have_account
                        else R.string.auth_need_account,
                    ),
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(MelodifySpacing.md),
        )
    }
}
