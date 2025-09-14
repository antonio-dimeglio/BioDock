package io.github.antoniodimeglio.biodock.biodock.util

sealed class Result<out T> {
    data class Success<T>(val data: T, val message: String = "") : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
}