package io.github.antoniodimeglio.biodock.biodock.service

object DockerService {
}

/*
example from older code

    fun runFastqc(
        fastqPath: String,
        onLog: (String) -> Unit,
        onDone: (String) -> Unit
    ) {
        try {
            val processBuilder = ProcessBuilder(
                "docker", "run", "--rm",
                "-v", "${fastqPath}:/data/input.fastq",
                "-v", "${System.getProperty("user.dir")}:/data/output",
                "biocontainers/fastqc:v0.11.9_cv8",
                "fastqc", "/data/input.fastq", "-o", "/data/output"
            )
            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                onLog(line!!)
            }

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                val reportPath = "${System.getProperty("user.dir")}/input_fastqc.html"
                onDone(reportPath)
            } else {
                onLog("FastQC failed with exit code $exitCode")
            }

        } catch (e: Exception) {
            onLog("Error running FastQC: ${e.message}")
        }
    }
 */