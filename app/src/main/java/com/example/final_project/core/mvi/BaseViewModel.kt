package com.example.final_project.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel for every feature screen.
 *
 * Copy this pattern when creating a new screen ViewModel:
 * ```
 * class HomeViewModel : BaseViewModel<HomeUiState, HomeUiEvent, HomeUiEffect>(
 *     initialState = HomeUiState(),
 * ) {
 *     override fun onEvent(event: HomeUiEvent) { ... }
 * }
 * ```
 */
abstract class BaseViewModel<S : UiState, E : UiEvent, F : UiEffect>(
    initialState: S,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<F>(Channel.BUFFERED)
    val effect: Flow<F> = _effect.receiveAsFlow()

    protected fun setState(reducer: S.() -> S) {
        _state.update(reducer)
    }

    protected fun currentState(): S = _state.value

    protected fun sendEffect(effect: F) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    abstract fun onEvent(event: E)
}
