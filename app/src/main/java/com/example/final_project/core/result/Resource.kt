package com.example.final_project.core.result

/**
 * Wraps asynchronous data results for repository → ViewModel communication.
 */
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()

    data class Success<T>(val data: T) : Resource<T>()

    data class Error(
        val message: String? = null,
        val throwable: Throwable? = null,
    ) : Resource<Nothing>()

    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = (this as? Success)?.data

    fun <R> map(transform: (T) -> R): Resource<R> = when (this) {
        is Loading -> Loading
        is Success -> Success(transform(data))
        is Error -> Error(message, throwable)
    }
}
