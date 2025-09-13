package io.github.antoniodimeglio.biodock.biodock.util

sealed class Result {
    data class Success(val message: String) : Result()
    data class Error(val message: String) : Result()
}