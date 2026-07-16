package com.example.final_project.core.mvi

/**
 * Marker interfaces for the MVI (Model-View-Intent) pattern used across all screens.
 *
 * Every feature screen defines its own:
 * - [UiState]  — immutable data class holding all UI state
 * - [UiEvent]  — sealed interface for user intents
 * - [UiEffect] — sealed interface for one-time side effects (snackbar, navigation)
 */
interface UiState

interface UiEvent

interface UiEffect
