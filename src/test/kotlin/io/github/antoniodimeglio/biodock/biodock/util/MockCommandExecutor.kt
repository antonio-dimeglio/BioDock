package io.github.antoniodimeglio.biodock.biodock.util

class MockCommandExecutor : CommandExecutor {
    val executedCommands = mutableListOf<List<String>>()
    var mockResult = CommandResult(0, "", "")

    override suspend fun execute(vararg command: String): CommandResult {
        executedCommands.add(command.toList())
        return mockResult
    }
}