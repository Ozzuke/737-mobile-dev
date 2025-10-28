package com.example.project.ui

/**
 * A sealed class representing the different states of a UI component that loads data.
 * This makes it easy and safe to handle loading, success, error, and empty states in the UI.
 */
sealed class UiState<out T> {
    /** Represents an idle, initial state before any loading has occurred. */
    object Idle : UiState<Nothing>()

    /** Represents a loading state, typically used to show a progress indicator. */
    object Loading : UiState<Nothing>()

    /**
     * Represents a successful state, containing the loaded data.
     * @param data The data that was successfully loaded.
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * Represents an error state, containing a message and the original exception.
     * @param message A user-friendly error message to display.
     * @param exception The original throwable that caused the error, for debugging purposes.
     */
    data class Error(val message: String, val exception: Throwable) : UiState<Nothing>()

    /** Represents a state where the operation was successful but resulted in no data. */
    object Empty : UiState<Nothing>()
}

