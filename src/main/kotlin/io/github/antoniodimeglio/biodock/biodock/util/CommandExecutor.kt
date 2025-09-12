package io.github.antoniodimeglio.biodock.biodock.util

interface CommandExecutor {
    suspend fun execute(vararg command: String): CommandResult
}

data class CommandResult(val exitCode: Int, val output: String, val error: String)