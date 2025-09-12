package io.github.antoniodimeglio.biodock.biodock.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultCommandExecutor : CommandExecutor {
    override suspend fun execute(vararg command: String): CommandResult {
        return withContext(Dispatchers.IO) {
            try {
                val process = ProcessBuilder(*command).start()
                val output = process.inputStream.bufferedReader().readText()
                val error = process.errorStream.bufferedReader().readText()
                val exitCode = process.waitFor()

                CommandResult(exitCode, output, error)
            } catch (e: Exception) {
                CommandResult(-1, "", e.message ?: "Unknown Error")
            }
        }
    }
}